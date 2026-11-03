package com.plate.boot.security.jwt;

import java.time.Instant;
import java.util.List;

/**
 * Immutable, validated view over the claims of a parsed JWT.
 *
 * @param username    the {@code sub} claim — the authenticated user's login name.
 * @param code        the {@code code} claim — the user's unique business code.
 * @param clientType  the {@code cty} claim — the client platform the token was issued to.
 * @param tokenType   the {@code typ} claim — {@link TokenType#ACCESS} or {@link TokenType#REFRESH}.
 * @param authorities the {@code authorities} claim — granted authority names at issue time.
 * @param jti         the {@code jti} claim — unique token identifier.
 * @param issuedAt    the {@code iat} claim — issue timestamp.
 * @param expiresAt   the {@code exp} claim — expiration timestamp.
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public record JwtPayload(String username, String code, ClientType clientType, TokenType tokenType,
                         List<String> authorities, String jti, Instant issuedAt, Instant expiresAt) {
}
