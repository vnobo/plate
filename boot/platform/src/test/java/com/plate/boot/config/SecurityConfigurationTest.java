package com.plate.boot.config;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the JWT authentication entry point in {@link SecurityConfiguration}.
 */
class SecurityConfigurationTest {

    private static JsonMapper savedMapper;

    @BeforeAll
    static void setUpMapper() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDownMapper() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @Test
    void authenticationEntryPointWritesJsonUnauthorizedResponse() {
        SecurityConfiguration.CustomServerAuthenticationEntryPoint entryPoint =
                new SecurityConfiguration.CustomServerAuthenticationEntryPoint();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        StepVerifier.create(entryPoint.commence(exchange, exception)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("Authentication Failure");
    }
}
