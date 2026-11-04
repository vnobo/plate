package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtSecurityContextRepository}: JWT bearer resolution, anonymous
 * fallback on invalid tokens and delegation of session-based requests.
 */
@DisplayName("JwtSecurityContextRepository")
@ExtendWith(MockitoExtension.class)
class JwtSecurityContextRepositoryTest {

    private static final String SECRET = "repository-test-secret";

    @Mock
    private SecurityManager securityManager;

    private JwtService jwtService;
    private JwtSecurityContextRepository repository;
    private SecurityDetails details;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenTtl(Duration.ofMinutes(5));
        properties.setRefreshTokenTtl(Duration.ofHours(1));
        this.jwtService = new JwtService(properties);
        this.repository = new JwtSecurityContextRepository(this.jwtService, this.securityManager);
        this.details = buildDetails("bob");
    }

    @Test
    @DisplayName("should authenticate requests carrying a valid JWT bearer token")
    void shouldAuthenticateJwtBearerRequests() {
        when(this.securityManager.loadUserDetails("bob")).thenReturn(Mono.just(this.details));
        MockServerWebExchange exchange = exchangeWith(bearer(this.jwtService, this.details));

        StepVerifier.create(this.repository.load(exchange))
                .assertNext(context -> {
                    assertThat(context.getAuthentication().getPrincipal()).isSameAs(this.details);
                    assertThat(context.getAuthentication().isAuthenticated()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should resolve the principal read-only, without recording a login")
    void shouldNotRecordLoginWhenResolvingPrincipal() {
        when(this.securityManager.loadUserDetails("bob")).thenReturn(Mono.just(this.details));
        MockServerWebExchange exchange = exchangeWith(bearer(this.jwtService, this.details));

        StepVerifier.create(this.repository.load(exchange)).expectNextCount(1).verifyComplete();
        verify(this.securityManager, never()).findByUsername(any());
    }

    @Test
    @DisplayName("should resolve to anonymous when the JWT is invalid or tampered")
    void shouldResolveToAnonymousOnTamperedJwt() {
        String validToken = this.jwtService.issueTokenPair(this.details, ClientType.PC).block().accessToken();
        MockServerWebExchange exchange = exchangeWith("Bearer " + tamper(validToken));

        StepVerifier.create(this.repository.load(exchange))
                .verifyComplete();
    }

    @Test
    @DisplayName("should reject refresh tokens used as request credentials")
    void shouldRejectRefreshTokensAsCredentials() {
        MockServerWebExchange exchange = exchangeWith("Bearer " + refresh(this.jwtService, this.details));

        StepVerifier.create(this.repository.load(exchange))
                .verifyComplete();
    }

    @Test
    @DisplayName("should delegate session-id bearer and plain requests to the session repository")
    void shouldDelegateNonJwtRequestsToSessionRepository() {
        MockServerWebExchange exchange = exchangeWith("Bearer session-id-without-dots");

        StepVerifier.create(this.repository.load(exchange))
                .verifyComplete();
    }

    @Test
    @DisplayName("should not create or touch a session when saving a JWT-authenticated request")
    void shouldStayStatelessOnSaveForJwtRequests() {
        MockServerWebExchange exchange = exchangeWith(bearer(this.jwtService, this.details));
        SecurityContext context = new org.springframework.security.core.context.SecurityContextImpl();

        StepVerifier.create(this.repository.save(exchange, context))
                .verifyComplete();
        assertThat(exchange.getSession().block().getAttributes()).isEmpty();
    }

    @Test
    @DisplayName("isJwtToken should discriminate JWS compact tokens from session ids")
    void shouldDiscriminateTokenShapes() {
        assertThat(JwtSecurityContextRepository.isJwtToken(bearer(this.jwtService, this.details)
                .substring("Bearer ".length()))).isTrue();
        assertThat(JwtSecurityContextRepository.isJwtToken("session-id-without-dots")).isFalse();
        assertThat(JwtSecurityContextRepository.isJwtToken(null)).isFalse();
    }

    private static MockServerWebExchange exchangeWith(String authorization) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/sec/v1/rel/users")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .build();
        return MockServerWebExchange.from(request);
    }

    private static String bearer(JwtService service, SecurityDetails details) {
        String token = service.issueTokenPair(details, ClientType.PC).block().accessToken();
        return "Bearer " + token;
    }

    private static String refresh(JwtService service, SecurityDetails details) {
        return service.issueTokenPair(details, ClientType.PC).block().refreshToken();
    }

    /**
     * Corrupts the payload segment of a compact JWS so the signature check must fail.
     */
    private static String tamper(String token) {
        String[] segments = token.split("\\.");
        String payload = segments[1];
        String flipped = (payload.startsWith("A") ? "B" : "A") + payload.substring(1);
        return segments[0] + "." + flipped + "." + segments[2];
    }

    private static SecurityDetails buildDetails(String username) {
        var granted = List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
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
