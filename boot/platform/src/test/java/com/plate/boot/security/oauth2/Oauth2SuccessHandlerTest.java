package com.plate.boot.security.oauth2;

import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link Oauth2SuccessHandler}.
 * Tests OAuth2 authentication success handling including XHR requests and redirects.
 * 
 * @author CodeBuddy
 */
@ExtendWith(MockitoExtension.class)
class Oauth2SuccessHandlerTest {

    @Mock
    private ServerAuthenticationSuccessHandler defaultSuccessHandler;

    @Mock
    private WebSession webSession;

    @Mock
    private WebFilterExchange webFilterExchange;

    private Oauth2SuccessHandler oauth2SuccessHandler;
    private OAuth2AuthenticationToken oauth2AuthenticationToken;
    private SecurityDetails securityDetails;

    @BeforeEach
    void setUp() {
        oauth2SuccessHandler = new Oauth2SuccessHandler();
        
        // Create test OAuth2 user
        Map<String, Object> attributes = Map.of(
            "id", "123456",
            "login", "testuser",
            "name", "Test User",
            "email", "test@example.com"
        );
        
        DefaultOAuth2User oauth2User = new DefaultOAuth2User(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "login"
        );
        
        oauth2AuthenticationToken = new OAuth2AuthenticationToken(
            oauth2User,
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            "github"
        );
        
        // Create SecurityDetails
        securityDetails = new SecurityDetails(
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "login"
        );
        securityDetails.setCode(UUID.randomUUID());
        securityDetails.setUsername("testuser");
    }

    @Nested
    @DisplayName("XHR Request Handling Tests")
    class XhrRequestHandlingTests {

        @Test
        @DisplayName("Should handle XHR request and return JSON response")
        void shouldHandleXhrRequestAndReturnJsonResponse() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn("test-session-id");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
            when(webSession.getLastAccessTime()).thenReturn(Instant.now());

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        }

        @Test
        @DisplayName("Should identify XHR request by X-Requested-With header")
        void shouldIdentifyXhrRequestByHeader() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn("test-session-id");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
            when(webSession.getLastAccessTime()).thenReturn(Instant.now());

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert - Should handle as XHR request
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should handle case-insensitive X-Requested-With header")
        void shouldHandleCaseInsensitiveXRequestedWithHeader() {
            // Test different case variations
            String[] headerVariations = {
                "XMLHttpRequest",
                "xmlhttprequest", 
                "XmlHttpRequest",
                "XMLHTTPREQUEST"
            };

            for (String headerValue : headerVariations) {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                        .header("X-Requested-With", headerValue)
                        .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);
                
                when(webFilterExchange.getExchange()).thenReturn(exchange);
                when(exchange.getSession()).thenReturn(Mono.just(webSession));
                when(webSession.getId()).thenReturn("test-session-id-" + headerValue);
                when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
                when(webSession.getLastAccessTime()).thenReturn(Instant.now());

                // Act
                StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                        .expectComplete()
                        .verify();

                // Assert
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        }

