package com.plate.boot.security.captcha;

import com.plate.boot.security.captcha.CaptchaFilter.CaptchaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * CaptchaFilter Unit Tests
 *
 * <p>This test class provides unit tests for the CaptchaFilter class, covering:</p>
 * <ul>
 *   <li>Captcha validation for protected endpoints</li>
 *   <li>Access denied handling for invalid captchas</li>
 *   <li>Filter chain continuation for valid requests</li>
 * </ul>
 *
 * @author Qwen Code
 */
class CaptchaFilterTest {

    private CaptchaFilter captchaFilter;
    private CaptchaRepository captchaRepository;

    @BeforeEach
    void setUp() {
        captchaRepository = mock(CaptchaRepository.class);
        captchaFilter = new CaptchaFilter(captchaRepository);
    }

    @Nested
    @DisplayName("Filter Order Tests")
    class FilterOrderTests {

        @Test
        @DisplayName("Should have highest precedence order")
        void shouldHaveHighestPrecedenceOrder() {
            // When
            int order = captchaFilter.getOrder();

            // Then
            assertThat(order).isEqualTo(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        }
    }

    @Nested
    @DisplayName("Captcha Validation Tests")
    class CaptchaValidationTests {

        @Test
        @DisplayName("Should continue filter chain when not matching protected path")
        void shouldContinueFilterChainWhenNotMatchingProtectedPath() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/some/other/path").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            // When
            Mono<Void> result = captchaFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify chain was called
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("Should continue filter chain when no captcha token in session")
        void shouldContinueFilterChainWhenNoCaptchaTokenInSession() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.post("/oauth2/token").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            // When
            Mono<Void> result = captchaFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify chain was called
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("Should validate captcha token successfully")
        void shouldValidateCaptchaTokenSuccessfully() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.post("/oauth2/token")
                    .header("X-CAPTCHA-TOKEN", "54321")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            WebSession session = exchange.getSession().block();
            CaptchaToken captchaToken = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "54321");
            session.getAttributes().put(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, captchaToken);

            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());
            when(captchaRepository.loadToken(exchange)).thenReturn(Mono.just(captchaToken));
            when(captchaRepository.clearToken(exchange)).thenReturn(Mono.empty());

            // When
            Mono<Void> result = captchaFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify repository methods were called
            verify(captchaRepository).loadToken(exchange);
            verify(captchaRepository).clearToken(exchange);
            // Verify chain was called
            verify(chain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle invalid captcha token")
        void shouldHandleInvalidCaptchaToken() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.post("/oauth2/token")
                    .header("X-CAPTCHA-TOKEN", "wrong-code")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            WebSession session = exchange.getSession().block();
            CaptchaToken captchaToken = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "54321");
            session.getAttributes().put(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, captchaToken);

            WebFilterChain chain = mock(WebFilterChain.class);
            when(captchaRepository.loadToken(exchange)).thenReturn(Mono.just(captchaToken));

            // When
            Mono<Void> result = captchaFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                    .expectError(CaptchaFilter.CaptchaException.class)
                    .verify();

            // Verify repository method was called
            verify(captchaRepository).loadToken(exchange);
        }

        @Test
        @DisplayName("Should handle missing captcha token")
        void shouldHandleMissingCaptchaToken() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.post("/oauth2/token").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            WebSession session = exchange.getSession().block();
            // Don't put captcha token in session

            WebFilterChain chain = mock(WebFilterChain.class);
            when(chain.filter(exchange)).thenReturn(Mono.empty());

            // When
            Mono<Void> result = captchaFilter.filter(exchange, chain);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify chain was called
            verify(chain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Access Denied Handler Tests")
    class AccessDeniedHandlerTests {

        @Test
        @DisplayName("Should handle access denied with proper response")
        void shouldHandleAccessDeniedWithProperResponse() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.post("/oauth2/token").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            CaptchaFilter.CaptchaServerAccessDeniedHandler handler =
                    new CaptchaFilter.CaptchaServerAccessDeniedHandler(HttpStatus.FORBIDDEN);
            CaptchaException exception = new CaptchaException("Invalid Captcha Token");

            // When
            Mono<Void> result = handler.handle(exchange, exception);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify response status and content type
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.APPLICATION_JSON);
        }
    }
}