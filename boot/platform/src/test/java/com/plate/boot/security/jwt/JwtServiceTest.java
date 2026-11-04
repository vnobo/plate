package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtService}: token round-trips, claim layout, refresh and rejection paths.
 */
@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-value";

    private SecurityManager securityManager;
    private SecurityDetails details;

    @BeforeEach
    void setUp() {
        this.securityManager = mock(SecurityManager.class);
        this.details = buildDetails("alice", List.of("ROLE_ADMIN", "user:read"));
    }

    @Test
    @DisplayName("should issue a token pair whose access token round-trips all claims")
    void shouldIssueAndParseAccessToken() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.PC))
                .assertNext(pair -> {
                    assertThat(pair.tokenType()).isEqualTo("Bearer");
                    assertThat(pair.expiresIn()).isEqualTo(300);
                    assertThat(pair.clientType()).isEqualTo(ClientType.PC);
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotBlank();
                    assertThat(pair.accessToken()).isNotEqualTo(pair.refreshToken());
                })
                .verifyComplete();

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.PC)
                        .flatMap(pair -> service.parse(pair.accessToken())))
                .assertNext(payload -> {
                    assertThat(payload.username()).isEqualTo("alice");
                    assertThat(payload.code()).isEqualTo(this.details.getCode().toString());
                    assertThat(payload.clientType()).isEqualTo(ClientType.PC);
                    assertThat(payload.tokenType()).isEqualTo(TokenType.ACCESS);
                    assertThat(payload.authorities()).containsExactlyInAnyOrder("ROLE_ADMIN", "user:read");
                    assertThat(payload.jti()).isNotBlank();
                    assertThat(payload.expiresAt()).isAfter(payload.issuedAt());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should type refresh tokens as REFRESH and omit authorities")
    void shouldTypeRefreshTokensAsRefresh() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.APP)
                        .flatMap(pair -> service.parse(pair.refreshToken())))
                .assertNext(payload -> {
                    assertThat(payload.tokenType()).isEqualTo(TokenType.REFRESH);
                    assertThat(payload.clientType()).isEqualTo(ClientType.APP);
                    assertThat(payload.username()).isEqualTo("alice");
                    assertThat(payload.authorities()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should reject expired tokens with BadCredentialsException")
    void shouldRejectExpiredTokens() throws InterruptedException {
        JwtService service = newService(properties(SECRET, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        String expired = service.issueTokenPair(this.details, ClientType.PC).block().accessToken();
        Thread.sleep(2200);

        StepVerifier.create(service.parse(expired))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject expired refresh tokens as well")
    void shouldRejectExpiredRefreshTokens() throws InterruptedException {
        JwtService service = newService(properties(SECRET, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        String expired = service.issueTokenPair(this.details, ClientType.PC).block().refreshToken();
        Thread.sleep(1500);

        StepVerifier.create(service.parse(expired))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject tokens whose payload was tampered with")
    void shouldRejectTamperedTokens() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.PC)
                        .map(pair -> tamper(pair.accessToken()))
                        .flatMap(service::parse))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject tokens signed with a different secret")
    void shouldRejectForeignSignatures() {
        JwtService issuer = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        JwtService verifier = newService(properties("another-secret-value",
                Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(issuer.issueTokenPair(this.details, ClientType.PC)
                        .flatMap(pair -> verifier.parse(pair.accessToken())))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject garbage token strings")
    void shouldRejectGarbageTokens() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.parse("not-a-jwt"))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject null arguments when issuing a pair")
    void shouldRejectNullIssueArguments() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        assertThatThrownBy(() -> service.issueTokenPair(null, ClientType.PC))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> service.issueTokenPair(this.details, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should omit the code claim when the user has no business code")
    void shouldOmitCodeClaimWhenAbsent() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        this.details.setCode(null);

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.PC)
                        .flatMap(pair -> service.parse(pair.accessToken())))
                .assertNext(payload -> assertThat(payload.code()).isNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("should refresh a pair while preserving the client type")
    void shouldRefreshPairPreservingClientType() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        TokenPair issued = service.issueTokenPair(this.details, ClientType.APP).block();
        assertThat(issued).isNotNull();
        when(this.securityManager.loadUserDetails("alice")).thenReturn(Mono.just(this.details));

        StepVerifier.create(service.refresh(issued.refreshToken()))
                .assertNext(pair -> {
                    assertThat(pair.clientType()).isEqualTo(ClientType.APP);
                    assertThat(pair.accessToken()).isNotBlank();
                    assertThat(pair.refreshToken()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should reject access tokens at the refresh flow")
    void shouldRejectAccessTokenOnRefresh() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        TokenPair issued = service.issueTokenPair(this.details, ClientType.PC).block();
        assertThat(issued).isNotNull();

        StepVerifier.create(service.refresh(issued.accessToken()))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("should reject refresh when the user no longer exists")
    void shouldRejectRefreshForUnknownUser() {
        JwtService service = newService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        TokenPair issued = service.issueTokenPair(this.details, ClientType.PC).block();
        assertThat(issued).isNotNull();
        when(this.securityManager.loadUserDetails("alice")).thenReturn(Mono.empty());

        StepVerifier.create(service.refresh(issued.refreshToken()))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    @DisplayName("should reject an expired refresh token at the refresh flow")
    void shouldRejectExpiredRefreshOnRefresh() throws InterruptedException {
        JwtService service = newService(properties(SECRET, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        TokenPair issued = service.issueTokenPair(this.details, ClientType.PC).block();
        assertThat(issued).isNotNull();
        Thread.sleep(1500);

        StepVerifier.create(service.refresh(issued.refreshToken()))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    private JwtService newService(JwtProperties properties) {
        return new JwtService(properties, this.securityManager);
    }

    private static JwtProperties properties(String secret, Duration accessTtl, Duration refreshTtl) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        properties.setIssuer("plate-test");
        properties.setAccessTokenTtl(accessTtl);
        properties.setRefreshTokenTtl(refreshTtl);
        return properties;
    }

    private static SecurityDetails buildDetails(String username, List<String> authorities) {
        var granted = authorities.stream().map(SimpleGrantedAuthority::new).toList();
        SecurityDetails details = new SecurityDetails(granted, Map.of("username", username), "username");
        details.setCode(UUID.randomUUID());
        details.setUsername(username);
        details.setPassword("$2a$10$not-a-real-hash");
        details.setNickname(username);
        details.setDisabled(false);
        details.setAccountExpired(false);
        details.setAccountLocked(false);
        details.setCredentialsExpired(false);
        return details;
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
}
