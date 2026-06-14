package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Captcha integration tests.
 * <p>
 * Covers captcha code generation endpoint, including response format
 * and authentication behavior.
 * The actual path is {@code /sec/v1/captcha/code} (requires authentication).
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Captcha")
class CaptchaIntegrationTests extends AbstractIntegrationTests {

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Test
    @DisplayName("should return 200 and PNG image when authenticated user requests captcha code")
    void shouldReturnPngImageForAuthenticatedUser() {
        // Given — adminToken from setUp

        // When & Then
        var response = webTestClient.get().uri(captchaBase() + "/code")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk()
                .expectHeader().contentType("image/png")
                .expectBody().returnResult();

        assertThat(response.getResponseBody()).isNotNull();
    }
}
