package com.plate.boot.config;

import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.List;

/**
 * Configures session management for web applications with support for custom session ID resolution,
 * particularly tailored for handling both AJAX requests and cases where a Bearer token might be
 * present either in the request header or from parameters.
 * <p>
 * This configuration class enhances the session handling by introducing a custom strategy to read
 * session IDs from a custom header ({@code X-Auth-Token}) for AJAX requests or falling back to
 * cookies for non-AJAX requests. It also supports extracting Bearer tokens from the Authorization
 * header or access_token from parameter, ensuring secure and flexible session management.
 */
@Configuration(proxyBeanMethods = false)
@EnableRedisIndexedWebSession
public class SessionConfiguration {

    /**
     * The name of the custom header used for session ID resolution.
     * This header is used to store and retrieve the session ID in AJAX requests.
     */
    public static final String HEADER_SESSION_ID_NAME = "X-Auth-Token";

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
        resolver.setHeaderName(HEADER_SESSION_ID_NAME);
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
        /**
         * Resolver for session IDs using cookies.
         * This resolver is used as a fallback when the request does not contain specific headers.
         */
        private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

        /**
         * Sets the session ID in the server web exchange.
         * This method determines whether to use a header-based or cookie-based approach for setting the session ID
         * based on the presence of specific headers in the request.
         *
         * @param exchange The server web exchange containing the request and response.
         * @param id       The session ID to be set.
         */
        @Override
        public void setSessionId(@NonNull ServerWebExchange exchange, @NonNull String id) {
            if (exchange.getRequest().getHeaders().containsHeader(X_REQUESTED_WITH)) {
                super.setSessionId(exchange, id);
            } else {
                cookieWebSessionIdResolver.setSessionId(exchange, id);
            }
        }

        /**
         * Resolves session IDs from the given server web exchange based on the request's characteristics.
         * If the request is identified as an XMLHttpRequest, it attempts to extract a token from the request.
         * If the token is present, it returns a list containing that single token; otherwise, it delegates
         * to the superclass implementation for session ID resolution. For non-XMLHttpRequests, it uses
         * a cookie-based resolver to obtain the session IDs.
         *
         * @param exchange The current server web exchange containing the request and response information.
         * @return A non-null list of session IDs associated with the given exchange.
         */
        @Override
        public @NonNull List<String> resolveSessionIds(@NonNull ServerWebExchange exchange) {
            List<String> requestedWith;
            HttpHeaders httpHeaders = exchange.getRequest().getHeaders();
            if (XML_HTTP_REQUEST.equalsIgnoreCase(httpHeaders.getFirst(X_REQUESTED_WITH))) {
                requestedWith = super.resolveSessionIds(exchange);
            } else {
                requestedWith = cookieWebSessionIdResolver.resolveSessionIds(exchange);
            }
            return requestedWith;
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
            cookieWebSessionIdResolver.expireSession(exchange);
        }

    }
}