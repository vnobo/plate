package com.plate.boot.security.jwt;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtProperties}.
 */
class JwtPropertiesTest {

    private static final String SECRET = "a-valid-secret-at-least-thirty-two-bytes-long";

    @Test
    void buildsWithAllValues() {
        JwtProperties p = new JwtProperties(SECRET, "my-issuer", Duration.ofMinutes(5), Duration.ofDays(1));
        assertThat(p.getSecret()).isEqualTo(SECRET);
        assertThat(p.getIssuer()).isEqualTo("my-issuer");
        assertThat(p.getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(p.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(1));
    }

    @Test
    void defaultsIssuerAndTtls() {
        JwtProperties p = new JwtProperties(SECRET, null, null, null);
        assertThat(p.getIssuer()).isEqualTo("plate");
        assertThat(p.getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(p.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void defaultsApplyToBeanConstructor() {
        JwtProperties p = new JwtProperties();
        assertThat(p.getIssuer()).isEqualTo("plate");
        assertThat(p.getAccessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(p.getRefreshTokenTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(p.getSecret()).isNotBlank();
    }

    @Test
    void rejectsBlankSecret() {
        assertThatThrownBy(() -> new JwtProperties("   ", "iss", Duration.ofMinutes(1), Duration.ofDays(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtProperties("too-short", "iss", Duration.ofMinutes(1), Duration.ofDays(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNullSecret() {
        assertThatThrownBy(() -> new JwtProperties(null, "iss", Duration.ofMinutes(1), Duration.ofDays(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsAccessTtlNotShorterThanRefresh() {
        assertThatThrownBy(() -> new JwtProperties(SECRET, "iss", Duration.ofDays(7), Duration.ofDays(7)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties(SECRET, "iss", Duration.ofDays(8), Duration.ofDays(7)))
                .isInstanceOf(IllegalStateException.class);
    }
}
