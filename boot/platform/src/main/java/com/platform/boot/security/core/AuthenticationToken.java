package com.platform.boot.security.core;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.WebSession;

import java.io.Serializable;

/**
 * Authentication token
 * @author <a href="https://github.com/vnobo">Alex bob</a>
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