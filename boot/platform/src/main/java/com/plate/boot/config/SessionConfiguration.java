package com.plate.boot.config;

import com.plate.boot.commons.exception.RestServerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisIndexedWebSession;
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
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableRedisIndexedWebSession
public class SessionConfiguration{

    public static final String HEADER_SESSION_ID_NAME = "X-Auth-Token";
    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
    public static final Pattern AUTHORIZATION_PATTERN = Pattern.compile(
            "^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new CustomWebSessionIdResolver();
        resolver.setHeaderName(HEADER_SESSION_ID_NAME);
        return resolver;
    }

    static class CustomWebSessionIdResolver extends HeaderWebSessionIdResolver {

        private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

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

        private static RestServerException invalidTokenError(String message) {
            return RestServerException.withMsg("Bearer token is malformed!", message);
        }

        @Override
        public void setSessionId(@NonNull ServerWebExchange exchange, @NonNull String id) {
            if (exchange.getRequest().getHeaders().containsKey(X_REQUESTED_WITH)) {
                super.setSessionId(exchange, id);
            } else {
                cookieWebSessionIdResolver.setSessionId(exchange, id);
            }
        }

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

        @Override
        public void expireSession(@NonNull ServerWebExchange exchange) {
            cookieWebSessionIdResolver.expireSession(exchange);
        }

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

        private String resolveFromAuthorizationHeader(HttpHeaders headers) {
            String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;
            String authorization = headers.getFirst(bearerTokenHeaderName);
            if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
                return null;
            }
            Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization);
            if (!matcher.matches()) {
                throw invalidTokenError("bearer is malformed.");
            }
            return matcher.group("token");
        }

        private boolean isParameterTokenSupportedForRequest(ServerHttpRequest request) {
            return HttpMethod.GET.equals(request.getMethod());
        }
    }
}