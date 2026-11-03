package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TokenService}. Redis is mocked so every branch (rotation, replay,
 * concurrent reuse, generation mismatch, expiry) is exercised without a live store.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TokenServiceTest {

    private static final String SECRET = "a-valid-secret-at-least-thirty-two-bytes-long";
    private static final String REFRESH_PREFIX = "plate:tokens:refresh:";
    private static final String USED_PREFIX = "plate:tokens:used:";
    private static final String GEN_PREFIX = "plate:tokens:gen:";

    @Mock
    private ReactiveStringRedisTemplate redis;
    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private final JwtProperties props = new JwtProperties(SECRET, "plate", Duration.ofMinutes(15), Duration.ofDays(7));
    private final JwtEncoder encoder = NimbusJwtEncoder.withSecretKey(props.secretKey()).build();
    private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(props.secretKey()).macAlgorithm(MacAlgorithm.HS256).build();

    private TokenService service;

    @BeforeEach
    void init() {
        when(redis.opsForValue()).thenReturn(valueOps);
        service = new TokenService(encoder, props, redis);
    }

    private static SecurityDetails detailsOf(UUID code) {
        SecurityDetails d = new SecurityDetails(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("username", "user"), "username");
        d.setCode(code);
        d.setUsername("user");
        return d;
    }

    @Test
    void issueReturnsJwtAndRefresh() {
        UUID code = UUID.randomUUID();
        when(valueOps.get(contains(GEN_PREFIX))).thenReturn(Mono.empty());
        when(valueOps.set(anyString(), anyString(), eq(props.refreshTtl()))).thenReturn(Mono.just(true));

        StepVerifier.create(service.issue(detailsOf(code)))
                .assertNext(pair -> {
                    assertThat(pair.refreshToken()).isNotNull();
                    Jwt jwt = decoder.decode(pair.accessToken());
                    assertThat(jwt.getClaimAsString("code")).isEqualTo(code.toString());
                    long gen = ((Number) jwt.getClaim("gen")).longValue();
                    assertThat(gen).isEqualTo(0L);
                    assertThat(jwt.getSubject()).isEqualTo("user");
                })
                .verifyComplete();
    }

    @Test
    void consumeReturnsRotationForValidToken() {
        UUID code = UUID.randomUUID();
        String raw = code + "|user|fam|0";
        when(valueOps.getAndDelete(contains(REFRESH_PREFIX))).thenReturn(Mono.just(raw));
        when(valueOps.get(contains(GEN_PREFIX))).thenReturn(Mono.just("0"));
        when(valueOps.set(contains(USED_PREFIX), anyString(), eq(props.refreshTtl()))).thenReturn(Mono.just(true));

        StepVerifier.create(service.consume("any-refresh-token"))
                .assertNext(rotation -> {
                    assertThat(rotation.userCode()).isEqualTo(code);
                    assertThat(rotation.username()).isEqualTo("user");
                    assertThat(rotation.family()).isEqualTo("fam");
                    assertThat(rotation.generation()).isZero();
                })
                .verifyComplete();
    }

    @Test
    void consumeRejectsMalformedToken() {
        when(valueOps.getAndDelete(anyString())).thenReturn(Mono.just("only-one-part"));
        StepVerifier.create(service.consume("x"))
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(InvalidBearerTokenException.class)
                        .hasMessageContaining("malformed"))
                .verify();
    }

    @Test
    void consumeRejectsUnparseableStoredValue() {
        when(valueOps.getAndDelete(anyString())).thenReturn(Mono.just("not-a-uuid|user|fam|0"));
        StepVerifier.create(service.consume("x"))
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(InvalidBearerTokenException.class)
                        .hasMessageContaining("malformed"))
                .verify();
    }

    @Test
    void consumeRejectsWhenGenerationMismatch() {
        UUID code = UUID.randomUUID();
        when(valueOps.getAndDelete(anyString())).thenReturn(Mono.just(code + "|user|fam|0"));
        when(valueOps.get(contains(GEN_PREFIX))).thenReturn(Mono.just("5"));
        when(valueOps.set(contains(USED_PREFIX), anyString(), eq(props.refreshTtl()))).thenReturn(Mono.just(true));
        StepVerifier.create(service.consume("x"))
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(InvalidBearerTokenException.class)
                        .hasMessageContaining("revoked"))
                .verify();
    }

    @Test
    void consumeRejectsUnknownToken() {
        when(valueOps.getAndDelete(anyString())).thenReturn(Mono.empty());
        when(valueOps.get(contains(USED_PREFIX))).thenReturn(Mono.empty());
        StepVerifier.create(service.consume("unknown"))
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(InvalidBearerTokenException.class)
                        .hasMessageContaining("invalid or expired"))
                .verify();
    }

    @Test
    void consumeRejectsConcurrentReuseWithinGrace() {
        UUID code = UUID.randomUUID();
        when(valueOps.getAndDelete(anyString())).thenReturn(Mono.empty());
        when(valueOps.get(contains(USED_PREFIX)))
                .thenReturn(Mono.just(code + "|" + (Instant.now().getEpochSecond() - 5)));
        StepVerifier.create(service.consume("x"))
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(InvalidBearerTokenException.class)
                        .hasMessageContaining("concurrent"))
                .verify();
    }

    @Test
    void consumeRevokesAllOnReplayAfterGrace() {
        UUID code = UUID.randomUUID();
        when(valueOps.getAndDelete(anyString())).thenReturn(Mono.empty());
        when(valueOps.get(contains(USED_PREFIX)))
                .thenReturn(Mono.just(code + "|" + (Instant.now().getEpochSecond() - 100)));
        when(valueOps.increment(contains(GEN_PREFIX))).thenReturn(Mono.just(1L));
        when(redis.expire(anyString(), any())).thenReturn(Mono.just(true));
        StepVerifier.create(service.consume("x"))
                .expectErrorSatisfies(e -> assertThat(e)
                        .isInstanceOf(InvalidBearerTokenException.class)
                        .hasMessageContaining("revoked"))
                .verify();
        verify(valueOps).increment(contains(GEN_PREFIX));
    }

    @Test
    void rotateIssuesNewPair() {
        UUID code = UUID.randomUUID();
        TokenService.Rotation rotation = new TokenService.Rotation(code, "user", "fam", 0L);
        when(valueOps.get(contains(GEN_PREFIX))).thenReturn(Mono.just("0"));
        when(valueOps.set(anyString(), anyString(), eq(props.refreshTtl()))).thenReturn(Mono.just(true));
        StepVerifier.create(service.rotate(detailsOf(code), rotation))
                .assertNext(pair -> {
                    assertThat(pair.accessToken()).isNotNull();
                    assertThat(pair.refreshToken()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void isCurrentReflectsGeneration() {
        when(valueOps.get(anyString())).thenReturn(Mono.just("3"));
        StepVerifier.create(service.isCurrent(UUID.randomUUID(), 3)).expectNext(true).verifyComplete();
        StepVerifier.create(service.isCurrent(UUID.randomUUID(), 2)).expectNext(false).verifyComplete();
    }

    @Test
    void isCurrentDefaultsToZero() {
        when(valueOps.get(anyString())).thenReturn(Mono.empty());
        StepVerifier.create(service.isCurrent(UUID.randomUUID(), 0)).expectNext(true).verifyComplete();
    }

    @Test
    void consumeFailsWhenSha256Unavailable() {
        try (var mocked = mockStatic(MessageDigest.class)) {
            mocked.when(() -> MessageDigest.getInstance("SHA-256")).thenThrow(new NoSuchAlgorithmException());
            assertThatThrownBy(() -> service.consume("any"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SHA-256 unavailable");
        }
    }

    @Test
    void revokeAllIncrementsAndExpires() {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(2L));
        when(redis.expire(anyString(), any())).thenReturn(Mono.just(true));
        StepVerifier.create(service.revokeAll(UUID.randomUUID())).expectNext(2L).verifyComplete();
    }
}
