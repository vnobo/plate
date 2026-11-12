package com.plate.boot.config;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.Collections;
import java.util.List;

/**
 * Configures Redis-backed web session management with a dual-transport session ID resolver.
 * <p>
 * The transport is selected per request from the {@code X-Requested-With} header:
 * <ul>
 *   <li>AJAX requests ({@code X-Requested-With: XMLHttpRequest}) carry the session ID in the
 *       {@code X-Auth-Token} request header, falling back to a query parameter of the same name.
 *       The ID is written back through the {@code X-Auth-Token} response header.</li>
 *   <li>All other requests carry the session ID in the default {@code SESSION} cookie, which is
 *       also where it is written when the session is created.</li>
 * </ul>
 * The two transports are mutually exclusive per request: an AJAX request never consults the
 * cookie, and a non-AJAX request never consults the header or the query parameter. AJAX clients
 * are therefore expected to read the ID from the {@code X-Auth-Token} response header and send it
 * back on subsequent requests.
 */
@Configuration(proxyBeanMethods = false)
@EnableRedisIndexedWebSession
public class SessionConfiguration {


    /**
     * Name of the header, response header, and query parameter used to carry the session ID on
     * AJAX requests. It is configured as the header name of {@link HeaderWebSessionIdResolver},
     * so {@code super} reads it from request headers and writes it to response headers.
     */
    public static final String SESSION_KEY_NAME = "X-Auth-Token";

    /**
     * The name of the header used to identify AJAX requests.
     * This header is typically set to "XMLHttpRequest" for AJAX requests.
     */
    public static final String X_REQUESTED_WITH = "X-Requested-With";

    /**
     * The value of the X-Requested-With header for AJAX requests.
     * This value is used to identify requests made via XMLHttpRequest.
     */
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    /**
     * Provides the session ID resolver used by Spring Session, with {@code X-Auth-Token}
     * configured as its header name.
     *
     * @return a {@link WebSessionIdResolver} that uses the {@code X-Auth-Token} header or query
     * parameter for AJAX requests and the default {@code SESSION} cookie for all other requests
     */
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new CustomWebSessionIdResolver();
        resolver.setHeaderName(SESSION_KEY_NAME);
        return resolver;
    }

    /**
     * A {@link HeaderWebSessionIdResolver} that selects one of two session ID transports per
     * request, based on the {@code X-Requested-With} header.
     * <p>
     * For AJAX requests the session ID is read from the {@code X-Auth-Token} request header (or a
     * query parameter of the same name when the header is absent) and written to the
     * {@code X-Auth-Token} response header. For every other request the session ID is read from
     * and written to the default {@code SESSION} cookie via {@link CookieWebSessionIdResolver}.
     * Expiration clears both the header-based and the cookie-based state.
     */
    static class CustomWebSessionIdResolver extends HeaderWebSessionIdResolver {

        private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

        /**
         * Reads session IDs from the query parameter named after the configured header
         * ({@code X-Auth-Token}). Used as the AJAX fallback when the request header is absent.
         *
         * @param exchange The current server web exchange.
         * @return The matching query parameter values, or an empty list if none are present.
         */
        private List<String> resolveTokenFromRequestParams(ServerWebExchange exchange) {
            return exchange.getRequest().getQueryParams().getOrDefault(getHeaderName(), Collections.emptyList());
        }

        /**
         * Writes the session ID using the transport selected for the current request: the
         * {@code X-Auth-Token} response header for AJAX requests, or the default {@code SESSION}
         * cookie otherwise. Only one transport is written per request.
         *
         * @param exchange The server web exchange containing the request and response.
         * @param id       The session ID to be set.
         */
        @Override
        public void setSessionId(@NonNull ServerWebExchange exchange, @NonNull String id) {
            if (isAjaxRequest(exchange)) {
                super.setSessionId(exchange, id);
            } else {
                cookieWebSessionIdResolver.setSessionId(exchange, id);
            }
        }

        /**
         * Resolves session IDs using the transport selected for the current request. For AJAX
         * requests the {@code X-Auth-Token} request header is read first, falling back to a query
         * parameter of the same name when the header is absent. For all other requests the default
         * {@code SESSION} cookie is used. AJAX requests do not consult the cookie.
         *
         * @param exchange The current server web exchange containing the request and response information.
         * @return A non-null list of session IDs associated with the given exchange.
         */
        @Override
        public @NonNull List<String> resolveSessionIds(@NonNull ServerWebExchange exchange) {
            if (isAjaxRequest(exchange)) {
                List<String> headerTokenIds = super.resolveSessionIds(exchange);
                if (!CollectionUtils.isEmpty(headerTokenIds)) {
                    return headerTokenIds;
                }
                return resolveTokenFromRequestParams(exchange);
            }
            return cookieWebSessionIdResolver.resolveSessionIds(exchange);
        }

        /**
         * Expires the session ID on both transports by delegating to the superclass
         * ({@code X-Auth-Token} response header) and then to the cookie resolver
         * ({@code SESSION} cookie).
         *
         * @param exchange The server web exchange representing the current HTTP interaction,
         *                 from which the session will be expired.
         * @throws NullPointerException if the provided server web exchange is null.
         */
        @Override
        public void expireSession(@NonNull ServerWebExchange exchange) {
            super.expireSession(exchange);
            cookieWebSessionIdResolver.expireSession(exchange);
        }

        /**
         * Determines whether the request is treated as AJAX by matching the
         * {@code X-Requested-With} header against the exact value {@code XMLHttpRequest}.
         * Any other value (or a missing header) selects the cookie transport.
         *
         * @param exchange The current server web exchange.
         * @return {@code true} if the request is an AJAX request.
         */
        private Boolean isAjaxRequest(ServerWebExchange exchange) {
            return exchange.getRequest().getHeaders().containsHeaderValue(X_REQUESTED_WITH, XML_HTTP_REQUEST);
        }

    }
}