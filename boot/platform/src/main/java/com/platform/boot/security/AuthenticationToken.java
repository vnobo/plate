package com.platform.boot.security;

import org.springframework.web.server.WebSession;

import java.io.Serializable;

/**
 * This class is used to create an AuthenticationToken object.
 * It takes a WebSession object and generates a token, expires and lastAccessTime.
 * Then, this can be used to implement customized authentication schemes.
 *
 * @param token          Token used for authentication
 * @param expires        Expiry time of the token in seconds
 * @param lastAccessTime Last access time of the token in epoch seconds
 * @author Alex bob
 */
public record AuthenticationToken(String token, Long expires, Long lastAccessTime) implements Serializable {

    public static AuthenticationToken of(String token, String expires, Long lastAccessTime) {
        return new AuthenticationToken(token, Long.parseLong(expires), lastAccessTime);
    }

    /**
     * Builds an authentication token from a web session
     *
     * @param session The web session
     * @return The authentication token
     */
    public static AuthenticationToken build(WebSession session) {
        return new AuthenticationToken(session.getId(), session.getMaxIdleTime().getSeconds(),
                session.getLastAccessTime().getEpochSecond());
    }
}