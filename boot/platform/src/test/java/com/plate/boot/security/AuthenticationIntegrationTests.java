package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Authentication integration tests.
 * <p>
 * Covers login authentication flows for different user roles,
 * credential validation, and token response structure.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Authentication")
class AuthenticationIntegrationTests extends AbstractIntegrationTests {

    @Nested
    @DisplayName("Admin Login")
    class AdminLoginTests {

        private String adminToken;

        @BeforeEach
        void setUp() {
            adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("should return token with full details when admin authenticates via Basic Auth")
        void shouldReturnTokenWithFullDetails() {
            // When
            var response = loginWithBasicAuth(ADMIN_USERNAME, ADMIN_PASSWORD);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isNotBlank();
            assertThat(response.expires()).isPositive();
            assertThat(response.lastAccessTime()).isPositive();
            assertThat(response.details()).isNotNull();
        }

        @Test
        @DisplayName("should return admin profile with correct name and nickname via Bearer token")
        void shouldReturnAdminProfileViaBearerToken() {
            // Given — adminToken from setUp

            // When
            var response = loginWithBearerToken(adminToken);

            // Then
            assertThat(response.details()).isNotNull();
            // Details is a SecurityDetails map — verify via JSON path as fallback
            webTestClient.get().uri(oauth2Base() + "/login")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.name").isEqualTo("admin")
                    .jsonPath("$.details.nickname").isEqualTo("系统超级管理员")
                    .jsonPath("$.details.enabled").isEqualTo(true);
        }

        @Test
        @DisplayName("should include SYSTEM_ADMINISTRATORS and GROUP_ADMINISTRATORS authorities")
        void shouldIncludeAdminAuthorities() {
            // Given — adminToken from setUp

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/login")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.authorities").isArray()
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_SYSTEM_ADMINISTRATORS')]").exists()
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_GROUP_ADMINISTRATORS')]").exists();
        }
    }

    @Nested
    @DisplayName("User Login")
    class UserLoginTests {

        private String userToken;

        @BeforeEach
        void setUp() {
            userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);
        }

        @Test
        @DisplayName("should return token with details when regular user authenticates via Basic Auth")
        void shouldReturnTokenWithDetails() {
            // When
            var response = loginWithBasicAuth(USER_USERNAME, USER_PASSWORD);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.token()).isNotBlank();
            assertThat(response.details()).isNotNull();
        }

        @Test
        @DisplayName("should return authorities as array for regular user")
        void shouldReturnUserAuthoritiesAsArray() {
            // Given — userToken from setUp

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/login")
                    .headers(headers -> headers.setBearerAuth(userToken))
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.authorities").isArray();
        }
    }

    @Nested
    @DisplayName("Invalid Credentials")
    class InvalidCredentialsTests {

        @Test
        @DisplayName("should reject request without credentials with 401")
        void shouldRejectWithoutCredentials() {
            // Given — no Authorization header

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/login")
                    .exchange().expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("should reject non-existent user credentials with 401")
        void shouldRejectNonExistentUser() {
            // Given
            String credentials = encodeBasicCredentials("invalid", "credentials");

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange().expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("should reject valid user with wrong password with 401")
        void shouldRejectWrongPassword() {
            // Given
            String credentials = encodeBasicCredentials(ADMIN_USERNAME, "wrongPassword");

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange().expectStatus().isUnauthorized();
        }
    }

    // ---- Helper ----

    private AuthenticationToken loginWithBearerToken(String token) {
        return webTestClient.get().uri(oauth2Base() + "/login")
                .headers(headers -> headers.setBearerAuth(token))
                .exchange().expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .returnResult().getResponseBody();
    }
}
