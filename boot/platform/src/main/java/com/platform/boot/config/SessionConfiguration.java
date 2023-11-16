package com.platform.boot.config;

import com.platform.boot.commons.annotation.exception.RestServerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
public class SessionConfiguration {
    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService oAuth2ClientService(DatabaseClient databaseClient,
                                                                     ReactiveClientRegistrationRepository clientRepository) {
        return new R2dbcReactiveOAuth2AuthorizedClientService(databaseClient, clientRepository);
    }
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        return new CustomBearerWebSessionIdResolver();
    }

    static class CustomBearerWebSessionIdResolver implements WebSessionIdResolver {
        private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
                Pattern.CASE_INSENSITIVE);

        private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

        @Override
        public void setSessionId(@NonNull ServerWebExchange exchange, @NonNull String id) {
            cookieWebSessionIdResolver.setSessionId(exchange, id);
        }

        @NonNull
        @Override
        public List<String> resolveSessionIds(@NonNull ServerWebExchange exchange) {
            List<String> requestedWith = List.of();
            if ("XMLHttpRequest".equalsIgnoreCase(exchange.getRequest().getHeaders().getFirst("X-Requested-With"))) {
                String token = token(exchange.getRequest());
                if (StringUtils.hasLength(token)) {
                    requestedWith = List.of(token);
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

        private static String resolveAccessTokenFromRequest(ServerHttpRequest request) {
            List<String> parameterTokens = request.getQueryParams().get("access_token");
            if (CollectionUtils.isEmpty(parameterTokens)) {
                return null;
            }
            if (parameterTokens.size() == 1) {
                return parameterTokens.get(0);
            }
            throw invalidTokenError("Found multiple bearer tokens in the request");
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

        private static RestServerException invalidTokenError(String message) {
            return RestServerException.withMsg("Bearer token is malformed!", message);
        }
    }
}