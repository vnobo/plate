package com.plate.boot.commons;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.lang.reflect.Method;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GlobalExceptionHandler} (no Spring / container required).
 * <p>
 * Each handler method is exercised with a mocked {@link ServerWebExchange} so that the request
 * URI, request headers, and the un-committed response can be controlled without a live server.
 * The {@code protected} {@code handleWebExchangeBindException} and {@code handleServerWebInputException}
 * methods are reachable because this test lives in the same package as the handler.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDataAccessExceptionUsesCauseDetailWhenCausePresent() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/users"));
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException(
                "db fail", new IllegalArgumentException("fk constraint violated"));

        ResponseEntity<Object> response = handler.handleDataAccessException(ex, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Bad Sql Grammar Data Access Exception");
        assertThat(body.getDetail()).isEqualTo("fk constraint violated");
        assertThat(body.getType()).isEqualTo(URI.create("/api/users"));
    }

    @Test
    void handleDataAccessExceptionFallsBackToOwnMessageWhenCauseAbsent() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/users"));
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("db fail");

        ResponseEntity<Object> response = handler.handleDataAccessException(ex, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Bad Sql Grammar Data Access Exception");
        assertThat(body.getDetail()).isEqualTo("db fail");
        assertThat(body.getType()).isEqualTo(URI.create("/api/users"));
    }

    @Test
    void handleRuntimeExceptionBuildsServerErrorProblemDetail() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/orders"));
        RuntimeException ex = new RuntimeException("unexpected boom");

        ResponseEntity<Object> response = handler.handleRuntimeException(ex, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Runtime Server Error Exception");
        assertThat(body.getDetail()).isEqualTo("unexpected boom");
        assertThat(body.getType()).isEqualTo(URI.create("/api/orders"));
    }

    @Test
    void handleWebExchangeBindExceptionJoinsFieldErrors() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/users"));
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "user");
        binding.addError(new FieldError("user", "name", "must not be blank"));
        binding.addError(new FieldError("user", "age", "must be positive"));
        WebExchangeBindException ex = new WebExchangeBindException(methodParameter(), binding);

        ResponseEntity<Object> response = handler.handleWebExchangeBindException(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Bad Request Bind Params Error");
        assertThat(body.getDetail()).isEqualTo("must not be blank,must be positive");
        assertThat(body.getType()).isEqualTo(URI.create("/api/users"));
    }

    @Test
    void handleWebExchangeBindExceptionWithoutErrorsProducesEmptyDetail() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/users"));
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "user");
        WebExchangeBindException ex = new WebExchangeBindException(methodParameter(), binding);

        ResponseEntity<Object> response = handler.handleWebExchangeBindException(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Bad Request Bind Params Error");
        assertThat(body.getDetail()).isEmpty();
        assertThat(body.getType()).isEqualTo(URI.create("/api/users"));
    }

    @Test
    void handleServerWebInputExceptionUsesCauseDetailWhenCausePresent() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/users"));
        ServerWebInputException ex = new ServerWebInputException(
                "Malformed JSON", null, new IllegalArgumentException("Unexpected token"));

        ResponseEntity<Object> response = handler.handleServerWebInputException(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Bad Request Server Input Error");
        assertThat(body.getDetail()).isEqualTo("Unexpected token");
        assertThat(body.getType()).isEqualTo(URI.create("/api/users"));
    }

    @Test
    void handleServerWebInputExceptionFallsBackToOwnMessageWhenCauseAbsent() {
        ServerWebExchange exchange = mockExchange(URI.create("/api/users"));
        ServerWebInputException ex = new ServerWebInputException("Malformed JSON");

        ResponseEntity<Object> response = handler.handleServerWebInputException(
                ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, exchange).block();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ProblemDetail body = (ProblemDetail) response.getBody();
        assertThat(body.getTitle()).isEqualTo("Bad Request Server Input Error");
        assertThat(body.getDetail()).isEqualTo("400 BAD_REQUEST \"Malformed JSON\"");
        assertThat(body.getType()).isEqualTo(URI.create("/api/users"));
    }

    private MethodParameter methodParameter() {
        try {
            Method method = GlobalExceptionHandler.class.getDeclaredMethod(
                    "handleDataAccessException", DataAccessException.class, ServerWebExchange.class);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private ServerWebExchange mockExchange(URI uri) {
        HttpHeaders headers = new HttpHeaders();

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getURI()).thenReturn(uri);
        when(request.getHeaders()).thenReturn(headers);

        ServerHttpResponse response = mock(ServerHttpResponse.class);
        when(response.isCommitted()).thenReturn(false);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        return exchange;
    }
}
