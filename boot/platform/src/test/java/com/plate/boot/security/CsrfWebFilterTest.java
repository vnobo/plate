package com.plate.boot.security;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.Test;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.ContextView;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link CsrfWebFilter}.
 */
class CsrfWebFilterTest {

    private final CsrfWebFilter filter = new CsrfWebFilter();

    @Test
    void filterContinuesWithoutContextWhenNoCsrfTokenAttribute() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getAttribute(CsrfToken.class.getName())).thenReturn(null);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void filterWritesCsrfTokenToContextWhenTokenPresent() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        CsrfToken csrfToken = mock(CsrfToken.class);
        when(exchange.getAttribute(CsrfToken.class.getName())).thenReturn(Mono.just(csrfToken));

        AtomicReference<ContextView> captured = new AtomicReference<>();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.deferContextual(context -> {
            captured.set(context);
            return Mono.empty();
        }));

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(exchange);
        CsrfToken actual = captured.get().get(ContextUtils.CSRF_TOKEN_CONTEXT);
        assertThat(actual).isSameAs(csrfToken);
    }

    @Test
    void filterDoesNotOverwriteExistingContextToken() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        CsrfToken csrfToken = mock(CsrfToken.class);
        CsrfToken existingToken = mock(CsrfToken.class);
        when(exchange.getAttribute(CsrfToken.class.getName())).thenReturn(Mono.just(csrfToken));

        AtomicReference<ContextView> captured = new AtomicReference<>();
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(exchange)).thenReturn(Mono.deferContextual(context -> {
            captured.set(context);
            return Mono.empty();
        }));

        StepVerifier.create(filter.filter(exchange, chain)
                        .contextWrite(context -> context.put(ContextUtils.CSRF_TOKEN_CONTEXT, existingToken)))
                .verifyComplete();

        CsrfToken actual = captured.get().get(ContextUtils.CSRF_TOKEN_CONTEXT);
        assertThat(actual).isSameAs(existingToken);
    }

    @Test
    void filterSkipsChainWhenCsrfTokenMonoIsEmpty() {
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getAttribute(CsrfToken.class.getName())).thenReturn(Mono.empty());
        WebFilterChain chain = mock(WebFilterChain.class);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain, never()).filter(any());
    }
}
