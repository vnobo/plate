package com.plate.boot.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Self-signed JWT settings.
 *
 * @param secret     HMAC signing secret, at least 32 bytes (HS256 requires 256-bit keys).
 * @param issuer     {@code iss} claim value.
 * @param accessTtl  access token lifetime, short lived.
 * @param refreshTtl refresh token lifetime, also the TTL of the rotation/revocation records.
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, String issuer, Duration accessTtl, Duration refreshTtl) {

    public JwtProperties {
        if (!StringUtils.hasText(secret) || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("security.jwt.secret must be at least 32 bytes for HS256");
        }
        issuer = StringUtils.hasText(issuer) ? issuer : "plate";
        accessTtl = accessTtl == null ? Duration.ofMinutes(15) : accessTtl;
        refreshTtl = refreshTtl == null ? Duration.ofDays(7) : refreshTtl;
        if (accessTtl.compareTo(refreshTtl) >= 0) {
            throw new IllegalStateException("security.jwt.access-ttl must be shorter than refresh-ttl");
        }
    }

    public SecretKey secretKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
