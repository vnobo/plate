package com.plate.boot.security.jwt;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for the stateless JWT authentication endpoints covering the
 * PC and native app client channels.
 * <p>
 * Requires the Testcontainers infrastructure defined in {@code InfrastructureConfiguration}
 * (PostgreSQL + Redis) and therefore runs only where a Docker environment is available.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("JWT Authentication (PC / App)")
class JwtAuthIntegrationTests extends AbstractIntegrationTests {

    @Test
    @DisplayName("should issue a token pair for PC login with username and password")
    void shouldIssueTokenPairForPcLogin() {
        webTestClient.post().uri(secPrefix() + "/oauth2/login")
                .bodyValue(Map.of("username", ADMIN_USERNAME, "password", ADMIN_PASSWORD))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .jsonPath("$.expiresIn").isNumber()
                .jsonPath("$.clientType").isEqualTo("PC");
    }

    @Test
    @DisplayName("should accept the issued JWT as a bearer credential on protected endpoints")
    void shouldAcceptJwtBearerOnProtectedEndpoints() {
        String accessToken = loginPcAndExtract("accessToken");

        webTestClient.get().uri(captchaBase() + "/code")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .exchange().expectStatus().isOk()
                .expectHeader().contentType("image/png");
    }

    @Test
    @DisplayName("should issue an APP client type pair for native app logins")
    void shouldIssueAppClientTypePair() {
        webTestClient.post().uri(secPrefix() + "/oauth2/login")
                .bodyValue(Map.of("username", ADMIN_USERNAME, "password", ADMIN_PASSWORD,
                        "clientType", "APP"))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.clientType").isEqualTo("APP");
    }

    @Test
    @DisplayName("should reject protected access without a token")
    void shouldRejectProtectedAccessWithoutToken() {
        webTestClient.get().uri(captchaBase() + "/code")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("should reject malformed and tampered bearer tokens")
    void shouldRejectTamperedTokens() {
        webTestClient.get().uri(captchaBase() + "/code")
                .headers(headers -> headers.setBearerAuth("aGVsbG8.d29ybGQ.hanNoA"))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("should reject wrong password with 401")
    void shouldRejectWrongPassword() {
        webTestClient.post().uri(secPrefix() + "/oauth2/login")
                .bodyValue(Map.of("username", ADMIN_USERNAME, "password", "wrong-password"))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("should rotate the token pair on refresh")
    void shouldRotateTokenPairOnRefresh() {
        String refreshToken = loginPcAndExtract("refreshToken");

        var response = webTestClient.post().uri(secPrefix() + "/oauth2/refresh")
                .bodyValue(Map.of("refreshToken", refreshToken))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.tokenType").isEqualTo("Bearer")
                .returnResult();

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("should reject access tokens presented at the refresh endpoint")
    void shouldRejectAccessTokensAtRefreshEndpoint() {
        String accessToken = loginPcAndExtract("accessToken");

        webTestClient.post().uri(secPrefix() + "/oauth2/refresh")
                .bodyValue(Map.of("refreshToken", accessToken))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("should reject an unbound third-party identity")
    void shouldRejectUnboundOauth2Login() {
        webTestClient.post().uri(secPrefix() + "/oauth2/login/github")
                .bodyValue(Map.of("openid", "unbound-" + UUID.randomUUID()))
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("should bind a third-party account and then log in with it")
    void shouldBindThirdPartyAccountAndLoginWithIt() {
        String openid = "it-github-" + UUID.randomUUID();

        webTestClient.post().uri(secPrefix() + "/oauth2/bind")
                .bodyValue(Map.of("username", ADMIN_USERNAME, "password", ADMIN_PASSWORD,
                        "registrationId", "github", "openid", openid))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty();

        webTestClient.post().uri(secPrefix() + "/oauth2/login/github")
                .bodyValue(Map.of("openid", openid))
                .exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.clientType").isEqualTo("PC");
    }

    /**
     * Executes a PC login and extracts a field from the {@link TokenPair} response body.
     *
     * @param jsonKey the response field name, e.g. {@code accessToken}
     * @return the string value of the field
     */
    private String loginPcAndExtract(String jsonKey) {
        Map<?, ?> body = webTestClient.post().uri(secPrefix() + "/oauth2/login")
                .bodyValue(Map.of("username", ADMIN_USERNAME, "password", ADMIN_PASSWORD))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        assertThat(body).isNotNull();
        return String.valueOf(body.get(jsonKey));
    }
}
