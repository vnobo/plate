package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Map;

/**
 * Password change integration tests.
 * <p>
 * Covers the password change endpoint including validation errors,
 * authentication requirements, and boundary conditions.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Password Change")
class PasswordChangeIntegrationTests extends AbstractIntegrationTests {

    private static final String CHANGE_PASSWORD_URI = "/change/password";

    @Nested
    @DisplayName("Validation Errors")
    class ValidationTests {

        private String adminToken;

        @BeforeEach
        void setUp() {
            adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("should reject empty password fields with 4xx")
        void shouldRejectEmptyPasswordFields() {
            // Given
            var request = """
                    {"password": "", "newPassword": ""}
                    """;

            // When & Then
            postChangePasswordAndExpect4xx(adminToken, request);
        }

        @Test
        @DisplayName("should reject wrong current password with 4xx")
        void shouldRejectWrongCurrentPassword() {
            // Given
            var request = """
                    {"password": "wrongPassword", "newPassword": "newPassword123"}
                    """;

            // When & Then
            postChangePasswordAndExpect4xx(adminToken, request);
        }

        @Test
        @DisplayName("should reject same old and new password with 4xx")
        void shouldRejectSameOldAndNewPassword() {
            // Given
            var request = Map.of("password", "123456", "newPassword", "123456");

            // When & Then
            webTestClient.post().uri(oauth2Base() + CHANGE_PASSWORD_URI)
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange().expectStatus().is4xxClientError();
        }
    }

    @Nested
    @DisplayName("Authentication Requirements")
    class AuthenticationTests {

        @Test
        @DisplayName("should reject password change without authentication with 403")
        void shouldRejectWithoutAuthentication() {
            // Given
            var request = Map.of("password", "oldPass", "newPassword", "newPass");

            // When & Then
            webTestClient.post().uri(oauth2Base() + CHANGE_PASSWORD_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange().expectStatus().isForbidden();
        }
    }

    // ---- Helper ----

    private void postChangePasswordAndExpect4xx(String token, String jsonBody) {
        webTestClient.post().uri(oauth2Base() + CHANGE_PASSWORD_URI)
                .headers(headers -> headers.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonBody))
                .exchange().expectStatus().is4xxClientError();
    }
}
