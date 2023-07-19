package com.platform.boot.security.filter;

import com.platform.boot.commons.utils.ContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Component
public record CsrfWebFilter() implements WebFilter, Ordered {
    private static final Log log = LogFactory.getLog(CsrfWebFilter.class);

    @Override
    @NonNull
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        log.debug("Csrf filter next log id %s".formatted(exchange.getLogPrefix()));
        Mono<CsrfToken> csrfTokenMono = exchange.getAttribute(CsrfToken.class.getName());
        if (csrfTokenMono != null) {
            return csrfTokenMono.flatMap(csrfToken -> Mono.defer(() -> chain.filter(exchange))
                    .contextWrite((context) -> context.hasKey(ContextUtils.CSRF_TOKEN_CONTEXT) ?
                            context : context.put(ContextUtils.CSRF_TOKEN_CONTEXT, csrfToken)));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}