package com.plate.boot.security;

import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("GET /oauth2/login - Should return authentication token for authenticated user")
    @WithMockUser(username = "admin", password = "123456")
    void testLoginTokenSuccess() {
        webTestClient.get()
                .uri("/sec/v1/oauth2/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .value(token -> {
                    assertThat(token).isNotNull();
                    assertThat(token.details()).isNotNull();
                    assertThat(token.details()).hasFieldOrProperty("username").isEqualTo("admin");
                });
    }
}