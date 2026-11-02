package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Issues and rotates the JWT access token / opaque refresh token pair.
 *
 * <p>Refresh tokens are single use. Every rotation writes a "used" marker; presenting a used
 * token again is a replay, and is answered by bumping the user's generation counter, which
 * invalidates every access token and every refresh token that user holds.
 *
 * <p>shortcut: refresh token state lives in Redis with {@code plate:tokens:*} keys and no
 * per-token revocation list; a user-level generation counter is enough to kill all sessions,
 * upgrade to per-session revocation only if single-device logout is required.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class TokenService {

    private static final String REFRESH_PREFIX = "plate:tokens:refresh:";
    private static final String USED_PREFIX = "plate:tokens:used:";
    private static final String GENERATION_PREFIX = "plate:tokens:gen:";

    /**
     * Two tabs / a retried request can submit the same refresh token within a second or two.
     * Inside this window it is treated as a concurrent refresh, not as a replay.
     */
    private static final long CONCURRENT_GRACE_SECONDS = 10;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final ReactiveStringRedisTemplate redis;

    /**
     * A freshly rotated pair.
     */
    public record TokenPair(String accessToken, String refreshToken, long expiresIn) {
    }

    /**
     * What a valid refresh token resolves to. The username is stored alongside the token so
     * rotation does not need a second lookup before it can re-issue an access token.
     */
    public record Rotation(UUID userCode, String username, String family, long generation) {
    }

    /**
     * Issues a new pair, starting a new token family (one session).
     */
    public Mono<TokenPair> issue(SecurityDetails details) {
        UUID userCode = details.getCode();
        String family = UUID.randomUUID().toString();
        return currentGeneration(userCode).flatMap(generation -> {
            String accessToken = accessToken(details, generation);
            return issueRefresh(userCode, details.getUsername(), family, generation)
                    .map(refreshToken -> new TokenPair(accessToken, refreshToken, properties.accessTtl().toSeconds()));
        });
    }

    /**
     * Consumes a refresh token exactly once and returns what is needed to mint the next pair.
     * Replay (the same token submitted twice, outside the concurrency grace window) revokes all
     * sessions of that user.
     */
    public Mono<Rotation> consume(String refreshToken) {
        String hash = sha256(refreshToken);
        return this.redis.opsForValue().getAndDelete(REFRESH_PREFIX + hash)
                .flatMap(raw -> {
                    String[] parts = raw.split("\\|", 4);
                    if (parts.length != 4) {
                        return Mono.<Rotation>error(new InvalidBearerTokenException("Refresh token is malformed"));
                    }
                    UUID userCode = UUID.fromString(parts[0]);
                    String username = parts[1];
                    String family = parts[2];
                    long generation = Long.parseLong(parts[3]);
                    return markUsed(hash, userCode)
                            .then(currentGeneration(userCode))
                            .flatMap(current -> current == generation
                                    ? Mono.just(new Rotation(userCode, username, family, generation))
                                    : Mono.<Rotation>error(new InvalidBearerTokenException(
                                            "Refresh token has been revoked")));
                })
                .switchIfEmpty(Mono.defer(() -> detectReplay(hash)));
    }

    /**
     * Issues the next pair of an existing token family, keeping the same family id so that a later
     * replay of any of its members can be attributed to the same session chain.
     */
    public Mono<TokenPair> rotate(SecurityDetails details, Rotation rotation) {
        return currentGeneration(details.getCode()).flatMap(generation -> {
            String accessToken = accessToken(details, generation);
            return issueRefresh(rotation.userCode(), details.getUsername(), rotation.family(), generation)
                    .map(refreshToken -> new TokenPair(accessToken, refreshToken,
                            properties.accessTtl().toSeconds()));
        });
    }

    /**
     * Reports whether {@code generation} is still the user's current one, i.e. the token was not
     * revoked by a logout, a replay or a password change.
     */
    public Mono<Boolean> isCurrent(UUID userCode, long generation) {
        return currentGeneration(userCode).map(current -> current == generation);
    }

    /**
     * Kills every session of the user: old access tokens fail {@link #isCurrent} and old refresh
     * tokens fail {@link #consume}.
     */
    public Mono<Long> revokeAll(UUID userCode) {
        String key = GENERATION_PREFIX + userCode;
        return this.redis.opsForValue().increment(key)
                .flatMap(generation -> this.redis.expire(key, properties.refreshTtl()).thenReturn(generation));
    }

    private String accessToken(SecurityDetails details, long generation) {
        Instant now = Instant.now();
        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).distinct().toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(details.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTtl()))
                .id(UUID.randomUUID().toString())
                .claim("code", Objects.toString(details.getCode(), null))
                .claim("gen", generation)
                .claim("authorities", authorities)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private Mono<String> issueRefresh(UUID userCode, String username, String family, long generation) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String value = userCode + "|" + username + "|" + family + "|" + generation;
        return this.redis.opsForValue()
                .set(REFRESH_PREFIX + sha256(token), value, properties.refreshTtl())
                .thenReturn(token);
    }

    private Mono<Long> currentGeneration(UUID userCode) {
        return this.redis.opsForValue().get(GENERATION_PREFIX + userCode)
                .map(Long::valueOf)
                .defaultIfEmpty(0L);
    }

    private Mono<Void> markUsed(String hash, UUID userCode) {
        String value = userCode + "|" + Instant.now().getEpochSecond();
        return this.redis.opsForValue().set(USED_PREFIX + hash, value, properties.refreshTtl()).then();
    }

    private Mono<Rotation> detectReplay(String hash) {
        return this.redis.opsForValue().get(USED_PREFIX + hash)
                .flatMap(value -> {
                    String[] parts = value.split("\\|", 2);
                    UUID userCode = UUID.fromString(parts[0]);
                    long usedAt = Long.parseLong(parts[1]);
                    if (Instant.now().getEpochSecond() - usedAt <= CONCURRENT_GRACE_SECONDS) {
                        return Mono.<Rotation>error(new InvalidBearerTokenException(
                                "Refresh token is already being rotated by a concurrent request"));
                    }
                    log.warn("Refresh token replay detected for user {}, revoking all sessions", userCode);
                    return revokeAll(userCode).then(Mono.<Rotation>error(new InvalidBearerTokenException(
                            "Refresh token replay detected, all sessions of the user have been revoked")));
                })
                .switchIfEmpty(Mono.<Rotation>error(
                        new InvalidBearerTokenException("Refresh token is invalid or expired")));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
