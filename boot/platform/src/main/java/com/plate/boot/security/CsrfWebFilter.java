package com.plate.boot.security;

import com.plate.boot.commons.utils.ContextUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A WebFilter implementation that provides CSRF protection by ensuring a valid CSRF token is present in the request.
 * It integrates with the server's request processing pipeline to check for the presence of a CSRF token attribute.
 * If the token is found, it is propagated through the context for further use downstream.
 * This filter is designed to be executed early in the filter chain with a low precedence order.
 */
@Log4j2
@Order(Ordered.LOWEST_PRECEDENCE - 1)
@Component
public class CsrfWebFilter implements WebFilter {

    private static final String SIGN_HEADER = "X-Request-Sign";
    private static final PasswordEncoder passwordEncoder = ContextUtils.createDelegatingPasswordEncoder("SHA-256");

    /**
     * Filters the incoming server web exchange to ensure CSRF protection.
     * It checks for the presence of a CSRF token in the exchange attributes.
     * If the token is present, it propagates it through the context for downstream use.
     *
     * @param exchange The current server web exchange containing the request and response information.
     * @param chain    The next WebFilterChain to proceed with if the filtering condition is met.
     * @return A Mono that, when subscribed to, will execute the remainder of the filter chain.
     * If a CSRF token is found, the Mono will also ensure the token is available in the context.
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        exchange.getRequest().getQueryParams();
        log.debug("{}Csrf filter chain [CsrfWebFilter] next.", exchange.getLogPrefix());
        Mono<CsrfToken> csrfTokenMono = exchange.getAttribute(CsrfToken.class.getName());
        if (csrfTokenMono != null) {
            return csrfTokenMono.flatMap(csrfToken -> Mono.defer(() -> chain.filter(exchange))
                    .contextWrite((context) -> context.hasKey(ContextUtils.CSRF_TOKEN_CONTEXT) ? context
                            : context.put(ContextUtils.CSRF_TOKEN_CONTEXT, csrfToken)));
        }
        return chain.filter(exchange);
    }

}