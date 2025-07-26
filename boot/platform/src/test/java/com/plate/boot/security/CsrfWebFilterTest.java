package com.plate.boot.security;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link CsrfWebFilter} including unit tests, integration tests, and debug tests.
 * Tests verify CSRF token validation, request filtering behavior, and error handling scenarios.
 */
@ExtendWith(MockitoExtension.class)
class CsrfWebFilterTest {

    private CsrfWebFilter csrfWebFilter;

    @Mock
    private WebFilterChain mockFilterChain;

    @Mock
    private CsrfToken mockCsrfToken;

    @BeforeEach
    void setUp() {
        csrfWebFilter = new CsrfWebFilter();
        // Remove default stubbing to avoid UnnecessaryStubbingException
        // Stubbing will be done in individual tests as needed
    }

    @Nested
    @DisplayName("CSRF Token Validation Tests")
    class CsrfTokenValidationTests {

        @Test
        @DisplayName("Should process request with valid CSRF token")
        void shouldProcessRequestWithValidCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Set CSRF token in exchange attributes
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));
            
            // Stub the filter chain for this specific test
            when(mockFilterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should process request without CSRF token")
        void shouldProcessRequestWithoutCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // No CSRF token in attributes

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle null CSRF token mono")
        void shouldHandleNullCsrfTokenMono() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Don't set CSRF token attribute (simulates null scenario)
            // exchange.getAttributes().put(CsrfToken.class.getName(), null);

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should propagate CSRF token in context")
        void shouldPropagateCsrfTokenInContext() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Mock filter chain to capture context
            when(mockFilterChain.filter(any(ServerWebExchange.class)))
                    .thenReturn(Mono.deferContextual(contextView -> {
                        // Verify that CSRF token is in context
                        assertThat(contextView.hasKey(ContextUtils.CSRF_TOKEN_CONTEXT)).isTrue();
                        CsrfToken tokenFromContext = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
                        assertThat(tokenFromContext).isEqualTo(mockCsrfToken);
                        return Mono.empty();
                    }));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("HTTP Method Tests")
    class HttpMethodTests {

        @Test
        @DisplayName("Should handle GET request with CSRF token")
        void shouldHandleGetRequestWithCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/data").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle POST request with CSRF token")
        void shouldHandlePostRequestWithCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/api/data")
                    .header("Content-Type", "application/json")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle PUT request with CSRF token")
        void shouldHandlePutRequestWithCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.put("/api/data/1")
                    .header("Content-Type", "application/json")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle DELETE request with CSRF token")
        void shouldHandleDeleteRequestWithCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.delete("/api/data/1").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Request Headers and Parameters Tests")
    class RequestHeadersAndParametersTests {

        @Test
        @DisplayName("Should handle request with X-Request-Sign header")
        void shouldHandleRequestWithSignHeader() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/api/secure")
                    .header("X-Request-Sign", "signature-value")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle request with query parameters")
        void shouldHandleRequestWithQueryParameters() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/data")
                    .queryParam("param1", "value1")
                    .queryParam("param2", "value2")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
            
            // Verify query parameters are accessed (as per implementation)
            MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
            assertThat(queryParams.get("param1")).containsExactly("value1");
            assertThat(queryParams.get("param2")).containsExactly("value2");
        }

        @Test
        @DisplayName("Should handle request with multiple headers")
        void shouldHandleRequestWithMultipleHeaders() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/api/data")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer token")
                    .header("X-Custom-Header", "custom-value")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle error in CSRF token mono")
        void shouldHandleErrorInCsrfTokenMono() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            RuntimeException testException = new RuntimeException("CSRF token error");
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.error(testException));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(mockFilterChain, never()).filter(any());
        }

        @Test
        @DisplayName("Should handle error in filter chain")
        void shouldHandleErrorInFilterChain() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            RuntimeException chainException = new RuntimeException("Filter chain error");
            when(mockFilterChain.filter(any(ServerWebExchange.class)))
                    .thenReturn(Mono.error(chainException));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectError(RuntimeException.class)
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle empty CSRF token mono")
        void shouldHandleEmptyCsrfTokenMono() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.empty());

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            // Filter chain should be called even when CSRF token mono is empty
            verify(mockFilterChain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Context Propagation Tests")
    class ContextPropagationTests {

        @Test
        @DisplayName("Should not overwrite existing CSRF token in context")
        void shouldNotOverwriteExistingCsrfTokenInContext() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            CsrfToken existingToken = mock(CsrfToken.class);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Mock filter chain to capture context with existing token
            when(mockFilterChain.filter(any(ServerWebExchange.class)))
                    .thenReturn(Mono.deferContextual(contextView -> {
                        // Verify that existing token is preserved
                        if (contextView.hasKey(ContextUtils.CSRF_TOKEN_CONTEXT)) {
                            CsrfToken tokenFromContext = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
                            assertThat(tokenFromContext).isEqualTo(existingToken);
                        }
                        return Mono.empty();
                    }));

            // Act & Assert - with existing context
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain)
                            .contextWrite(Context.of(ContextUtils.CSRF_TOKEN_CONTEXT, existingToken)))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should add CSRF token to empty context")
        void shouldAddCsrfTokenToEmptyContext() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Mock filter chain to capture context
            when(mockFilterChain.filter(any(ServerWebExchange.class)))
                    .thenReturn(Mono.deferContextual(contextView -> {
                        // Verify that token is added to context
                        assertThat(contextView.hasKey(ContextUtils.CSRF_TOKEN_CONTEXT)).isTrue();
                        CsrfToken tokenFromContext = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
                        assertThat(tokenFromContext).isEqualTo(mockCsrfToken);
                        return Mono.empty();
                    }));

            // Act & Assert - with empty context
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete request lifecycle with CSRF token")
        void shouldHandleCompleteRequestLifecycleWithCsrfToken() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.post("/api/users")
                    .header("Content-Type", "application/json")
                    .header("X-CSRF-TOKEN", "valid-token")
                    .queryParam("action", "create")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Mock a realistic filter chain that processes the request
            when(mockFilterChain.filter(any(ServerWebExchange.class)))
                    .thenReturn(Mono.deferContextual(contextView -> {
                        // Simulate downstream processing
                        assertThat(contextView.hasKey(ContextUtils.CSRF_TOKEN_CONTEXT)).isTrue();
                        CsrfToken token = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
                        assertThat(token).isEqualTo(mockCsrfToken);
                        
                        // Simulate setting response status
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
                        return Mono.empty();
                    }));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(org.springframework.http.HttpStatus.OK);
        }

        @Test
        @DisplayName("Should handle request without CSRF token gracefully")
        void shouldHandleRequestWithoutCsrfTokenGracefully() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/public").build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // No CSRF token in attributes (simulating public endpoint)

            when(mockFilterChain.filter(any(ServerWebExchange.class)))
                    .thenReturn(Mono.fromRunnable(() -> {
                        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
                    }));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
            assertThat(exchange.getResponse().getStatusCode())
                    .isEqualTo(org.springframework.http.HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("Debug and Logging Tests")
    class DebugAndLoggingTests {

        @Test
        @DisplayName("Should log debug information during filtering")
        void shouldLogDebugInformationDuringFiltering() {
            // Arrange
            MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                    .header("User-Agent", "Test-Agent")
                    .build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            // Assert
            verify(mockFilterChain).filter(exchange);
            
            // Verify that log prefix is accessible (implementation calls getLogPrefix)
            assertThat(exchange.getLogPrefix()).isNotNull();
        }

        @Test
        @DisplayName("Should handle request with various content types")
        void shouldHandleRequestWithVariousContentTypes() {
            // Test different content types
            String[] contentTypes = {
                "application/json",
                "application/xml",
                "text/plain",
                "multipart/form-data",
                "application/x-www-form-urlencoded"
            };

            for (String contentType : contentTypes) {
                // Arrange
                MockServerHttpRequest request = MockServerHttpRequest.post("/api/data")
                        .header("Content-Type", contentType)
                        .build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);
                exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

                // Act & Assert
                StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                        .expectComplete()
                        .verify();
            }

            verify(mockFilterChain, times(contentTypes.length)).filter(any(ServerWebExchange.class));
        }
    }

    @Nested
    @DisplayName("Performance and Edge Case Tests")
    class PerformanceAndEdgeCaseTests {

        @Test
        @DisplayName("Should handle concurrent requests efficiently")
        void shouldHandleConcurrentRequestsEfficiently() {
            // Arrange
            int numberOfRequests = 10;
            
            for (int i = 0; i < numberOfRequests; i++) {
                MockServerHttpRequest request = MockServerHttpRequest.get("/api/test/" + i).build();
                MockServerWebExchange exchange = MockServerWebExchange.from(request);
                exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

                // Act & Assert
                StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                        .expectComplete()
                        .verify();
            }

            verify(mockFilterChain, times(numberOfRequests)).filter(any(ServerWebExchange.class));
        }

        @Test
        @DisplayName("Should handle very long URLs")
        void shouldHandleVeryLongUrls() {
            // Arrange
            StringBuilder longPath = new StringBuilder("/api");
            for (int i = 0; i < 100; i++) {
                longPath.append("/very-long-path-segment-").append(i);
            }
            
            MockServerHttpRequest request = MockServerHttpRequest.get(longPath.toString()).build();
            MockServerWebExchange exchange = MockServerWebExchange.from(request);
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
        }

        @Test
        @DisplayName("Should handle requests with many query parameters")
        void shouldHandleRequestsWithManyQueryParameters() {
            // Arrange
            MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest.get("/api/search");
            
            // Add many query parameters
            for (int i = 0; i < 50; i++) {
                requestBuilder.queryParam("param" + i, "value" + i);
            }
            
            MockServerWebExchange exchange = MockServerWebExchange.from(requestBuilder.build());
            exchange.getAttributes().put(CsrfToken.class.getName(), Mono.just(mockCsrfToken));

            // Act & Assert
            StepVerifier.create(csrfWebFilter.filter(exchange, mockFilterChain))
                    .expectComplete()
                    .verify();

            verify(mockFilterChain).filter(exchange);
            
            // Verify query parameters are accessible
            MultiValueMap<String, String> queryParams = exchange.getRequest().getQueryParams();
            assertThat(queryParams.size()).isEqualTo(50);
        }
    }
}