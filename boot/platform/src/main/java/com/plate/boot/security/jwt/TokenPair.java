package com.plate.boot.security.jwt;

/**
 * Response body of the JWT login and refresh endpoints, carrying the issued credential pair.
 *
 * @param accessToken  short-lived JWT authorizing API requests as {@code Authorization: Bearer <accessToken>}.
 * @param refreshToken long-lived JWT accepted only by the {@code /oauth2/refresh} endpoint.
 * @param tokenType    the token type understood by API clients, always {@code Bearer}.
 * @param expiresIn    access token lifetime in seconds.
 * @param clientType   the client platform the pair was issued for.
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public record TokenPair(String accessToken, String refreshToken, String tokenType,
                        long expiresIn, ClientType clientType) {
}
