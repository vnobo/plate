package com.platform.boot.security;

import lombok.Data;
import org.springframework.web.server.WebSession;

import java.io.Serializable;

/**
 * This class is used to create an AuthenticationToken object.
 * It takes a WebSession object and generates a token, expires and lastAccessTime.
 * Then, this can be used to implement customized authentication schemes.
 *
 * @author Alex bob
 */
@Data
public class AuthenticationToken implements Serializable {
    // Token used for authentication
    private final String token;
    // Expiry time of the token in seconds
    private final Long expires;
    // Last access time of the token in epoch seconds
    private final Long lastAccessTime;

    /**
     * Constructor for the authentication token
     *
     * @param token          The token used for authentication
     * @param expires        The expiry time of the token in seconds
     * @param lastAccessTime The last access time of the token in epoch seconds
     */
    public AuthenticationToken(String token, Long expires, Long lastAccessTime) {
        this.token = token;
        this.expires = expires;
        this.lastAccessTime = lastAccessTime;
    }

    /**
     * Builds an authentication token from a web session
     *
     * @param session The web session
     * @return The authentication token
     */
    public static AuthenticationToken build(WebSession session) {
        return new AuthenticationToken(session.getId(),
                session.getMaxIdleTime().getSeconds(),
                session.getLastAccessTime().getEpochSecond());
    }
}