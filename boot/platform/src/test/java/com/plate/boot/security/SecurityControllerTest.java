package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.security.SecurityController.ChangePasswordRequest;
import com.plate.boot.security.SecurityController.RefreshTokenRequest;
import com.plate.boot.security.jwt.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SecurityController}, focusing on the JWT issuance and refresh endpoints.
 */
@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private SecurityManager securityManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ServerOAuth2AuthorizedClientRepository clientRepository;
    @Mock
    private RememberMeService rememberMeService;
    @Mock
    private org.springframework.security.web.server.context.ServerSecurityContextRepository securityContextRepository;
    @Mock
    private TokenService tokenService;
    @Mock
    private ReactiveAuthenticationManager authenticationManager;
    @Spy
    private JwtService jwtService = new JwtService(jwtProperties());

    @InjectMocks
    private SecurityController controller;

    private static SecurityDetails detailsOf(UUID code) {
        SecurityDetails d = new SecurityDetails(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("username", "admin"), "username");
        d.setCode(code);
        d.setUsername("admin");
        return d;
    }

    private static JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-secret-value");
        properties.setIssuer("plate-test");
        properties.setAccessTokenTtl(Duration.ofMinutes(5));
        properties.setRefreshTokenTtl(Duration.ofHours(1));
        return properties;
    }

    private static Authentication authenticated(SecurityDetails details) {
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    @Test
    void loginIssuesTokenPairForDefaultPcClient() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(details)));

        StepVerifier.create(controller.login(new SecurityController.LoginRequest("admin", "pw", null)))
                .assertNext(pair -> {
                    assertThat(pair.tokenType()).isEqualTo("Bearer");
                    assertThat(pair.clientType()).isEqualTo(ClientType.PC);
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void loginHonoursRequestedClientType() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        when(authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(details)));

        StepVerifier.create(controller.login(new SecurityController.LoginRequest("admin", "pw", ClientType.APP)))
                .assertNext(pair -> assertThat(pair.clientType()).isEqualTo(ClientType.APP))
                .verifyComplete();
    }

    @Test
    void loginRejectsBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(Mono.error(new BadCredentialsException("bad credentials")));

        StepVerifier.create(controller.login(new SecurityController.LoginRequest("admin", "wrong", null)))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void refreshRotatesPairAndKeepsClientType() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        TokenPair issued = jwtService.issueTokenPair(details, ClientType.APP).block();
        assertThat(issued).isNotNull();
        when(securityManager.loadUserDetails("admin")).thenReturn(Mono.just(details));

        StepVerifier.create(controller.refresh(new SecurityController.RefreshRequest(issued.refreshToken())))
                .assertNext(pair -> {
                    assertThat(pair.clientType()).isEqualTo(ClientType.APP);
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void refreshRejectsAccessToken() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        TokenPair issued = jwtService.issueTokenPair(details, ClientType.PC).block();
        assertThat(issued).isNotNull();

        StepVerifier.create(controller.refresh(new SecurityController.RefreshRequest(issued.accessToken())))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void refreshRejectsGarbageToken() {
        StepVerifier.create(controller.refresh(new SecurityController.RefreshRequest("not-a-jwt")))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void refreshRejectsUnknownUser() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        TokenPair issued = jwtService.issueTokenPair(details, ClientType.PC).block();
        assertThat(issued).isNotNull();
        when(securityManager.loadUserDetails("admin")).thenReturn(Mono.empty());

        StepVerifier.create(controller.refresh(new SecurityController.RefreshRequest(issued.refreshToken())))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void loginTokenIssuesJwtPair() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(details);
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        TokenService.TokenPair pair = new TokenService.TokenPair("access", "refresh", 900L);
        when(tokenService.issue(details)).thenReturn(Mono.just(pair));

        StepVerifier.create(controller.loginToken(auth, exchange, false))
                .assertNext(token -> {
                    assertThat(token.token()).isEqualTo("access");
                    assertThat(token.refreshToken()).isEqualTo("refresh");
                    assertThat(token.expires()).isEqualTo(900L);
                })
                .verifyComplete();
        verify(rememberMeService, never()).issue(any(), any());
    }

    @Test
    void loginTokenIssuesRememberMeCookieWhenRequested() {
        SecurityDetails details = detailsOf(UUID.randomUUID());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(details);
        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(tokenService.issue(details)).thenReturn(Mono.just(new TokenService.TokenPair("a", "r", 900L)));
        when(rememberMeService.issue(exchange, "admin")).thenReturn(Mono.empty());

        StepVerifier.create(controller.loginToken(auth, exchange, true)).expectNextCount(1).verifyComplete();
        verify(rememberMeService).issue(exchange, "admin");
    }

    @Test
    void loginTokenRejectsUnsupportedPrincipal() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-details");
        ServerWebExchange exchange = mock(ServerWebExchange.class);

        StepVerifier.create(controller.loginToken(auth, exchange, false))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(RestServerException.class))
                .verify();
    }

    @Test
    void rotateReturnsNewPair() {
        UUID code = UUID.randomUUID();
        TokenService.Rotation rotation = new TokenService.Rotation(code, "admin", "fam", 0L);
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh");
        SecurityDetails details = detailsOf(code);
        when(tokenService.consume("refresh")).thenReturn(Mono.just(rotation));
        when(securityManager.loadUserDetails("admin")).thenReturn(Mono.just(details));
        when(tokenService.rotate(details, rotation)).thenReturn(Mono.just(new TokenService.TokenPair("newAccess", "newRefresh", 900L)));

        StepVerifier.create(controller.rotate(request))
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(resp.getBody().token()).isEqualTo("newAccess");
                    assertThat(resp.getBody().refreshToken()).isEqualTo("newRefresh");
                })
                .verifyComplete();
    }

    @Test
    void rotateReturnsUnauthorizedOnInvalidToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad");
        when(tokenService.consume("bad")).thenReturn(Mono.error(new InvalidBearerTokenException("no")));

        StepVerifier.create(controller.rotate(request))
                .assertNext(resp -> assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                .verifyComplete();
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
        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getCredentials()).thenReturn("encoded-old");
        when(passwordEncoder.matches("old", "encoded-old")).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        StepVerifier.create(controller.changePassword(request, authentication))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(RestServerException.class)
                        .hasMessageContaining("verification failed"))
                .verify();
        verify(securityManager, never()).updatePassword(any(), anyString());
    }

    @Test
    void changePasswordRejectsWhenPrincipalIsNotUserDetails() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setPassword("old");
        request.setNewPassword("new");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not-user-details");

        assertThatThrownBy(() -> controller.changePassword(request, authentication))
                .isInstanceOf(RestServerException.class)
                .hasMessageContaining("User details not found");
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
