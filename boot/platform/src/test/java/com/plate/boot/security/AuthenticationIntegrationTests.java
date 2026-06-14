package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Base64;

/**
 * Authentication integration tests.
 * <p>
 * Covers login authentication flows for different user roles,
 * credential validation, and token response structure.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTests extends AbstractIntegrationTests {

    @Nested
    @DisplayName("Administrator Authentication")
    class AdminAuthenticationTests {

        private String adminToken;

        @BeforeEach
        void setUp() {
            adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        }

        @Test
        @DisplayName("Should authenticate admin with Basic Auth")
        void shouldAuthenticateAdminWithBasicAuth() {
            String credentials = Base64.getEncoder()
                    .encodeToString((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes());

            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").exists()
                    .jsonPath("$.details").exists()
                    .jsonPath("$.expires").exists()
                    .jsonPath("$.lastAccessTime").exists();
        }

        @Test
        @DisplayName("Should return admin details with correct profile")
        void shouldReturnAdminDetailsWithProfile() {
            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").exists()
                    .jsonPath("$.details.name").isEqualTo("admin")
                    .jsonPath("$.details.nickname").isEqualTo("系统超级管理员")
                    .jsonPath("$.details.enabled").isEqualTo(true);
        }

        @Test
        @DisplayName("Should include admin authorities in token")
        void shouldIncludeAdminAuthorities() {
            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.authorities").isArray()
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_SYSTEM_ADMINISTRATORS')]").exists()
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_GROUP_ADMINISTRATORS')]").exists();
        }
    }

    @Nested
    @DisplayName("Regular User Authentication")
    class UserAuthenticationTests {

        private String userToken;

        @BeforeEach
        void setUp() {
            userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);
        }

        @Test
        @DisplayName("Should authenticate user with Basic Auth")
        void shouldAuthenticateUserWithBasicAuth() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange().expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").exists()
                    .jsonPath("$.details").exists()
                    .jsonPath("$.expires").exists();
        }

        @Test
        @DisplayName("Should return user authorities as array")
        void shouldReturnUserAuthorities() {
            webTestClient.get().uri(paths.getOauth2Base() + "/login")
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
        @DisplayName("Should reject login without credentials - 401")
        void shouldRejectLoginWithoutCredentials() {
            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .exchange().expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should reject login with invalid credentials - 401")
        void shouldRejectInvalidCredentials() {
            String invalidCredentials = Base64.getEncoder()
                    .encodeToString("invalid:credentials".getBytes());

            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .header("Authorization", "Basic " + invalidCredentials)
                    .exchange().expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should reject login with wrong password - 401")
        void shouldRejectWrongPassword() {
            String wrongCredentials = Base64.getEncoder()
                    .encodeToString((ADMIN_USERNAME + ":wrongPassword").getBytes());

            webTestClient.get().uri(paths.getOauth2Base() + "/login")
                    .header("Authorization", "Basic " + wrongCredentials)
                    .exchange().expectStatus().isUnauthorized();
        }
    }
}
