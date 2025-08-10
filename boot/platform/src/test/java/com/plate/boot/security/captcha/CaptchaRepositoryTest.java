package com.plate.boot.security.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CaptchaRepository Unit Tests
 *
 * <p>This test class provides unit tests for the CaptchaRepository class, covering:</p>
 * <ul>
 *   <li>Captcha token generation</li>
 *   <li>Captcha token saving and loading</li>
 *   <li>Captcha token clearing</li>
 * </ul>
 *
 * @author Qwen Code
 */
class CaptchaRepositoryTest {

    private CaptchaRepository captchaRepository;

    @BeforeEach
    void setUp() {
        captchaRepository = new CaptchaRepository();
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate captcha token")
        void shouldGenerateCaptchaToken() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<CaptchaToken> result = captchaRepository.generateToken(exchange);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(captchaToken -> {
                        assertThat(captchaToken).isNotNull();
                        assertThat(captchaToken.headerName()).isEqualTo(CaptchaRepository.DEFAULT_CAPTCHA_HEADER_NAME);
                        assertThat(captchaToken.parameterName()).isEqualTo(CaptchaRepository.DEFAULT_CAPTCHA_PARAMETER_NAME);
                        assertThat(captchaToken.captcha()).isEqualTo("54321");
                        return true;
                    })
                    .verifyComplete();

            // Verify the token is added to exchange attributes
            CaptchaToken token = exchange.getAttribute(CaptchaToken.class.getName());
            assertThat(token).isNotNull();
            assertThat(token.captcha()).isEqualTo("54321");
        }
    }

    @Nested
    @DisplayName("Token Save Tests")
    class TokenSaveTests {

        @Test
        @DisplayName("Should save captcha token to session")
        void shouldSaveCaptchaTokenToSession() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebSession session = exchange.getSession().block();
            CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "ABCD1234");

            // When
            Mono<Void> result = captchaRepository.saveToken(exchange, token);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify the token is saved in session attributes
            Map<String, Object> attributes = session.getAttributes();
            assertThat(attributes).containsKey(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME);
            CaptchaToken savedToken = (CaptchaToken) attributes.get(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME);
            assertThat(savedToken).isEqualTo(token);
        }
    }

    @Nested
    @DisplayName("Token Load Tests")
    class TokenLoadTests {

        @Test
        @DisplayName("Should load captcha token from session")
        void shouldLoadCaptchaTokenFromSession() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebSession session = exchange.getSession().block();
            CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "ABCD1234");
            session.getAttributes().put(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, token);

            // When
            Mono<CaptchaToken> result = captchaRepository.loadToken(exchange);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(loadedToken -> {
                        assertThat(loadedToken).isNotNull();
                        assertThat(loadedToken).isEqualTo(token);
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should return empty when no captcha token in session")
        void shouldReturnEmptyWhenNoCaptchaTokenInSession() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // When
            Mono<CaptchaToken> result = captchaRepository.loadToken(exchange);

            // Then
            StepVerifier.create(result)
                    .verifyComplete(); // Empty completion
        }
    }

    @Nested
    @DisplayName("Token Clear Tests")
    class TokenClearTests {

        @Test
        @DisplayName("Should clear captcha token from session")
        void shouldClearCaptchaTokenFromSession() {
            // Given
            MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            WebSession session = exchange.getSession().block();
            CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "ABCD1234");
            session.getAttributes().put(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME, token);

            // When
            Mono<Void> result = captchaRepository.clearToken(exchange);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify the token is removed from session attributes
            Map<String, Object> attributes = session.getAttributes();
            assertThat(attributes).doesNotContainKey(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME);
        }
    }
}