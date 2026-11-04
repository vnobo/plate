package com.plate.boot.security.jwt;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.plate.boot.security.SecurityDetails;
import lombok.extern.log4j.Log4j2;
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
 * Issues and validates the JWT access/refresh token pairs used by stateless clients
 * (PC, WeChat mini-program and native app) of the plate platform.
 * <p>
 * Tokens are signed with HS256 using a key derived (SHA-256) from the configured secret,
 * so secrets of any length are accepted while the effective key always satisfies the
 * 256-bit minimum required by the algorithm. Claim layout:
 * </p>
 * <ul>
 *   <li>{@code sub} — username; {@code code} — user business code</li>
 *   <li>{@code cty} — {@link ClientType}; {@code typ} — {@link TokenType}</li>
 *   <li>{@code authorities} — granted authority names (access tokens only)</li>
 *   <li>{@code iss}, {@code iat}, {@code exp}, {@code jti} — standard claims</li>
 * </ul>
 * <p>
 * Expiration ({@code exp}) and not-before are validated by the decoder; every other
 * validation failure (bad signature, malformed token, unknown claims) is normalized to
 * {@link BadCredentialsException} so callers can map it to a 401 response consistently.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Service
public class JwtService {

    /**
     * Claim holding the user business code.
     */
    public static final String CLAIM_CODE = "code";

    /**
     * Claim holding the issuing {@link ClientType}.
     */
    public static final String CLAIM_CLIENT_TYPE = "cty";

    /**
     * Claim holding the {@link TokenType}.
     */
    public static final String CLAIM_TOKEN_TYPE = "typ";

    /**
     * Claim holding the granted authority names.
     */
    public static final String CLAIM_AUTHORITIES = "authorities";

    /**
     * Token type value returned to clients, per RFC 6750 bearer usage.
     */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    private final JwtProperties properties;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    /**
     * Creates a service instance backed by Nimbus JOSE+JWT, deriving the HMAC key
     * from the configured secret.
     *
     * @param properties the JWT configuration properties
     */
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        SecretKeySpec secretKey = signingKey(properties.getSecret());
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256).build();
        // Both issuer and validator run server-side on the same clock: enforce expiry strictly.
        decoder.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
        this.jwtDecoder = decoder;
    }

    /**
     * Issues a fresh access/refresh token pair for the given user and client platform.
     *
     * @param user       the authenticated user details used to populate the claims
     * @param clientType the client platform the pair is issued for
     * @return a {@link Mono} emitting the issued {@link TokenPair}
     */
    public Mono<TokenPair> issueTokenPair(SecurityDetails user, ClientType clientType) {
        Objects.requireNonNull(user, "user must not be null");
        Objects.requireNonNull(clientType, "clientType must not be null");
        Instant now = Instant.now();
        Mono<String> access = createToken(user, clientType, TokenType.ACCESS, now);
        Mono<String> refresh = createToken(user, clientType, TokenType.REFRESH, now);
        return Mono.zip(access, refresh)
                .map(pair -> new TokenPair(pair.getT1(), pair.getT2(), TOKEN_TYPE_BEARER,
                        this.properties.getAccessTokenTtl().toSeconds(), clientType));
    }

    /**
     * Parses and validates a compact JWT, mapping every failure to {@link BadCredentialsException}.
     *
     * @param token the compact serialized JWT
     * @return a {@link Mono} emitting the validated {@link JwtPayload}
     */
    public Mono<JwtPayload> parse(String token) {
        return Mono.fromCallable(() -> toPayload(this.jwtDecoder.decode(token)))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> !(e instanceof BadCredentialsException),
                        e -> new BadCredentialsException("Invalid JWT token: " + e.getMessage(), e));
    }

    /**
     * Builds and signs a token of the given kind for the user.
     */
    private Mono<String> createToken(SecurityDetails user, ClientType clientType,
                                     TokenType tokenType, Instant now) {
        var ttl = tokenType == TokenType.ACCESS
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

    /**
     * Converts validated JWT claims into a {@link JwtPayload} record.
     */
    private static JwtPayload toPayload(Jwt jwt) {
        Map<String, Object> claims = jwt.getClaims();
        ClientType clientType = ClientType.valueOf(
                String.valueOf(claims.get(CLAIM_CLIENT_TYPE)));
        TokenType tokenType = TokenType.valueOf(
                String.valueOf(claims.get(CLAIM_TOKEN_TYPE)));
        String code = Objects.isNull(claims.get(CLAIM_CODE)) ? null : String.valueOf(claims.get(CLAIM_CODE));
        return new JwtPayload(jwt.getSubject(), code, clientType, tokenType,
                toAuthorities(claims.get(CLAIM_AUTHORITIES)), jwt.getId(),
                jwt.getIssuedAt(), jwt.getExpiresAt());
    }

    /**
     * Normalizes the authorities claim, tolerating its absence on refresh tokens.
     */
    @SuppressWarnings("unchecked")
    private static List<String> toAuthorities(Object claim) {
        if (claim instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    /**
     * Derives a 256-bit HMAC key from the configured secret via SHA-256. Shared with the
     * resource-server decoder/encoder beans so every token service uses the same key.
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
}
