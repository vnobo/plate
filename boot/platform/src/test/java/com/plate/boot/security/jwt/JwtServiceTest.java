package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtService}: token round-trips, claim layout and rejection paths.
 */
@DisplayName("JwtService")
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-value";

    private SecurityDetails details;

    @BeforeEach
    void setUp() {
        this.details = buildDetails("alice", List.of("ROLE_ADMIN", "user:read"));
    }

    @Test
    @DisplayName("should issue a token pair whose access token round-trips all claims")
    void shouldIssueAndParseAccessToken() {
        JwtService service = new JwtService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

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
        JwtService service = new JwtService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

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
    @DisplayName("should stamp the mini-program client type on issued tokens")
    void shouldStampMiniAppClientType() {
        JwtService service = new JwtService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.MINIAPP)
                        .flatMap(pair -> service.parse(pair.accessToken())))
                .assertNext(payload -> assertThat(payload.clientType()).isEqualTo(ClientType.MINIAPP))
                .verifyComplete();
    }

    @Test
    @DisplayName("should reject expired tokens with BadCredentialsException")
    void shouldRejectExpiredTokens() throws InterruptedException {
        JwtService service = new JwtService(properties(SECRET, Duration.ofSeconds(1), Duration.ofSeconds(1)));
        String expired = service.issueTokenPair(this.details, ClientType.PC).block().accessToken();
        Thread.sleep(2200);

        StepVerifier.create(service.parse(expired))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject tokens whose payload was tampered with")
    void shouldRejectTamperedTokens() {
        JwtService service = new JwtService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.issueTokenPair(this.details, ClientType.PC)
                        .map(pair -> tamper(pair.accessToken()))
                        .flatMap(service::parse))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject tokens signed with a different secret")
    void shouldRejectForeignSignatures() {
        JwtService issuer = new JwtService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        JwtService verifier = new JwtService(properties("another-secret-value",
                Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(issuer.issueTokenPair(this.details, ClientType.PC)
                        .flatMap(pair -> verifier.parse(pair.accessToken())))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
    }

    @Test
    @DisplayName("should reject garbage token strings")
    void shouldRejectGarbageTokens() {
        JwtService service = new JwtService(properties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));

        StepVerifier.create(service.parse("not-a-jwt"))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();
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
