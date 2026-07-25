package com.plate.boot.commons;

import com.plate.boot.commons.exception.RestServerException;
import io.r2dbc.spi.R2dbcException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Centralized, reactive exception handler that translates exceptions thrown by REST endpoints
 * into a consistent {@link org.springframework.http.ProblemDetail} response body.
 * <p>
 * Handled exception types:
 * <ul>
 *   <li>Binding/validation failures ({@link WebExchangeBindException}, via {@link ResponseEntityExceptionHandler}).</li>
 *   <li>Malformed request input ({@code ServerWebInputException}).</li>
 *   <li>Data access failures ({@link DataAccessException}).</li>
 *   <li>Any other uncaught {@link RuntimeException}.</li>
 * </ul>
 * Each handler returns a {@link ResponseEntity} whose body is a {@link ProblemDetail} carrying the
 * request URI, an error title, and the underlying message.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles {@link DataAccessException} by building a standardized {@link ProblemDetail} error response.
     *
     * @param ex       the {@link DataAccessException} that was thrown
     * @param exchange the current server web exchange, used to extract the request URI
     * @return a {@link Mono} with a {@link ResponseEntity} whose body is a {@link ProblemDetail}
     * describing the error (status {@link HttpStatus#INSUFFICIENT_STORAGE})
     */
    @ExceptionHandler(DataAccessException.class)
    public Mono<ResponseEntity<Object>> handleDataAccessException(DataAccessException ex, ServerWebExchange exchange) {
        if (logger.isDebugEnabled()) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCause().getLocalizedMessage());
        problemDetail.setTitle("Bad Sql Grammar Data Access Exception");
        problemDetail.setType(exchange.getRequest().getURI());
        return handleExceptionInternal(ex, problemDetail, exchange.getRequest().getHeaders(),
                HttpStatus.INSUFFICIENT_STORAGE, exchange);
    }

    /**
     * Handles any uncaught {@link RuntimeException} by building a standardized {@link ProblemDetail} error response.
     *
     * @param ex       the {@link RuntimeException} that was thrown
     * @param exchange the current server web exchange, used to extract the request URI
     * @return a {@link Mono} with a {@link ResponseEntity} whose body is a {@link ProblemDetail}
     * describing the error (status {@link HttpStatus#INSUFFICIENT_STORAGE})
     */
    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Object>> handleRuntimeException(RuntimeException ex, ServerWebExchange exchange) {
        if (logger.isDebugEnabled()) {
            logger.error(ex.getMessage(), ex);
        }
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        problemDetail.setTitle("Runtime Server Error Exception");
        problemDetail.setType(exchange.getRequest().getURI());
        return handleExceptionInternal(ex, problemDetail, exchange.getRequest().getHeaders(),
                HttpStatus.INSUFFICIENT_STORAGE, exchange);
    }

    /**
     * Customize the handling of {@link WebExchangeBindException}.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex       the exception to handle
     * @param headers  the headers to use for the response
     * @param status   the status code to use for the response
     * @param exchange the current request and response
     * @return a {@code Mono} with the {@code ResponseEntity} for the response
     */
    @Override
    protected Mono<ResponseEntity<Object>> handleWebExchangeBindException(WebExchangeBindException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatusCode status,
                                                                          ServerWebExchange exchange) {
        if (logger.isDebugEnabled()) {
            for (var err : ex.getAllErrors()) {
                logger.error("Bind Request Error! Field: " + err.getObjectName() + ",Error: " + err.getDefaultMessage());
            }
            logger.error(ex.getMessage(), ex);
        }
        var errMsg = ex.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, StringUtils.collectionToCommaDelimitedString(errMsg));
        problemDetail.setTitle("Bad Request Bind Params Error");
        problemDetail.setType(exchange.getRequest().getURI());
        return handleExceptionInternal(ex, problemDetail, headers, status, exchange);
    }

    /**
     * Handles {@link ServerWebInputException} (malformed request input) by building a
     * standardized {@link ProblemDetail} error response.
     *
     * @param ex       the {@link ServerWebInputException} that was thrown
     * @param headers  the headers to use for the response
     * @param status   the status code to use for the response
     * @param exchange the current server web exchange, used to extract the request URI
     * @return a {@link Mono} with a {@link ResponseEntity} whose body is a {@link ProblemDetail}
     * describing the malformed-input error (status {@link HttpStatus#BAD_REQUEST})
     */
    @Override
    protected Mono<ResponseEntity<Object>> handleServerWebInputException(
            ServerWebInputException ex, HttpHeaders headers, HttpStatusCode status,
            ServerWebExchange exchange) {

        if (logger.isDebugEnabled()) {
            logger.error(ex.getCause().getMessage(), ex);
        }
        ProblemDetail problemDetail = ProblemDetail
                .forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getCause().getLocalizedMessage());
        problemDetail.setTitle("Bad Request Server Input Error");
        problemDetail.setType(exchange.getRequest().getURI());
        return handleExceptionInternal(ex, problemDetail, headers, status, exchange);
    }
}