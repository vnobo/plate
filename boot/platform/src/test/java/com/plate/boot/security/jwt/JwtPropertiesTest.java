package com.plate.boot.security.jwt;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
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
        assertThat(p.secret()).isEqualTo(SECRET);
        assertThat(p.issuer()).isEqualTo("my-issuer");
        assertThat(p.accessTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(p.refreshTtl()).isEqualTo(Duration.ofDays(1));
    }

    @Test
    void defaultsIssuerAndTtls() {
        JwtProperties p = new JwtProperties(SECRET, null, null, null);
        assertThat(p.issuer()).isEqualTo("plate");
        assertThat(p.accessTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(p.refreshTtl()).isEqualTo(Duration.ofDays(7));
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

    @Test
    void secretKeyUsesHmacSha256() {
        JwtProperties p = new JwtProperties(SECRET, "iss", Duration.ofMinutes(5), Duration.ofDays(1));
        SecretKey key = p.secretKey();
        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(key.getEncoded().length).isEqualTo(SECRET.getBytes(StandardCharsets.UTF_8).length);
    }

    @Test
    void beanAccessorsMatchCompactAccessors() {
        JwtProperties p = new JwtProperties();
        p.setSecret(SECRET);
        p.setIssuer("my-issuer");
        p.setAccessTokenTtl(Duration.ofMinutes(5));
        p.setRefreshTokenTtl(Duration.ofDays(1));

        assertThat(p.getSecret()).isEqualTo(p.secret());
        assertThat(p.getIssuer()).isEqualTo(p.issuer());
        assertThat(p.getAccessTokenTtl()).isEqualTo(p.accessTtl());
        assertThat(p.getRefreshTokenTtl()).isEqualTo(p.refreshTtl());
    }

    @Test
    void defaultsApplyToBeanConstructor() {
        JwtProperties p = new JwtProperties();
        assertThat(p.issuer()).isEqualTo("plate");
        assertThat(p.accessTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(p.refreshTtl()).isEqualTo(Duration.ofDays(7));
    }
}
