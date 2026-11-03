package com.plate.boot.security.jwt;

import com.plate.boot.config.SessionConfiguration;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ServerSecurityContextRepository} that resolves the authentication for a request
 * from either a stateless JWT bearer token or the traditional web session.
 * <p>
 * Requests carrying an {@code Authorization: Bearer <jwt>} header in JWS compact form are
 * authenticated statelessly from the token claims and the authoritative user record; no
 * session is created or consulted. Every other request (cookie sessions, the legacy
 * session-id bearer transport used by XHR clients) is delegated unchanged to
 * {@link WebSessionServerSecurityContextRepository}, keeping existing behavior intact.
 * </p>
 * <p>
 * Invalid or expired JWTs resolve to an empty (anonymous) context so that the standard
 * authorization filter and authentication entry point produce a uniform 401 response.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    /**
     * JWS compact serialization shape: three base64url segments separated by dots.
     */
    private static final Pattern JWS_COMPACT_PATTERN =
            Pattern.compile("^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$");

    private final JwtService jwtService;
    private final SecurityManager securityManager;

    private final ServerSecurityContextRepository sessionDelegate = new WebSessionServerSecurityContextRepository();

    /**
     * Resolves the security context for the request, preferring JWT bearer authentication
     * and falling back to the session-based repository.
     *
     * @param exchange the current server exchange
     * @return a {@link Mono} emitting the resolved {@link SecurityContext}, or empty for anonymous requests
     */
    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String token = resolveBearerToken(exchange);
        if (isJwtToken(token)) {
            return this.loadFromJwt(token);
        }
        return this.sessionDelegate.load(exchange);
    }

    /**
     * Persists the security context only for session-based requests; JWT-authenticated
     * requests stay fully stateless.
     *
     * @param exchange the current server exchange
     * @param context  the security context to save
     * @return a {@link Mono} completing when the operation is done
     */
    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        String token = resolveBearerToken(exchange);
        if (isJwtToken(token)) {
            return Mono.empty();
        }
        return this.sessionDelegate.save(exchange, context);
    }

    /**
     * Builds the security context from a validated access token and the user's
     * authoritative record. Failures resolve to an anonymous (empty) context.
     */
    private Mono<SecurityContext> loadFromJwt(String token) {
        return this.jwtService.parse(token)
                .filter(payload -> TokenType.ACCESS == payload.tokenType())
                .flatMap(payload -> this.securityManager.findByUsername(payload.username()))
                .ofType(SecurityDetails.class)
                .map(details -> {
                    var authentication = UsernamePasswordAuthenticationToken
                            .authenticated(details, details.getPassword(), details.getAuthorities());
                    return (SecurityContext) new SecurityContextImpl(authentication);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("JWT bearer token did not resolve to a user, continuing as anonymous");
                    return Mono.empty();
                }))
                .onErrorResume(e -> {
                    log.debug("JWT bearer authentication failed, continuing as anonymous: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Extracts the bearer token from the {@code Authorization} header using the same
     * pattern as the session transport.
     *
     * @param exchange the current server exchange
     * @return the raw token value, or {@code null} when absent
     */
    public static String resolveBearerToken(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null) {
            return null;
        }
        Matcher matcher = SessionConfiguration.AUTHORIZATION_PATTERN.matcher(header);
        return matcher.matches() ? matcher.group("token") : null;
    }

    /**
     * Determines whether the given token is a JWS compact JWT (as opposed to a session id).
     *
     * @param token the raw token value, may be {@code null}
     * @return {@code true} when the token has the three-segment JWS compact shape
     */
    public static boolean isJwtToken(String token) {
        return token != null && JWS_COMPACT_PATTERN.matcher(token).matches();
    }
}
