package com.platform.boot.config;

import com.platform.boot.commons.utils.ContextUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.List;

/**
 * This configuration class is used to configure the session for the application. It is using
 * HeaderWebSessionIdResolver and CookieWebSessionIdResolver to resolve the session id. The setHeaderName
 * method of HeaderWebSessionIdResolver is used to set the name of the header which contains the session
 * id while resolving. It is also overriding setSessionId, resolveSessionIds and expireSession methods
 * of its parent class to add custom functionality.
 * Configuration class for session management.
 *
 * @author billb
 */
@Configuration(proxyBeanMethods = false)
public class SessionConfiguration {

    /**
     * Bean for WebSessionIdResolver.
     *
     * @return HeaderWebSessionIdResolver
     */
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        HeaderWebSessionIdResolver resolver = new CustomHeaderWebSessionIdResolver();
        resolver.setHeaderName(ContextUtils.SECURITY_AUTH_TOKEN_HEADER);
        return resolver;
    }

    /**
     * CustomHeaderWebSessionIdResolver class.
     */
    static class CustomHeaderWebSessionIdResolver extends HeaderWebSessionIdResolver {
        private final CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();

        @Override
        public void setSessionId(@NonNull ServerWebExchange exchange, @NonNull String id) {
            super.setSessionId(exchange, id);
            cookieWebSessionIdResolver.setSessionId(exchange, id);
        }

        @NonNull
        @Override
        public List<String> resolveSessionIds(@NonNull ServerWebExchange exchange) {
            List<String> requestedWith = super.resolveSessionIds(exchange);
            if (ObjectUtils.isEmpty(requestedWith)) {
                requestedWith = cookieWebSessionIdResolver.resolveSessionIds(exchange);
            }
            return requestedWith;
        }

        @Override
        public void expireSession(@NonNull ServerWebExchange exchange) {
            super.expireSession(exchange);
            cookieWebSessionIdResolver.expireSession(exchange);
        }
    }
}