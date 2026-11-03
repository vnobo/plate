package com.plate.boot.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the JWT authentication subsystem.
 * <p>
 * Bound from the {@code plate.security.jwt} prefix. The {@link #secret} is used to derive a
 * 256-bit HMAC key (via SHA-256) so any configured string length is accepted; production
 * deployments MUST override the default value. TTLs control the lifetime of access and
 * refresh tokens respectively.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@ConfigurationProperties(prefix = "plate.security.jwt")
public class JwtProperties {

    /**
     * Shared secret used to derive the HS256 signing key. Override in production.
     */
    private String secret = "plate-dev-jwt-secret-change-me";

    /**
     * Value of the {@code iss} (issuer) claim.
     */
    private String issuer = "plate";

    /**
     * Lifetime of access tokens.
     */
    private Duration accessTokenTtl = Duration.ofHours(2);

    /**
     * Lifetime of refresh tokens.
     */
    private Duration refreshTokenTtl = Duration.ofDays(7);
}