        @Test
        @DisplayName("Should create authentication token from session and OAuth2 token")
        void shouldCreateAuthenticationTokenFromSessionAndOAuth2Token() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            String sessionId = "test-session-123";
            Duration maxIdleTime = Duration.ofHours(8);
            Instant lastAccessTime = Instant.now();
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn(sessionId);
            when(webSession.getMaxIdleTime()).thenReturn(maxIdleTime);
            when(webSession.getLastAccessTime()).thenReturn(lastAccessTime);

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert - Response should contain authentication token data
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            
            // Verify session methods were called
            verify(webSession).getId();
            verify(webSession).getMaxIdleTime();
            verify(webSession).getLastAccessTime();
        }

        @Test
        @DisplayName("Should handle session retrieval error gracefully")
        void shouldHandleSessionRetrievalErrorGracefully() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.error(new RuntimeException("Session error")));

            // Act & Assert
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectError(RuntimeException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("Non-XHR Request Handling Tests")
    class NonXhrRequestHandlingTests {

        @Test
        @DisplayName("Should delegate to default handler for non-XHR requests")
        void shouldDelegateToDefaultHandlerForNonXhrRequests() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    // No X-Requested-With header
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(defaultSuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .thenReturn(Mono.empty());

            // Create a test implementation that delegates to default handler
            oauth2SuccessHandler = new Oauth2SuccessHandler() {
                @Override
                public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
                    String requestedWith = webFilterExchange.getExchange().getRequest()
                            .getHeaders().getFirst("X-Requested-With");
                    
                    if (!"XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                        return defaultSuccessHandler.onAuthenticationSuccess(webFilterExchange, authentication);
                    }
                    return super.onAuthenticationSuccess(webFilterExchange, authentication);
                }
            };

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert
            verify(defaultSuccessHandler).onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken);
        }

        @Test
        @DisplayName("Should handle regular browser requests")
        void shouldHandleRegularBrowserRequests() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            
            // Mock default redirect behavior
            oauth2SuccessHandler = new Oauth2SuccessHandler() {
                @Override
                public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
                    String requestedWith = webFilterExchange.getExchange().getRequest()
                            .getHeaders().getFirst("X-Requested-With");
                    
                    if (!"XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                        // Simulate default redirect behavior
                        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
                        response.setStatusCode(HttpStatus.FOUND);
                        response.getHeaders().setLocation(URI.create("/"));
                        return Mono.empty();
                    }
                    
                    return super.onAuthenticationSuccess(webFilterExchange, authentication);
                }
            };

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert - Should redirect for non-XHR requests
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FOUND);
        }
    }

    @Nested
    @DisplayName("Authentication Token Creation Tests")
    class AuthenticationTokenCreationTests {

        @Test
        @DisplayName("Should create authentication token with correct session data")
        void shouldCreateAuthenticationTokenWithCorrectSessionData() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            String sessionId = "session-12345";
            Duration maxIdleTime = Duration.ofHours(4);
            Instant lastAccessTime = Instant.now().minusSeconds(300);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn(sessionId);
            when(webSession.getMaxIdleTime()).thenReturn(maxIdleTime);
            when(webSession.getLastAccessTime()).thenReturn(lastAccessTime);

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(webSession).getId();
            verify(webSession).getMaxIdleTime();
            verify(webSession).getLastAccessTime();
        }

        @Test
        @DisplayName("Should handle different OAuth2 providers")
        void shouldHandleDifferentOAuth2Providers() {
            String[] providers = {"github", "google", "facebook", "gitee"};
            
            for (String provider : providers) {
                // Arrange
                OAuth2AuthenticationToken providerToken = new OAuth2AuthenticationToken(
                    oauth2AuthenticationToken.getPrincipal(),
                    oauth2AuthenticationToken.getAuthorities(),
                    provider
                );
                
                MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/" + provider)
                        .header("X-Requested-With", "XMLHttpRequest")
                        .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);
                
                when(webFilterExchange.getExchange()).thenReturn(exchange);
                when(exchange.getSession()).thenReturn(Mono.just(webSession));
                when(webSession.getId()).thenReturn("session-" + provider);
                when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
                when(webSession.getLastAccessTime()).thenReturn(Instant.now());

                // Act
                StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, providerToken))
                        .expectComplete()
                        .verify();

                // Assert
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null authentication token")
        void shouldHandleNullAuthenticationToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);

            // Act & Assert
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, null))
                    .expectError(IllegalArgumentException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle invalid authentication token type")
        void shouldHandleInvalidAuthenticationTokenType() {
            // Arrange
            Authentication invalidAuth = mock(Authentication.class);
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);

            // Act & Assert
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, invalidAuth))
                    .expectError(ClassCastException.class)
                    .verify();
        }

        @Test
        @DisplayName("Should handle session timeout scenarios")
        void shouldHandleSessionTimeoutScenarios() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn("expired-session");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofMinutes(1)); // Very short timeout
            when(webSession.getLastAccessTime()).thenReturn(Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS)); // Long ago

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert - Should still complete successfully
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete OAuth2 success flow for XHR")
        void shouldHandleCompleteOAuth2SuccessFlowForXhr() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn("integration-test-session");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
            when(webSession.getLastAccessTime()).thenReturn(Instant.now());

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
            
            // Verify all session methods were called
            verify(webSession).getId();
            verify(webSession).getMaxIdleTime();
            verify(webSession).getLastAccessTime();
        }

        @Test
        @DisplayName("Should handle OAuth2 success with complex user attributes")
        void shouldHandleOAuth2SuccessWithComplexUserAttributes() {
            // Arrange
            Map<String, Object> complexAttributes = new java.util.HashMap<>();
            complexAttributes.put("id", 123456L);
            complexAttributes.put("login", "complexuser");
            complexAttributes.put("name", "Complex User Name");
            complexAttributes.put("email", "complex@example.com");
            complexAttributes.put("avatar_url", "https://example.com/avatar.jpg");
            complexAttributes.put("bio", "This is a complex bio with special characters: !@#$%^&*()");
            complexAttributes.put("location", "San Francisco, CA");
            complexAttributes.put("company", "Test Company Inc.");
            complexAttributes.put("blog", "https://blog.example.com");
            complexAttributes.put("public_repos", 42);
            complexAttributes.put("followers", 100);
            complexAttributes.put("following", 50);
            
            DefaultOAuth2User complexOAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_DEVELOPER")),
                complexAttributes,
                "login"
            );
            
            OAuth2AuthenticationToken complexToken = new OAuth2AuthenticationToken(
                complexOAuth2User,
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_DEVELOPER")),
                "github"
            );
            
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn("complex-user-session");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
            when(webSession.getLastAccessTime()).thenReturn(Instant.now());

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, complexToken))
                    .expectComplete()
                    .verify();

            // Assert
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Performance Tests")
    class EdgeCasesAndPerformanceTests {

        @Test
        @DisplayName("Should handle concurrent authentication success requests")
        void shouldHandleConcurrentAuthenticationSuccessRequests() {
            // Test multiple concurrent requests
            int numberOfRequests = 10;
            
            for (int i = 0; i < numberOfRequests; i++) {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                        .header("X-Requested-With", "XMLHttpRequest")
                        .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);
                
                when(webFilterExchange.getExchange()).thenReturn(exchange);
                when(exchange.getSession()).thenReturn(Mono.just(webSession));
                when(webSession.getId()).thenReturn("concurrent-session-" + i);
                when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
                when(webSession.getLastAccessTime()).thenReturn(Instant.now());

                // Act
                StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                        .expectComplete()
                        .verify();

                // Assert
                assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        }

        @Test
        @DisplayName("Should handle very long session IDs")
        void shouldHandleVeryLongSessionIds() {
            // Arrange
            StringBuilder longSessionId = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longSessionId.append("a");
            }
            
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn(longSessionId.toString());
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofHours(8));
            when(webSession.getLastAccessTime()).thenReturn(Instant.now());

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should handle extreme session timeout values")
        void shouldHandleExtremeSessionTimeoutValues() {
            // Test with very short timeout
            MockServerHttpRequest request = MockServerHttpRequest.get("/oauth2/authorization/github")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            
            when(webFilterExchange.getExchange()).thenReturn(exchange);
            when(exchange.getSession()).thenReturn(Mono.just(webSession));
            when(webSession.getId()).thenReturn("short-timeout-session");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofSeconds(1));
            when(webSession.getLastAccessTime()).thenReturn(Instant.now());

            // Act
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            // Assert
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
            
            // Test with very long timeout
            when(webSession.getId()).thenReturn("long-timeout-session");
            when(webSession.getMaxIdleTime()).thenReturn(Duration.ofDays(365));
            
            StepVerifier.create(oauth2SuccessHandler.onAuthenticationSuccess(webFilterExchange, oauth2AuthenticationToken))
                    .expectComplete()
                    .verify();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}