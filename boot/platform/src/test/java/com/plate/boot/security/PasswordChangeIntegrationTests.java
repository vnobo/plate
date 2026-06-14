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
@DisplayName("Password Change Integration Tests")
class PasswordChangeIntegrationTests extends AbstractIntegrationTests {

    @Nested
    @DisplayName("Validation Errors")
    class ValidationTests {

        private String adminToken;

        @BeforeEach
        void setUp() {
            adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("Should reject empty password fields - 4xx")
        void shouldRejectEmptyPasswordFields() {
            var request = """
                    {
                        "password": "",
                        "newPassword": ""
                    }
                    """;

            webTestClient.post().uri(paths.getOauth2Base() + "/change/password")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange().expectStatus().is4xxClientError();
        }

        @Test
        @DisplayName("Should reject wrong current password - 4xx")
        void shouldRejectWrongCurrentPassword() {
            var request = """
                    {
                        "password": "wrongPassword",
                        "newPassword": "newPassword123"
                    }
                    """;

            webTestClient.post().uri(paths.getOauth2Base() + "/change/password")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(request))
                    .exchange().expectStatus().is4xxClientError();
        }

        @Test
        @DisplayName("Should reject same old and new password - 4xx")
        void shouldRejectSameOldAndNewPassword() {
            var request = Map.of("password", "123456", "newPassword", "123456");

            webTestClient.post().uri(paths.getOauth2Base() + "/change/password")
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
        @DisplayName("Should reject password change without authentication - 403")
        void shouldRejectWithoutAuthentication() {
            var request = Map.of("password", "oldPass", "newPassword", "newPass");

            webTestClient.post().uri(paths.getOauth2Base() + "/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange().expectStatus().isForbidden();
        }
    }
}
