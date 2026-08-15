package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityController.ChangePasswordRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityController}.
 */
@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private SecurityManager securityManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ServerOAuth2AuthorizedClientRepository clientRepository;

    @InjectMocks
    private SecurityController controller;

    @Test
    void loginTokenBuildsTokenFromSessionAndPrincipal() {
        WebSession session = mock(WebSession.class);
        when(session.getId()).thenReturn("session-1");
        when(session.getLastAccessTime()).thenReturn(Instant.ofEpochSecond(1000));
        when(session.getMaxIdleTime()).thenReturn(Duration.ofSeconds(1800));

        Object principal = new Object();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        StepVerifier.create(controller.loginToken(session, authentication))
                .assertNext(token -> {
                    assertThat(token.token()).isEqualTo("session-1");
                    assertThat(token.expires()).isEqualTo(1800L);
                    assertThat(token.lastAccessTime()).isEqualTo(1000L);
                    assertThat(token.details()).isSameAs(principal);
                })
                .verifyComplete();
    }

    @Test
    void loginTokenDefaultsMissingSessionTimes() {
        WebSession session = mock(WebSession.class);
        when(session.getId()).thenReturn("session-2");
        when(session.getLastAccessTime()).thenReturn(null);
        when(session.getMaxIdleTime()).thenReturn(null);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("principal");

        StepVerifier.create(controller.loginToken(session, authentication))
                .assertNext(token -> {
                    assertThat(token.expires()).isEqualTo(1800L);
                    assertThat(token.lastAccessTime()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void csrfTokenReturnsTokenFromContext() {
        CsrfToken csrfToken = mock(CsrfToken.class);

        StepVerifier.create(controller.csrfToken()
                        .contextWrite(ctx -> ctx.put(ContextUtils.CSRF_TOKEN_CONTEXT, csrfToken)))
                .expectNext(csrfToken)
                .verifyComplete();
    }

    @Test
    void csrfTokenErrorsWhenContextMissing() {
        StepVerifier.create(controller.csrfToken())
                .expectError()
                .verify();
    }

    @Test
    void bindOauth2ReturnsAccessToken() {
        String clientRegistrationId = "github";
        Authentication authentication = mock(Authentication.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange))
                .thenReturn(Mono.just(authorizedClient));

        StepVerifier.create(controller.bindOauth2(clientRegistrationId, authentication, exchange))
                .expectNext(accessToken)
                .verifyComplete();
    }

    @Test
    void bindOauth2ErrorsWhenClientNotFound() {
        String clientRegistrationId = "github";
        Authentication authentication = mock(Authentication.class);
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.bindOauth2(clientRegistrationId, authentication, exchange))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(RestServerException.class);
                    assertThat(err.getMessage()).contains("github");
                })
                .verify();
    }

    @Test
    void changePasswordRejectsSamePassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("secret");
        request.setNewPassword("secret");
        Authentication authentication = mock(Authentication.class);

        assertThatThrownBy(() -> controller.changePassword(request, authentication))
                .isInstanceOf(RestServerException.class)
                .hasMessageContaining("same as current");

        verify(securityManager, never()).updatePassword(any(), anyString());
    }

    @Test
    void changePasswordRejectsWhenCurrentPasswordMismatches() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("old");
        request.setNewPassword("new");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn("encoded-old");
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(false);

        assertThatThrownBy(() -> controller.changePassword(request, authentication))
                .isInstanceOf(RestServerException.class)
                .hasMessageContaining("verification failed");

        verify(passwordEncoder, never()).encode(anyString());
        verify(securityManager, never()).updatePassword(any(), anyString());
    }

    @Test
    void changePasswordRejectsWhenPrincipalIsNotUserDetails() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("old");
        request.setNewPassword("new");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn("encoded-old");
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");
        when(authentication.getPrincipal()).thenReturn("not-user-details");

        assertThatThrownBy(() -> controller.changePassword(request, authentication))
                .isInstanceOf(RestServerException.class)
                .hasMessageContaining("User details not found");

        verify(securityManager, never()).updatePassword(any(), anyString());
    }

    @Test
    void changePasswordUpdatesPasswordForUserDetails() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("old");
        request.setNewPassword("new");

        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn("encoded-old");
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encoded-new");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        UserDetails updated = mock(UserDetails.class);
        when(securityManager.updatePassword(userDetails, "encoded-new")).thenReturn(Mono.just(updated));

        StepVerifier.create(controller.changePassword(request, authentication))
                .expectNext(updated)
                .verifyComplete();

        verify(securityManager).updatePassword(userDetails, "encoded-new");
    }
}
