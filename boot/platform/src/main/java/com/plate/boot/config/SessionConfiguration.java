package com.plate.boot.config;

import com.nimbusds.oauth2.sdk.dpop.verifiers.AccessTokenValidationException;
import com.plate.boot.commons.exception.RestServerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession;
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * A regular expression pattern for extracting Bearer tokens from the Authorization header.
     * This pattern matches the Bearer token format and extracts the token value.
     */
    public static final Pattern AUTHORIZATION_PATTERN = Pattern.compile(
            "^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);

    /**
     * Creates and configures a SpringSessionBackedReactiveSessionRegistry bean.
     * This registry is designed to manage sessions within a reactive environment, backed by the provided
     * ReactiveSessionRepository and ReactiveFindByIndexNameSessionRepository instances.
     *
     * @param <S>                      The type of session extending the Session interface.
     * @param sessionRepository        A reactive session repository for storing and retrieving session data.
     * @param indexedSessionRepository A reactive session repository capable of finding sessions by index name,
     *                                 enhancing session management capabilities.
     * @return An instance of SpringSessionBackedReactiveSessionRegistry configured with the given repositories,
     * ready to manage and provide session-related services in a reactive context.
     */
    @Bean
    public <S extends Session> SpringSessionBackedReactiveSessionRegistry<S> sessionRegistry(
            ReactiveSessionRepository<S> sessionRepository,
            ReactiveFindByIndexNameSessionRepository<S> indexedSessionRepository) {
        return new SpringSessionBackedReactiveSessionRegistry<>(sessionRepository, indexedSessionRepository);
    }

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
         * Resolves the access token from the request parameters.
         * This method checks for the presence of an "access_token" parameter in the request query parameters.
         * If a single token is found, it is returned; if multiple tokens are found, an exception is thrown.
         *
         * @param request The server HTTP request from which to extract the access token.
         * @return The access token as a String, or null if no token is found.
         * @throws RestServerException if multiple tokens are found in the request.
         */
        private static String resolveAccessTokenFromRequest(ServerHttpRequest request) {
            List<String> parameterTokens = request.getQueryParams().get("access_token");
            if (CollectionUtils.isEmpty(parameterTokens)) {
                return null;
            }
            if (parameterTokens.size() == 1) {
                return parameterTokens.getFirst();
            }
            throw invalidTokenError("Found multiple bearer tokens in the request");
        }

        /**
         * Creates a RestServerException for invalid tokens.
         * This method constructs an exception with a specific message and a nested AccessTokenValidationException.
         *
         * @param message The error message to be included in the exception.
         * @return A RestServerException with the provided message and a nested AccessTokenValidationException.
         */
        private static RestServerException invalidTokenError(String message) {
            return RestServerException.withMsg(message, new AccessTokenValidationException("Bearer token is malformed"));
        }

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
            if (exchange.getRequest().getHeaders().containsKey(X_REQUESTED_WITH)) {
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
                String token = token(exchange.getRequest());
                if (StringUtils.hasLength(token)) {
                    requestedWith = List.of(token);
                } else {
                    requestedWith = super.resolveSessionIds(exchange);
                }
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

        /**
         * Retrieves the authentication token from the given server HTTP request.
         * It checks for the token in the request's Authorization header and,
         * if not found, checks for a parameter token which is supported for GET requests.
         * If both are present, it throws an exception indicating multiple tokens.
         * If neither is found, it returns null.
         *
         * @param request The server HTTP request from which to extract the token.
         * @return The extracted token as a String, or null if no valid token is found.
         * @throws RestServerException if multiple token instances are found in the request.
         */
        private String token(ServerHttpRequest request) {
            String authorizationHeaderToken = resolveFromAuthorizationHeader(request.getHeaders());
            String parameterToken = resolveAccessTokenFromRequest(request);

            if (authorizationHeaderToken != null) {
                if (parameterToken != null) {
                    throw invalidTokenError("Found multiple bearer tokens in the request.");
                }
                return authorizationHeaderToken;
            }
            if (parameterToken != null && isParameterTokenSupportedForRequest(request)) {
                return parameterToken;
            }
            return null;
        }

        /**
         * Extracts the bearer token from the 'Authorization' header within the provided HTTP headers.
         * If the header starts with 'Bearer', the method attempts to parse the token following this keyword.
         * In case of a malformed bearer token format, an exception is thrown.
         *
         * @param headers The HTTP headers to inspect for the bearer token.
         * @return The extracted bearer token as a String if present and correctly formatted, null otherwise.
         * @throws RestServerException if the bearer token is malformed.
         */
        private String resolveFromAuthorizationHeader(HttpHeaders headers) {
            String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;
            String authorization = headers.getFirst(bearerTokenHeaderName);
            if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
                return null;
            }
            Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization);
            if (!matcher.matches()) {
                throw invalidTokenError("Extracts the bearer token from the" +
                        " 'Authorization' header within the provided HTTP headers.");
            }
            return matcher.group("token");
        }

        /**
         * Determines whether the given server HTTP request supports a parameter token based on its HTTP method.
         * Specifically, this method checks if the request method is HTTP GET, as parameter tokens are typically
         * supported in GET requests for fetching resources.
         *
         * @param request The server HTTP request to inspect for supported token parameters.
         * @return {@code true} if the request method is HTTP GET, indicating support for parameter tokens; otherwise, {@code false}.
         */
        private boolean isParameterTokenSupportedForRequest(ServerHttpRequest request) {
            return HttpMethod.GET.equals(request.getMethod());
        }
    }
}