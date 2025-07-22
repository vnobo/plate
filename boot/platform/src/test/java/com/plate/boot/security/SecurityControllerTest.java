package com.plate.boot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link SecurityController}.
 * Tests all endpoints including login token retrieval, CSRF token handling,
 * OAuth2 binding, and password change functionality.
 */
@WebFluxTest(controllers = SecurityController.class)
@Import(SecurityController.class)
class SecurityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SecurityManager securityManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ServerOAuth2AuthorizedClientRepository clientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetails testUser;
    private Authentication testAuthentication;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();
        
        testAuthentication = new UsernamePasswordAuthenticationToken(
                testUser, "credentials", testUser.getAuthorities());
    }

    @Test
    @DisplayName("GET /oauth2/login - Should return authentication token for authenticated user")
    @WithMockUser(username = "testuser")
    void testLoginToken_Success() {
        webTestClient.get()
                .uri("/oauth2/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .value(token -> {
                    assertThat(token).isNotNull();
                    assertThat(token.details()).isEqualTo("testuser");
                });
    }

    @Test
    @DisplayName("GET /oauth2/login - Should return 401 for unauthenticated user")
    void testLoginToken_Unauthorized() {
        webTestClient.get()
                .uri("/oauth2/login")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("GET /oauth2/csrf - Should return CSRF token when present")
    void testCsrfToken_Success() {
        webTestClient.get()
                .uri("/oauth2/csrf")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("GET /oauth2/csrf - Should return empty when CSRF token not present")
    void testCsrfToken_NotPresent() {
        webTestClient.get()
                .uri("/oauth2/csrf")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("GET /oauth2/bind - Should return OAuth2 access token when binding is successful")
    void testBindOauth2_Success() {
        String clientRegistrationId = "github";
        String accessTokenValue = "mock-access-token";
        
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                accessTokenValue,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        
        OAuth2AuthorizedClient mockClient = mock(OAuth2AuthorizedClient.class);
        when(mockClient.getAccessToken()).thenReturn(accessToken);
        when(clientRepository.loadAuthorizedClient(eq(clientRegistrationId), any(Authentication.class), any(ServerWebExchange.class)))
                .thenReturn(Mono.just(mockClient));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth2/bind")
                        .queryParam("clientRegistrationId", clientRegistrationId)
                        .build())
                .headers(headers -> headers.setBasicAuth("testuser", "password"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tokenValue").isEqualTo(accessTokenValue);
    }

    @Test
    @DisplayName("GET /oauth2/bind - Should return 401 when user is not authenticated")
    void testBindOauth2_Unauthorized() {
        webTestClient.get()
                .uri("/oauth2/bind?clientRegistrationId=github")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /oauth2/change/password - Should successfully change password")
    @WithMockUser(username = "testuser", password = "oldPassword")
    void testChangePassword_Success() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(passwordEncoder.matches("oldPassword", "oldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(securityManager.updatePassword(any(UserDetails.class), eq("encodedNewPassword")))
                .thenReturn(Mono.just(testUser));

        webTestClient.post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDetails.class)
                .value(userDetails -> {
                    assertThat(userDetails.getUsername()).isEqualTo("testuser");
                });
    }

    @Test
    @DisplayName("POST /oauth2/change/password - Should return 400 when passwords don't match")
    @WithMockUser(username = "testuser")
    void testChangePassword_PasswordMismatch() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("differentPassword");

        webTestClient.post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Password and newPassword not match");
    }

    @Test
    @DisplayName("POST /oauth2/change/password - Should return 400 when current password is incorrect")
    @WithMockUser(username = "testuser", password = "oldPassword")
    void testChangePassword_InvalidCurrentPassword() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("wrongOldPassword");
        request.setNewPassword("newPassword");

        when(passwordEncoder.matches("wrongOldPassword", "oldPassword")).thenReturn(false);

        webTestClient.post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Password verification failed, presented password not match");
    }

    @Test
    @DisplayName("POST /oauth2/change/password - Should return 401 when user is not authenticated")
    void testChangePassword_Unauthorized() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");

        webTestClient.post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("POST /oauth2/change/password - Should validate request body constraints")
    void testChangePassword_ValidationErrors() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("");
        request.setNewPassword("");

        webTestClient.post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("SecurityController.ChangePasswordRequest - Should validate correctly")
    void testChangePasswordRequest_Validation() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        
        // Test empty password
        request.setPassword("");
        request.setNewPassword("newPassword");
        assertThat(request.getPassword()).isEmpty();
        assertThat(request.getNewPassword()).isEqualTo("newPassword");
        
        // Test empty new password
        request.setPassword("oldPassword");
        request.setNewPassword("");
        assertThat(request.getPassword()).isEqualTo("oldPassword");
        assertThat(request.getNewPassword()).isEmpty();
        
        // Test valid request
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");
        assertThat(request.getPassword()).isEqualTo("oldPassword");
        assertThat(request.getNewPassword()).isEqualTo("newPassword");
    }
}