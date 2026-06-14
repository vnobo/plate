package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * CSRF token integration tests.
 * <p>
 * Verifies CSRF token retrieval and format validation,
 * including authentication requirements and token structure.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("CSRF Token Integration Tests")
class CsrfTokenIntegrationTests extends AbstractIntegrationTests {

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Test
    @DisplayName("Should reject unauthenticated CSRF token request - 401")
    void shouldRejectUnauthenticatedCsrfRequest() {
        webTestClient.get().uri(paths.getOauth2Base() + "/csrf")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should return valid CSRF token for authenticated user")
    void shouldReturnCsrfTokenForAuthenticatedUser() {
        webTestClient.get().uri(paths.getOauth2Base() + "/csrf")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.headerName").isEqualTo("X-XSRF-TOKEN")
                .jsonPath("$.parameterName").isEqualTo("_csrf");
    }

    @Test
    @DisplayName("Should return CSRF token with non-empty header and parameter names")
    void shouldReturnCsrfTokenWithMetadata() {
        webTestClient.get().uri(paths.getOauth2Base() + "/csrf")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isNotEmpty()
                .jsonPath("$.headerName").isNotEmpty()
                .jsonPath("$.parameterName").isNotEmpty();
    }
}
