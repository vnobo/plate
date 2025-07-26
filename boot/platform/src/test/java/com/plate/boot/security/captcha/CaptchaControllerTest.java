package com.plate.boot.security.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test suite for {@link CaptchaController}.
 *
 * @author CodeBuddy
 */
@WebFluxTest(CaptchaController.class)
class CaptchaControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CaptchaRepository captchaRepository;

    private CaptchaToken testCaptchaToken;

    @BeforeEach
    void setUp() {
        // Setup a test captcha token
        testCaptchaToken = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "1234");
    }

    @Test
    @DisplayName("Should return captcha image successfully")
    void shouldReturnCaptchaImageSuccessfully() {
        // Arrange
        when(captchaRepository.generateToken(any(ServerWebExchange.class)))
                .thenReturn(Mono.just(testCaptchaToken));
        when(captchaRepository.saveToken(any(ServerWebExchange.class), any(CaptchaToken.class)))
                .thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get().uri("/captcha/code")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG)
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    byte[] body = response.getResponseBody();
                    org.assertj.core.api.Assertions.assertThat(body).isNotNull();
                    org.assertj.core.api.Assertions.assertThat(body.length).isGreaterThan(0);
                });
    }

    @Test
    @DisplayName("Should handle captcha generation failure")
    void shouldHandleCaptchaGenerationFailure() {
        // Arrange
        when(captchaRepository.generateToken(any(ServerWebExchange.class)))
                .thenReturn(Mono.error(new RuntimeException("Generation failed")));

        // Act & Assert
        webTestClient.get().uri("/captcha/code")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}