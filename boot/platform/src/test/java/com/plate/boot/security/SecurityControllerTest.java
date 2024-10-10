package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityControllerTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SecurityManager securityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ServerOAuth2AuthorizedClientRepository clientRepository;

    @Autowired
    private WebSessionServerSecurityContextRepository securityContextRepository;

    @BeforeEach
    public void setup() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(this.context)
                // add Spring Security test Support
                .apply(springSecurity())
                .configureClient()
                .filter(basicAuthentication("admin", "123456"))
                .build();
    }

    @Test
    void loginToken_ValidUser_ReturnsAuthenticationToken() {
        Authentication authentication = new TestingAuthenticationToken("user", "password");
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(exchange.getSession()).thenReturn(Mono.just(mock(WebSession.class)));
        when(securityContextRepository.save(any(), any())).thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/oauth2/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationToken.class);
    }

    @Test
    @WithMockUser
    void csrfToken_ReturnsCsrfToken() {
        webTestClient.get()
                .uri("/oauth2/csrf")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CsrfToken.class);
    }

    @Test
    @WithMockUser
    void bindOauth2_ValidClientRegistrationId_ReturnsAccessToken() {
        String clientRegistrationId = "test-client";
        Authentication authentication = new TestingAuthenticationToken("user", "password");
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        when(clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange))
                .thenReturn(Mono.just(mock(OAuth2AuthorizedClient.class)));

        webTestClient
                .get()
                .uri("/oauth2/bind")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class); // Assuming the access token is a String
    }

    @Test
    @WithMockUser
    void changePassword_PasswordsMatch_UpdatesPassword() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");

        Authentication authentication = new TestingAuthenticationToken("user", "password");
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getDetails()).thenReturn(userDetails);
        when(passwordEncoder.matches("oldPassword", "oldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(securityManager.updatePassword(userDetails, "encodedPassword")).thenReturn(Mono.just(userDetails));

        webTestClient
                .post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDetails.class);
    }

    @Test
    @WithMockUser
    void changePassword_PasswordsDoNotMatch_ThrowsException() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("differentPassword");

        Authentication authentication = new TestingAuthenticationToken("user", "password");

        webTestClient
                .post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(RestServerException.class);
    }

    @Test
    @WithMockUser
    void changePassword_PresentedPasswordDoesNotMatch_ThrowsException() {
        SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
        request.setPassword("oldPassword");
        request.setNewPassword("newPassword");

        Authentication authentication = new TestingAuthenticationToken("user", "password");
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getDetails()).thenReturn(userDetails);
        when(passwordEncoder.matches("oldPassword", "oldPassword")).thenReturn(false);

        webTestClient
                .post()
                .uri("/oauth2/change/password")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(RestServerException.class);
    }
}
