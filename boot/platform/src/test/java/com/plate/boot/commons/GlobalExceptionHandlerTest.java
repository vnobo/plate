package com.plate.boot.commons;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the authentication failure mapping of {@link GlobalExceptionHandler}.
 */
@DisplayName("GlobalExceptionHandler authentication mapping")
class GlobalExceptionHandlerTest {

    @Test
    @DisplayName("should map AuthenticationException to 401 with a problem detail body")
    void shouldMapAuthenticationExceptionToUnauthorized() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockServerWebExchange exchange =
                MockServerWebExchange.from(MockServerHttpRequest.post("/sec/v1/auth/login"));

        StepVerifier.create(handler.handleAuthenticationException(
                        new BadCredentialsException("Bad credentials"), exchange))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    ProblemDetail body = (ProblemDetail) response.getBody();
                    assertThat(body).isNotNull();
                    assertThat(body.getTitle()).isEqualTo("Authentication Failed");
                    assertThat(body.getDetail()).contains("Bad credentials");
                })
                .verifyComplete();
    }
}
