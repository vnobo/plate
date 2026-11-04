package com.plate.boot.security.jwt;

/**
 * Enumeration of the JWT token kinds issued by {@link JwtService}.
 * <p>
 * Every issued JWT carries a {@code typ} claim holding one of these values. The claim
 * prevents cross-use of tokens: an access token cannot be presented at the refresh
 * endpoint and a refresh token cannot be used as a request credential.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public enum TokenType {

    /**
     * Short-lived bearer credential authorizing API requests.
     */
    ACCESS,

    /**
     * Long-lived credential used only at the {@code /oauth2/refresh} endpoint to obtain new token pairs.
     */
    REFRESH
}
