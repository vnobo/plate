package com.plate.boot.security.core;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.WebSession;

import java.io.Serializable;

/**
 * Represents an immutable data structure for holding authentication token details, including the token string, expiration timestamp,
 * last access time, and additional metadata. This record is designed to be serializable for storage or transmission.
 * <p>
 * It provides static factory methods to facilitate the creation of {@code AuthenticationToken} instances from individual components
 * or by extracting information from a {@code WebSession} and an {@code Authentication} object.
 *
 * @param token          The authentication token string.
 * @param expires        The timestamp indicating when the token expires, in seconds since the Unix epoch.
 * @param lastAccessTime The timestamp of the last access made using this token, in seconds since the Unix epoch.
 * @param details        Arbitrary object containing additional details associated with the authentication, typically the principal.
 */
public record AuthenticationToken(String token, Long expires, Long lastAccessTime,
                                  Object details) implements Serializable {

    /**
     * Creates a new instance of {@link AuthenticationToken} with the provided parameters.
     *
     * @param token          The authentication token string.
     * @param expires        The timestamp in seconds since the Unix epoch when the token expires.
     * @param lastAccessTime The timestamp in seconds since the Unix epoch of the last access made using this token.
     * @param details        An arbitrary object containing additional details associated with the authentication.
     * @return A new {@link AuthenticationToken} instance initialized with the given arguments.
     */
    public static AuthenticationToken of(String token, Long expires, Long lastAccessTime, Object details) {
        return new AuthenticationToken(token, expires, lastAccessTime, details);
    }

    /**
     * Constructs an {@link AuthenticationToken} instance using details extracted from the provided {@link WebSession} and
     * {@link Authentication} objects.
     *
     * @param session   The current web session from which the session ID and timestamps are retrieved.
     * @param principal The authentication object containing the principal detail.
     * @return A newly built {@link AuthenticationToken} representing the authenticated user's session with relevant metadata.
     */
    public static AuthenticationToken build(WebSession session, Object principal) {
        return of(session.getId(), session.getMaxIdleTime().getSeconds(),
                session.getLastAccessTime().getEpochSecond(), principal);
    }
}