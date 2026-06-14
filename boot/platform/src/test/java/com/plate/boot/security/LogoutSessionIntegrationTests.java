package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Logout and session integration tests.
 * <p>
 * Covers logout flow, session management, and session information verification.
 * Note: POST /oauth2/logout requires CSRF token (X-XSRF-TOKEN header).
 * Since WebTestClient does not manage cookies/sessions automatically,
 * the logout POST will be rejected by CSRF protection (403).
 * This is the expected security behavior.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Logout & Session")
class LogoutSessionIntegrationTests extends AbstractIntegrationTests {

    @Nested
    @DisplayName("CSRF Protection")
    class CsrfProtectionTests {

        private String adminToken;

        @BeforeEach
        void setUp() {
            adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("should reject authenticated logout without CSRF token with 403")
        void shouldRejectAuthenticatedLogoutWithoutCsrf() {
            // Given — adminToken from setUp, but no CSRF token

            // When & Then
            webTestClient.post().uri(oauth2Base() + "/logout")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange().expectStatus().isForbidden();
        }

        @Test
        @DisplayName("should reject unauthenticated logout with 403 (CSRF takes precedence)")
        void shouldRejectUnauthenticatedLogout() {
            // Given — no auth, no CSRF token

            // When & Then
            webTestClient.post().uri(oauth2Base() + "/logout")
                    .exchange().expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("Session Verification")
    class SessionVerificationTests {

        private String userToken;

        @BeforeEach
        void setUp() {
            userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);
        }

        @Test
        @DisplayName("should return consistent session info when re-querying login endpoint")
        void shouldReturnConsistentSessionInfo() {
            // Given — userToken from setUp

            // When
            var response = webTestClient.get().uri(oauth2Base() + "/login")
                    .headers(headers -> headers.setBearerAuth(userToken))
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").isEqualTo(userToken)
                    .jsonPath("$.details").exists()
                    .jsonPath("$.expires").exists()
                    .jsonPath("$.lastAccessTime").exists()
                    .returnResult();

            // Then — response body is present
            assertThat(response.getResponseBody()).isNotNull();
        }
    }
}
