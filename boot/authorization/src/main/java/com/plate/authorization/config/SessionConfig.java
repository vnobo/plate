package com.plate.authorization.config;

import com.plate.authorization.commons.exception.RestServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
public class SessionConfig {

    public static final String HEADER_SESSION_ID_NAME = "X-Auth-Token";
    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";
    public static final Pattern AUTHORIZATION_PATTERN = Pattern.compile(
            "^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$", Pattern.CASE_INSENSITIVE);
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new CustomHttpSessionIdResolver(HEADER_SESSION_ID_NAME);
    }

    static class CustomHttpSessionIdResolver extends HeaderHttpSessionIdResolver {

        private final CookieHttpSessionIdResolver cookieWebSessionIdResolver = new CookieHttpSessionIdResolver();

        /**
         * The name of the header to obtain the session id from.
         *
         * @param headerName the name of the header to obtain the session id from.
         */
        public CustomHttpSessionIdResolver(String headerName) {
            super(headerName);
        }

        private static String resolveAccessTokenFromRequest(HttpServletRequest request) {
            String parameterTokens = request.getParameter("access_token");
            if (ObjectUtils.isEmpty(parameterTokens)) {
                return null;
            }
            if (StringUtils.hasLength(parameterTokens)) {
                return parameterTokens;
            }
            throw invalidTokenError("Found multiple bearer tokens in the request");
        }

        private static RestServerException invalidTokenError(String message) {
            return RestServerException.withMsg("Bearer token is malformed!", message);
        }

        @Override
        public void setSessionId(HttpServletRequest request, HttpServletResponse response, @NonNull String id) {
            if (StringUtils.hasLength(request.getHeader(X_REQUESTED_WITH))) {
                super.setSessionId(request, response, id);
            } else {
                cookieWebSessionIdResolver.setSessionId(request, response, id);
            }
        }

        @Override
        public List<String> resolveSessionIds(HttpServletRequest request) {
            List<String> requestedWith;
            if (XML_HTTP_REQUEST.equalsIgnoreCase(request.getHeader(X_REQUESTED_WITH))) {
                String token = token(request);
                if (StringUtils.hasLength(token)) {
                    requestedWith = List.of(token);
                } else {
                    requestedWith = super.resolveSessionIds(request);
                }
            } else {
                requestedWith = cookieWebSessionIdResolver.resolveSessionIds(request);
            }
            return requestedWith;
        }

        @Override
        public void expireSession(HttpServletRequest request, HttpServletResponse response) {
            super.expireSession(request, response);
            cookieWebSessionIdResolver.expireSession(request, response);
        }

        private String token(HttpServletRequest request) {
            String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
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

        private String resolveFromAuthorizationHeader(HttpServletRequest request) {
            String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;
            String authorization = request.getHeader(bearerTokenHeaderName);
            if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
                return null;
            }
            Matcher matcher = AUTHORIZATION_PATTERN.matcher(authorization);
            if (!matcher.matches()) {
                throw invalidTokenError("bearer is malformed.");
            }
            return matcher.group("token");
        }

        private boolean isParameterTokenSupportedForRequest(HttpServletRequest request) {
            return HttpMethod.GET.matches(request.getMethod());
        }
    }
}