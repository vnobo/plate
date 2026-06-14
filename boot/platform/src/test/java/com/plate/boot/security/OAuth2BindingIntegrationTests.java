package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * OAuth2 binding integration tests.
 * <p>
 * Covers OAuth2 client binding scenarios including valid/invalid client
 * registration IDs and authentication requirements.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("OAuth2 Binding")
class OAuth2BindingIntegrationTests extends AbstractIntegrationTests {

    @Nested
    @DisplayName("Authentication Requirements")
    class AuthenticationTests {

        @Test
        @DisplayName("should reject OAuth2 bind request without authentication with 401")
        void shouldRejectWithoutAuthentication() {
            // Given — no Authorization header

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/bind?clientRegistrationId=github")
                    .exchange().expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("Client Registration Validation")
    class ClientRegistrationTests {

        private String userToken;

        @BeforeEach
        void setUp() {
            userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);
        }

        @Test
        @DisplayName("should return server error for unbound OAuth2 client")
        void shouldReturnErrorForUnboundClient() {
            // Given — userToken from setUp, github client not bound

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/bind?clientRegistrationId=github")
                    .headers(headers -> headers.setBearerAuth(userToken))
                    .exchange().expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("should reject request with missing clientRegistrationId with 400")
        void shouldRejectMissingClientRegistrationId() {
            // Given — userToken from setUp

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/bind")
                    .headers(headers -> headers.setBearerAuth(userToken))
                    .exchange().expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("should reject request with empty clientRegistrationId with 400")
        void shouldRejectEmptyClientRegistrationId() {
            // Given — userToken from setUp

            // When & Then
            webTestClient.get().uri(oauth2Base() + "/bind?clientRegistrationId=")
                    .headers(headers -> headers.setBearerAuth(userToken))
                    .exchange().expectStatus().isBadRequest();
        }
    }
}
