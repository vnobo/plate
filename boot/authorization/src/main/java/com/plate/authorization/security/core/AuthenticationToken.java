package com.plate.authorization.security.core;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Authentication token
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record AuthenticationToken(String token, Integer expires, Long lastAccessTime,
                                  Object details) implements Serializable {

    public static AuthenticationToken of(String token, Integer expires, Long lastAccessTime, Object details) {
        return new AuthenticationToken(token, expires, lastAccessTime, details);
    }

    public static AuthenticationToken build(HttpSession session, Authentication authentication) {
        return of(session.getId(), session.getMaxInactiveInterval(),
                session.getLastAccessedTime(), authentication.getPrincipal());
    }
}