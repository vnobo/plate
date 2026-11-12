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
 * Configures session management for web applications with support for custom session ID resolution.
 * <p>
 * The session is always resolved from the {@code SESSION} cookie first (the universal transport used by
 * browsers and HTTP clients on every request type, including AJAX). Only when no cookie is present does it
 * fall back to a query parameter or the {@code SESSION} response header, supporting non-browser / API clients.
 * This guarantees that AJAX requests carrying only the session cookie are correctly authenticated.
 */
@Configuration(proxyBeanMethods = false)
@EnableRedisIndexedWebSession
public class SessionConfiguration {


    /**
     * The name of the custom header used for session ID resolution.
     * This header is used to store and retrieve the session ID in AJAX requests.
     */
    public static final String SESSION_ID_NAME = "SESSION";

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
     * Configures and provides a custom strategy for resolving the session ID from web requests.
     * This resolver extends {@link HeaderWebSessionIdResolver} and conditionally switches
     * between header-based and cookie-based session ID resolution based on the presence of
     * specific request headers or from parameters.
     *
     * @return A configured instance of {@link WebSessionIdResolver} that is capable of
     * handling session ID resolution with custom logic to support both header and
     * cookie-based approaches depending on the nature of the incoming request.
     */
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new CustomWebSessionIdResolver();
        resolver.setHeaderName(SESSION_ID_NAME);
        return resolver;
    }

    /**
     * CustomWebSessionIdResolver is a specialized implementation of {@link HeaderWebSessionIdResolver} designed to handle
     * session ID resolution in web requests. It selectively employs either header-based or cookie-based strategies
     * contingent upon the characteristics of the incoming request, enhancing flexibility and compatibility with diverse
     * authentication mechanisms.
     * <p>
     * This resolver prioritizes the presence of certain request headers for session ID extraction but defaults to using
     * cookies if the request does not meet specific criteria. It also supports extracting access tokens from request
     * parameters, accommodating bearer token authentication schemes.
     * <p>
     * Key Features:
     * <ul>
     *   <li>Conditionally switches between header and cookie session ID resolution.</li>
     *   <li>Supports resolving session IDs from authorization headers and request parameters.</li>
     *   <li>Manages session ID setting, resolution, and expiration based on request context.</li>
     * </ul>
     */
    static class CustomWebSessionIdResolver extends HeaderWebSessionIdResolver {

        private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

        private List<String> resolveTokenFromRequestParams(ServerWebExchange exchange) {
            return exchange.getRequest().getQueryParams().getOrDefault(getHeaderName(), Collections.emptyList());
        }

        /**
         * Sets the session ID in the server web exchange.
         * The session is always persisted as a {@code SESSION} cookie so it can be reused on any transport
         * (browsers and HTTP clients send cookies automatically). It is additionally exposed as a response
         * header for non-browser / API clients that read it.
         *
         * @param exchange The server web exchange containing the request and response.
         * @param id       The session ID to be set.
         */
        @Override
        public void setSessionId(@NonNull ServerWebExchange exchange, @NonNull String id) {
            // Always persist as a cookie so the session survives any request type (including AJAX).
            cookieWebSessionIdResolver.setCookieName(getHeaderName());
            cookieWebSessionIdResolver.setSessionId(exchange, id);
            // Additionally expose it as a response header for non-browser / API clients.
            super.setSessionId(exchange, id);
        }

        /**
         * Resolves session IDs from the given server web exchange.
         * The {@code SESSION} cookie is always consulted first, because browsers and HTTP clients carry
         * the session exclusively via the cookie regardless of the request type (including AJAX). Only when
         * no cookie is present does it fall back to the query parameter / {@code SESSION} header used by
         * non-browser API clients.
         *
         * @param exchange The current server web exchange containing the request and response information.
         * @return A non-null list of session IDs associated with the given exchange.
         */
        @Override
        public @NonNull List<String> resolveSessionIds(@NonNull ServerWebExchange exchange) {
            // Cookie is the universal transport. Always try it first so that AJAX requests
            // (which typically carry only the SESSION cookie, not a SESSION header/param) authenticate.
            List<String> cookieIds = cookieWebSessionIdResolver.resolveSessionIds(exchange);
            if (!CollectionUtils.isEmpty(cookieIds)) {
                return cookieIds;
            }
            // Fallback for non-browser / API clients that pass the session via query param or header.
            if (isAjaxRequest(exchange)) {
                List<String> paramIds = resolveTokenFromRequestParams(exchange);
                if (!CollectionUtils.isEmpty(paramIds)) {
                    return paramIds;
                }
            }
            return super.resolveSessionIds(exchange);
        }

        /**
         * Expires the session associated with the given server web exchange.
         * <p>
         * This method utilizes the {@code cookieWebSessionIdResolver} to expire the session,
         * effectively invalidating any session data linked to the provided exchange.
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

        private Boolean isAjaxRequest(ServerWebExchange exchange) {
            return exchange.getRequest().getHeaders().containsHeaderValue(X_REQUESTED_WITH, XML_HTTP_REQUEST);
        }

    }
}