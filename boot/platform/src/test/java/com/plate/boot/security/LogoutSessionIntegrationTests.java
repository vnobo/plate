package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Logout and session integration tests.
 * <p>
 * Covers logout flow, session management, and
 * session information verification.
 * Note: POST /oauth2/logout requires CSRF token (X-XSRF-TOKEN header).
 * Since WebTestClient does not manage cookies/sessions automatically,
 * the logout POST will be rejected by CSRF protection (403).
 * This is the expected security behavior.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Logout and Session Integration Tests")
class LogoutSessionIntegrationTests extends AbstractIntegrationTests {

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);
    }

    @Test
    @DisplayName("Should reject logout without CSRF token - 403")
    void shouldRejectLogoutWithoutCsrfToken() {
        // POST /oauth2/logout requires CSRF token; without it, returns 403
        webTestClient.post().uri(paths.getOauth2Base() + "/logout")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange().expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Should verify session info via login endpoint")
    void shouldVerifySessionInfo() {
        webTestClient.get().uri(paths.getOauth2Base() + "/login")
                .headers(headers -> headers.setBearerAuth(userToken))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo(userToken)
                .jsonPath("$.details").exists()
                .jsonPath("$.expires").exists()
                .jsonPath("$.lastAccessTime").exists();
    }

    @Test
    @DisplayName("Should reject unauthenticated logout request - 403 (CSRF)")
    void shouldRejectUnauthenticatedLogout() {
        // POST without auth returns 403 due to CSRF protection, not 401
        webTestClient.post().uri(paths.getOauth2Base() + "/logout")
                .exchange().expectStatus().isForbidden();
    }
}
