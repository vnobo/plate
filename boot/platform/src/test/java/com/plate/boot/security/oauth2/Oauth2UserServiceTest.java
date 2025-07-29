package com.plate.boot.security.oauth2;

import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ExtendWith(MockitoExtension.class)
class Oauth2UserServiceTest {

    @Mock
    private SecurityManager securityManager;

    @InjectMocks
    private Oauth2UserService oauth2UserService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Nested
    @DisplayName("Load User Tests")
    class LoadUserTests {

        private final ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("github")
                .clientId("test-client").clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login/oauth2/code/github")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .build();

        @Test
        @DisplayName("Should load existing user by OAuth2")
        void shouldLoadExistingUserByOAuth2() {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                    "token", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS));
            OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken, Map.of("id", "123"));

            when(securityManager.loadByOauth2("github", "123")).thenReturn(Mono.just(testUser));
            OAuth2User oAuth2User = mock(OAuth2User.class, withSettings().extraInterfaces(UserDetails.class));
            when(securityManager.findByUsername("testuser")).thenReturn(Mono.just((UserDetails) oAuth2User));

            StepVerifier.create(oauth2UserService.loadUser(userRequest))
                    .expectNext(oAuth2User)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should register new user if not found by OAuth2")
        void shouldRegisterNewUserIfNotFoundByOAuth2() {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                    "token", Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS));
            OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, accessToken,
                    Map.of("id", "123", "login", "newuser", "email", "new@example.com"));

            when(securityManager.loadByOauth2("github", "123")).thenReturn(Mono.empty());
            when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(testUser));
            OAuth2User oAuth2User = mock(OAuth2User.class, withSettings().extraInterfaces(UserDetails.class));
            when(securityManager.findByUsername(anyString())).thenReturn(Mono.just((UserDetails) oAuth2User));

            StepVerifier.create(oauth2UserService.loadUser(userRequest))
                    .expectNext(oAuth2User)
                    .verifyComplete();
        }
    }
}