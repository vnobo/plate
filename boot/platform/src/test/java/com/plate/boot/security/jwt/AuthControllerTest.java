package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthController}: password login, mini-program login and refresh
 * rotation for the PC / mini-program / app client channels.
 */
@DisplayName("AuthController")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String SECRET = "controller-test-secret";

    @Mock
    private ReactiveAuthenticationManager authenticationManager;

    @Mock
    private WechatMiniAppUserService wechatMiniAppUserService;

    @Mock
    private SecurityManager securityManager;

    private JwtService jwtService;
    private AuthController controller;
    private SecurityDetails details;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenTtl(Duration.ofMinutes(5));
        properties.setRefreshTokenTtl(Duration.ofHours(1));
        this.jwtService = new JwtService(properties);
        this.controller = new AuthController(this.authenticationManager, this.jwtService,
                this.wechatMiniAppUserService, this.securityManager);
        this.details = buildDetails("alice");
    }

    @Test
    @DisplayName("should issue a parseable token pair on successful password login, defaulting to PC")
    void shouldIssueTokenPairOnPasswordLogin() {
        when(this.authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(this.details)));

        StepVerifier.create(this.controller.login(new AuthController.LoginRequest("alice", "secret", null)))
                .assertNext(pair -> {
                    assertThat(pair.tokenType()).isEqualTo("Bearer");
                    assertThat(pair.clientType()).isEqualTo(ClientType.PC);
                    assertThat(pair.expiresIn()).isEqualTo(300);
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should stamp the requested app client type on issued tokens")
    void shouldStampAppClientType() {
        when(this.authenticationManager.authenticate(any())).thenReturn(Mono.just(authenticated(this.details)));

        StepVerifier.create(this.controller.login(new AuthController.LoginRequest("alice", "secret", ClientType.APP))
                        .flatMap(pair -> this.jwtService.parse(pair.accessToken())))
                .assertNext(payload -> {
                    assertThat(payload.clientType()).isEqualTo(ClientType.APP);
                    assertThat(payload.username()).isEqualTo("alice");
                    assertThat(payload.tokenType()).isEqualTo(TokenType.ACCESS);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should propagate authentication failure on wrong password")
    void shouldPropagateAuthenticationFailure() {
        when(this.authenticationManager.authenticate(any()))
                .thenReturn(Mono.error(new BadCredentialsException("Bad credentials")));

        StepVerifier.create(this.controller.login(new AuthController.LoginRequest("alice", "wrong", ClientType.PC)))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should issue a mini-app token pair for WeChat mini-program login")
    void shouldIssueTokenPairForMiniAppLogin() {
        when(this.wechatMiniAppUserService.login("wx-code")).thenReturn(Mono.just(this.details));

        StepVerifier.create(this.controller.wechatMiniAppLogin(new AuthController.WechatMiniAppLoginRequest("wx-code"))
                        .flatMap(pair -> this.jwtService.parse(pair.accessToken())))
                .assertNext(payload -> {
                    assertThat(payload.clientType()).isEqualTo(ClientType.MINIAPP);
                    assertThat(payload.username()).isEqualTo("alice");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should rotate the token pair on a valid refresh token")
    void shouldRotateTokenPairOnRefresh() {
        when(this.securityManager.findByUsername("alice")).thenReturn(Mono.just(this.details));
        String refreshToken = this.jwtService.issueTokenPair(this.details, ClientType.PC).block().refreshToken();

        StepVerifier.create(this.controller.refresh(new AuthController.RefreshRequest(refreshToken)))
                .assertNext(pair -> {
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotEqualTo(refreshToken);
                    assertThat(pair.clientType()).isEqualTo(ClientType.PC);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should reject access tokens presented at the refresh endpoint")
    void shouldRejectAccessTokensAtRefreshEndpoint() {
        String accessToken = this.jwtService.issueTokenPair(this.details, ClientType.PC).block().accessToken();

        StepVerifier.create(this.controller.refresh(new AuthController.RefreshRequest(accessToken)))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject garbage refresh tokens")
    void shouldRejectGarbageRefreshTokens() {
        StepVerifier.create(this.controller.refresh(new AuthController.RefreshRequest("not-a-real-token")))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    private static UsernamePasswordAuthenticationToken authenticated(SecurityDetails details) {
        return UsernamePasswordAuthenticationToken.authenticated(details, details.getPassword(), details.getAuthorities());
    }

    private static SecurityDetails buildDetails(String username) {
        var granted = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityDetails details = new SecurityDetails(granted, Map.of("username", username), "username");
        details.setCode(UUID.randomUUID());
        details.setUsername(username);
        details.setPassword("$2a$10$not-a-real-hash");
        details.setDisabled(false);
        details.setAccountExpired(false);
        details.setAccountLocked(false);
        details.setCredentialsExpired(false);
        return details;
    }
}
