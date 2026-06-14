package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CSRF token integration tests.
 * <p>
 * Verifies CSRF token retrieval, authentication requirements,
 * and token response structure (headerName, parameterName, token).
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("CSRF Token")
class CsrfTokenIntegrationTests extends AbstractIntegrationTests {

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Test
    @DisplayName("should reject unauthenticated CSRF token request with 401")
    void shouldRejectUnauthenticatedRequest() {
        // Given — no Authorization header

        // When & Then
        webTestClient.get().uri(oauth2Base() + "/csrf")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("should return CSRF token with correct header and parameter names for authenticated user")
    void shouldReturnCsrfTokenWithMetadata() {
        // Given — adminToken from setUp

        // When & Then
        webTestClient.get().uri(oauth2Base() + "/csrf")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.headerName").isEqualTo("X-XSRF-TOKEN")
                .jsonPath("$.parameterName").isEqualTo("_csrf");
    }
}
