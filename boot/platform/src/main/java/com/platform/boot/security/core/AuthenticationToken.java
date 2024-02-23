package com.platform.boot.security.core;

import org.springframework.security.core.Authentication;
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
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
public record AuthenticationToken(String token, Long expires, Long lastAccessTime,
                                  Object details) implements Serializable {

    public static AuthenticationToken of(String token, Long expires, Long lastAccessTime, Object details) {
        return new AuthenticationToken(token, expires, lastAccessTime, details);
    }

    public static AuthenticationToken build(WebSession session, Authentication authentication) {
        return of(session.getId(), session.getMaxIdleTime().getSeconds(),
                session.getLastAccessTime().getEpochSecond(), authentication.getPrincipal());
    }
}