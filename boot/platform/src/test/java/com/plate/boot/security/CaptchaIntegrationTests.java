package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Captcha integration tests.
 * <p>
 * Covers captcha code generation endpoint,
 * including response format and authentication behavior.
 * Note: The captcha endpoint is in the security package, so it gets the
 * "sec" path prefix from WebConfiguration, making the actual path
 * /sec/v1/captcha/code (requires authentication).
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Captcha Integration Tests")
class CaptchaIntegrationTests extends AbstractIntegrationTests {

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Test
    @DisplayName("Should generate captcha code with authentication")
    void shouldGenerateCaptchaCodeWithAuthentication() {
        webTestClient.get().uri(paths.getCaptchaBase() + "/code")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk();
    }

    @Test
    @DisplayName("Should return captcha image with correct content type")
    void shouldReturnCaptchaImageWithContentType() {
        webTestClient.get().uri(paths.getCaptchaBase() + "/code")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk()
                .expectHeader().contentType("image/png");
    }
}
