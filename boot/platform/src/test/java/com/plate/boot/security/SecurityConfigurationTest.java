package com.plate.boot.security;

import com.plate.boot.config.SecurityConfiguration;
import com.plate.boot.security.oauth2.Oauth2SuccessHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password=",
    "logging.level.org.springframework.security=DEBUG"
})
@Import({SecurityConfiguration.class, SecurityConfigurationTest.TestController.class})
class SecurityConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private Oauth2SuccessHandler oauth2SuccessHandler;

    @MockitoBean
    private SecurityManager securityManager;

    @MockitoBean
    private ServerOAuth2AuthorizedClientRepository clientRepository;

    @MockitoBean
    private ReactiveClientRegistrationRepository registrationRepository;

    @MockitoBean
    private DatabaseClient databaseClient;

    @Test
    @DisplayName("Should allow access to captcha endpoints")
    void shouldAllowAccessToCaptchaEndpoints() {
        webTestClient.get().uri("/captcha/code")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should redirect unauthenticated users to login page")
    void shouldRedirectUnauthenticatedUsersToLoginPage() {
        webTestClient.get().uri("/private")
                .exchange()
                .expectStatus().isFound();
    }

    @Test
    @WithMockUser
    @DisplayName("Should allow authenticated users to access secured endpoints")
    void shouldAllowAuthenticatedUsersToAccessSecuredEndpoints() {
        webTestClient.get().uri("/private")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("private content");
    }

    @Test
    @DisplayName("Should create delegating password encoder bean")
    void shouldCreateDelegatingPasswordEncoderBean(@Autowired PasswordEncoder passwordEncoder) {
        assertThat(passwordEncoder).isNotNull();
        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        assertThat(encodedPassword).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @RestController
    static class TestController {
        @GetMapping("/private")
        public String privateEndpoint() {
            return "private content";
        }
    }
}
