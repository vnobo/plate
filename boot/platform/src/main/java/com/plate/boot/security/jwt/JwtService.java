package com.plate.boot.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * The single JWT service: token generation, parsing/validation and refresh.
 * <p>
 * Tokens are signed with HS256 using a 256-bit key derived (SHA-256) from the configured
 * secret. Claims: {@code sub} (username), {@code code} (user business code), {@code cty}
 * ({@link ClientType}), {@code typ} ({@link TokenType}), {@code authorities} (access tokens
 * only), plus the standard {@code iss/iat/exp/jti}.
 * <p>
 * Every validation failure is normalized to {@link BadCredentialsException} so both the
 * resource-server chain and the refresh endpoint map it to a consistent 401.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Service
public class JwtService {

    public static final String CLAIM_CODE = "code";
    public static final String CLAIM_CLIENT_TYPE = "cty";
    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String CLAIM_AUTHORITIES = "authorities";
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    private final JwtProperties properties;
    private final SecurityManager securityManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Autowired
    public JwtService(JwtProperties properties, SecurityManager securityManager) {
        this.properties = properties;
        this.securityManager = securityManager;
        SecretKeySpec secretKey = signingKey(properties.getSecret());
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
        // Issuer and validator share one clock, so expiry is enforced strictly.
        decoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
        this.jwtDecoder = decoder;
    }

    private static JwtPayload toPayload(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        ClientType clientType = ClientType.valueOf(String.valueOf(claims.get(CLAIM_CLIENT_TYPE)));
        TokenType tokenType = TokenType.valueOf(String.valueOf(claims.get(CLAIM_TOKEN_TYPE)));
        String code = Objects.isNull(claims.get(CLAIM_CODE)) ? null : String.valueOf(claims.get(CLAIM_CODE));
        return new JwtPayload(jwt.getSubject(), code, clientType, tokenType,
                toAuthorities(claims.get(CLAIM_AUTHORITIES)), jwt.getId(),
                jwt.getIssuedAt(), jwt.getExpiresAt());
    }

    /**
     * Parses and validates a compact JWT, mapping every failure to {@link BadCredentialsException}.
     */
    public Mono<JwtPayload> parse(String token) {
        return Mono.fromCallable(() -> toPayload(this.jwtDecoder.decode(token)))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> !(e instanceof BadCredentialsException),
                        e -> new BadCredentialsException("Invalid JWT token: " + e.getMessage(), e));
    }

    /**
     * Derives the HS256 key. Shared with the resource-server decoder bean so every consumer
     * validates with exactly the key tokens were signed with.
     */
    public static SecretKeySpec signingKey(String secret) {
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(key, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Issues a fresh access/refresh pair for the given user and client platform.
     */
    public Mono<TokenPair> issueTokenPair(SecurityDetails user, ClientType clientType) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(clientType, "clientType must not be null");
        Instant now = Instant.now();
        return Mono.zip(createToken(user, clientType, TokenType.ACCESS, now),
                        createToken(user, clientType, TokenType.REFRESH, now))
                .map(pair -> new TokenPair(pair.getT1(), pair.getT2(), TOKEN_TYPE_BEARER,
                        this.properties.getAccessTokenTtl().toSeconds(), clientType));
    }

    /**
     * Refreshes a pair: validate the refresh JWT, reject access tokens and expired tokens,
     * reload the principal (so authority changes and deleted accounts take effect), then mint
     * a new pair for the same client platform.
     */
    public Mono<TokenPair> refresh(String refreshToken) {
        return parse(refreshToken)
                .filter(payload -> TokenType.REFRESH == payload.tokenType())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Not a refresh token")))
                .flatMap(payload -> this.securityManager.loadUserDetails(payload.username())
                        .ofType(SecurityDetails.class)
                        .switchIfEmpty(Mono.error(new BadCredentialsException("Refresh token user no longer exists")))
                        .flatMap(details -> issueTokenPair(details, payload.clientType())));
    }

    @SuppressWarnings("unchecked")
    private static List<String> toAuthorities(Object claim) {
        if (claim instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private Mono<String> createToken(SecurityDetails user, ClientType clientType,
                                     TokenType tokenType, Instant now) {
        Duration ttl = tokenType == TokenType.ACCESS
                ? this.properties.getAccessTokenTtl() : this.properties.getRefreshTokenTtl();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(this.properties.getIssuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .id(UUID.randomUUID().toString())
                .claim(CLAIM_CLIENT_TYPE, clientType.name())
                .claim(CLAIM_TOKEN_TYPE, tokenType.name());
        if (Objects.nonNull(user.getCode())) {
            claims.claim(CLAIM_CODE, user.getCode().toString());
        }
        if (tokenType == TokenType.ACCESS) {
            List<String> authorities = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).sorted().toList();
            claims.claim(CLAIM_AUTHORITIES, authorities);
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return Mono.fromCallable(() ->
                        this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
