package com.plate.boot.security;

import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for {@link SecurityController}.
 * Tests the complete integration with Spring Security configuration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(SecurityController.class)
class SecurityControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "integrationuser", roles = {"USER"})
    void testCompleteSecurityFlow() {
        // Test login token
        webTestClient.get()
                .uri("/oauth2/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .consumeWith(result -> {
                    AuthenticationToken token = result.getResponseBody();
                    assert token != null;
                    assert token.details().equals("integrationuser");
                });

        // Test CSRF token
        webTestClient.get()
                .uri("/oauth2/csrf")
                .exchange()
                .expectStatus().isOk();
    }
}