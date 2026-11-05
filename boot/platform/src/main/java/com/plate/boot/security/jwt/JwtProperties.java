package com.plate.boot.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Configuration properties for JWT authentication, bound from {@code plate.security.jwt}.
 * <p>
 * {@link JwtService} derives a 256-bit HS256 key (via SHA-256) from {@code secret}, so any
 * string length is accepted; production deployments MUST override the default. TTLs control
 * access and refresh token lifetimes respectively.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@Component
@ConfigurationProperties(prefix = "plate.security.jwt")
public class JwtProperties {

    /**
     * HS256 needs a 256-bit key when the secret is used verbatim.
     */
    private static final int MIN_SECRET_BYTES = 32;
    private static final Duration DEFAULT_ACCESS_TTL = Duration.ofMinutes(15);
    private static final Duration DEFAULT_REFRESH_TTL = Duration.ofDays(7);

    /**
     * Shared secret used to derive the HS256 signing key. Override in production.
     */
    private String secret = "plate-dev-jwt-secret-change-me-0123456789";

    /**
     * Value of the {@code iss} (issuer) claim.
     */
    private String issuer = "plate";

    /**
     * Lifetime of access tokens.
     */
    private Duration accessTokenTtl = DEFAULT_ACCESS_TTL;

    /**
     * Lifetime of refresh tokens.
     */
    private Duration refreshTokenTtl = DEFAULT_REFRESH_TTL;

    public JwtProperties() {
    }

    /**
     * Builds validated properties for programmatic use (e.g. tests).
     *
     * @throws IllegalStateException when the secret is blank/too short, or the access TTL is
     *                               not strictly shorter than the refresh TTL
     */
    public JwtProperties(String secret, String issuer, Duration accessTtl, Duration refreshTtl) {
        if (secret == null || secret.isBlank()
                || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException("JWT secret must be at least " + MIN_SECRET_BYTES + " bytes");
        }
        Duration effectiveAccess = accessTtl == null ? DEFAULT_ACCESS_TTL : accessTtl;
        Duration effectiveRefresh = refreshTtl == null ? DEFAULT_REFRESH_TTL : refreshTtl;
        if (effectiveAccess.compareTo(effectiveRefresh) >= 0) {
            throw new IllegalStateException("Access token TTL must be shorter than refresh token TTL");
        }
        this.secret = secret;
        this.issuer = issuer == null || issuer.isBlank() ? "plate" : issuer;
        this.accessTokenTtl = effectiveAccess;
        this.refreshTokenTtl = effectiveRefresh;
    }
}
