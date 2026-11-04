package com.plate.boot.security;

import com.plate.boot.security.core.user.User;
import com.plate.boot.security.jwt.ClientType;
import com.plate.boot.security.jwt.JwtProperties;
import com.plate.boot.security.jwt.JwtService;
import com.plate.boot.security.jwt.TokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the unified authentication entry point, {@link SecurityController}.
 */
@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private ReactiveAuthenticationManager authenticationManager;
    @Mock
    private SecurityManager securityManager;

    private JwtService jwtService;
    private SecurityController controller;

    private static SecurityDetails detailsOf() {
        SecurityDetails details = new SecurityDetails(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("username", "admin"), "username");
        details.setCode(UUID.randomUUID());
        details.setUsername("admin");
        return details;
    }

    private static Authentication authenticated(SecurityDetails details) {
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("unit-test-secret-value");
        properties.setIssuer("plate-test");
        properties.setAccessTokenTtl(Duration.ofMinutes(5));
        properties.setRefreshTokenTtl(Duration.ofHours(1));
        this.jwtService = new JwtService(properties, this.securityManager);
        this.controller = new SecurityController(this.authenticationManager, this.jwtService, this.securityManager);
    }

    @Test
    void loginIssuesTokenPairForDefaultPcClient() {
        SecurityDetails details = detailsOf();
        when(this.authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(details)));

        StepVerifier.create(this.controller.login(new SecurityController.LoginRequest("admin", "pw", null)))
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
        SecurityDetails details = detailsOf();
        when(this.authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(details)));

        StepVerifier.create(this.controller.login(
                        new SecurityController.LoginRequest("admin", "pw", ClientType.APP)))
                .assertNext(pair -> assertThat(pair.clientType()).isEqualTo(ClientType.APP))
                .verifyComplete();
    }

    @Test
    void loginRejectsBadCredentials() {
        when(this.authenticationManager.authenticate(any()))
                .thenReturn(Mono.error(new BadCredentialsException("bad credentials")));

        StepVerifier.create(this.controller.login(new SecurityController.LoginRequest("admin", "wrong", null)))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void refreshRotatesPairAndKeepsClientType() {
        SecurityDetails details = detailsOf();
        TokenPair issued = this.jwtService.issueTokenPair(details, ClientType.APP).block();
        assertThat(issued).isNotNull();
        when(this.securityManager.loadUserDetails("admin")).thenReturn(Mono.just(details));

        StepVerifier.create(this.controller.refresh(
                        new SecurityController.RefreshRequest(issued.refreshToken())))
                .assertNext(pair -> {
                    assertThat(pair.clientType()).isEqualTo(ClientType.APP);
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void refreshRejectsAccessToken() {
        SecurityDetails details = detailsOf();
        TokenPair issued = this.jwtService.issueTokenPair(details, ClientType.PC).block();
        assertThat(issued).isNotNull();

        StepVerifier.create(this.controller.refresh(
                        new SecurityController.RefreshRequest(issued.accessToken())))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void refreshRejectsGarbageToken() {
        StepVerifier.create(this.controller.refresh(
                        new SecurityController.RefreshRequest("not-a-jwt")))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void refreshRejectsUnknownUser() {
        SecurityDetails details = detailsOf();
        TokenPair issued = this.jwtService.issueTokenPair(details, ClientType.PC).block();
        assertThat(issued).isNotNull();
        when(this.securityManager.loadUserDetails("admin")).thenReturn(Mono.empty());

        StepVerifier.create(this.controller.refresh(
                        new SecurityController.RefreshRequest(issued.refreshToken())))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void oauth2LoginIssuesPairForBoundAccount() {
        SecurityDetails details = detailsOf();
        User user = new User();
        user.setUsername("admin");
        when(this.securityManager.loadByOauth2("github", "gh-123")).thenReturn(Mono.just(user));
        when(this.securityManager.loadUserDetails("admin")).thenReturn(Mono.just(details));

        StepVerifier.create(this.controller.oauth2Login("github",
                        new SecurityController.Oauth2LoginRequest("gh-123", ClientType.APP)))
                .assertNext(pair -> {
                    assertThat(pair.clientType()).isEqualTo(ClientType.APP);
                    assertThat(pair.accessToken()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    void oauth2LoginRejectsUnboundAccount() {
        when(this.securityManager.loadByOauth2("github", "gh-999")).thenReturn(Mono.empty());

        StepVerifier.create(this.controller.oauth2Login("github",
                        new SecurityController.Oauth2LoginRequest("gh-999", null)))
                .expectError(BadCredentialsException.class)
                .verify();
        verify(this.securityManager, never()).loadUserDetails(any());
    }

    @Test
    void bindAssociatesThirdPartyAccountAndIssuesPair() {
        SecurityDetails details = detailsOf();
        when(this.authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(details)));
        when(this.securityManager.bindOauth2("admin", "github", "gh-123")).thenReturn(Mono.just(1L));

        StepVerifier.create(this.controller.bind(new SecurityController.BindRequest(
                        "admin", "pw", "github", "gh-123", null)))
                .assertNext(pair -> {
                    assertThat(pair.clientType()).isEqualTo(ClientType.PC);
                    assertThat(pair.accessToken()).isNotBlank();
                })
                .verifyComplete();
        verify(this.securityManager).bindOauth2("admin", "github", "gh-123");
    }

    @Test
    void bindRejectsWrongLocalPassword() {
        when(this.authenticationManager.authenticate(any()))
                .thenReturn(Mono.error(new BadCredentialsException("bad credentials")));

        StepVerifier.create(this.controller.bind(new SecurityController.BindRequest(
                        "admin", "wrong", "github", "gh-123", null)))
                .expectError(BadCredentialsException.class)
                .verify();
        verify(this.securityManager, never()).bindOauth2(any(), any(), any());
    }

    @Test
    void bindRejectsUnknownLocalAccount() {
        SecurityDetails details = detailsOf();
        when(this.authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(details)));
        when(this.securityManager.bindOauth2("admin", "github", "gh-123")).thenReturn(Mono.just(0L));

        StepVerifier.create(this.controller.bind(new SecurityController.BindRequest(
                        "admin", "pw", "github", "gh-123", null)))
                .expectError(BadCredentialsException.class)
                .verify();
    }
}
