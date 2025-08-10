package com.plate.boot.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.DefaultCsrfToken;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

/**
 * CsrfWebFilter Unit Tests
 *
 * <p>This test class provides unit tests for the CsrfWebFilter class, covering:</p>
 * <ul>
 *   <li>CSRF token filtering when token is present</li>
 *   <li>CSRF token filtering when token is absent</li>
 * </ul>
 *
 * @author Qwen Code
 */
class CsrfWebFilterTest {

    private CsrfWebFilter csrfWebFilter;

    @BeforeEach
    void setUp() {
        csrfWebFilter = new CsrfWebFilter();
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should filter with CSRF token present")
        void shouldFilterWithCsrfTokenPresent() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            CsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token");
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(csrfToken));
            
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            // When
            Mono<Void> result = csrfWebFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                .verifyComplete();
                
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("Should filter without CSRF token")
        void shouldFilterWithoutCsrfToken() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            // When
            Mono<Void> result = csrfWebFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                .verifyComplete();
                
            verify(chain).filter(exchange);
        }
    }
}