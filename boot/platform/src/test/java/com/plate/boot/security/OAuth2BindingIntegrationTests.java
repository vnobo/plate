package com.plate.boot.security;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * OAuth2 binding integration tests.
 * <p>
 * Covers OAuth2 client binding scenarios including
 * valid/invalid client registration IDs and authentication requirements.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("OAuth2 Binding Integration Tests")
class OAuth2BindingIntegrationTests extends AbstractIntegrationTests {

    private String userToken;

    @BeforeEach
    void setUp() {
        userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);
    }

    @Test
    @DisplayName("Should reject OAuth2 bind without authentication - 401")
    void shouldRejectBindWithoutAuthentication() {
        webTestClient.get().uri(paths.getOauth2Base() + "/bind?clientRegistrationId=github")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should return error for unbound OAuth2 client")
    void shouldReturnErrorForUnboundClient() {
        webTestClient.get().uri(paths.getOauth2Base() + "/bind?clientRegistrationId=github")
                .headers(headers -> headers.setBearerAuth(userToken))
                .exchange().expectStatus().is5xxServerError();
    }

    @Test
    @DisplayName("Should reject bind request with missing clientRegistrationId - 400")
    void shouldRejectMissingClientRegistrationId() {
        webTestClient.get().uri(paths.getOauth2Base() + "/bind")
                .headers(headers -> headers.setBearerAuth(userToken))
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should reject bind with empty clientRegistrationId - 400")
    void shouldRejectEmptyClientRegistrationId() {
        webTestClient.get().uri(paths.getOauth2Base() + "/bind?clientRegistrationId=")
                .headers(headers -> headers.setBearerAuth(userToken))
                .exchange().expectStatus().isBadRequest();
    }
}
