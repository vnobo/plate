package com.plate.boot.commons;

import com.plate.boot.commons.exception.RestServerException;
import io.r2dbc.spi.R2dbcException;
import lombok.NonNull;
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
import reactor.core.publisher.Mono;

/**
 * GlobalExceptionHandler is a centralized exception handler that intercepts and processes
 * various exceptions thrown during the execution of REST endpoints within an application.
 * It provides tailored responses for different types of exceptions, ensuring consistent
 * and informative error messages are returned to the client.
 * <p>
 * Key Features:
 * - Handles {@link ResponseEntityExceptionHandler} to manage binding errors, including validation failures.
 * - Manages data access exceptions such as {@link DataAccessException} and {@link R2dbcException}.
 * - Custom exception handling for {@link RestServerException}, designed for application-specific errors.
 * - Generic exception handling for uncaught {@link Exception}s to maintain robustness.
 * <p>
 * Each exception handler method transforms the exception into a standardized {@link ErrorResponse}
 * format before returning it in the body of a {@link ResponseEntity} with the appropriate HTTP status code.
 */
@ControllerAdvice
public class GlobalRespEntityExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handles exceptions of type {@link DataAccessException} by creating an appropriate error response.
     * This method is designed to be used within a Spring MVC controller advice context to manage
     * exceptions that occur during the execution of RESTful server operations.
     *
     * @param ex       The {@link DataAccessException} instance that was thrown, encapsulating
     *                 error details such as error code, message, and any additional info.
     * @param exchange The current server web exchange containing request and response information.
     *                 This is used to extract details necessary for constructing the error response.
     * @return A {@link Mono} containing a {@link ResponseEntity} with status {@link HttpStatus#INSUFFICIENT_STORAGE},
     * content type set to {@link MediaType#APPLICATION_JSON}, and body containing
     * a {@link ProblemDetail} object representing the details of the exception.
     * The error response includes the request URI, the error message, and a custom title.
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
    protected @NonNull Mono<ResponseEntity<Object>> handleWebExchangeBindException(@NonNull WebExchangeBindException ex,
                                                                                   @NonNull HttpHeaders headers,
                                                                                   @NonNull HttpStatusCode status,
                                                                                   @NonNull ServerWebExchange exchange) {
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
        problemDetail.setTitle("Bad Request Bind Params Error!");
        problemDetail.setType(exchange.getRequest().getURI());
        return handleExceptionInternal(ex, problemDetail, headers, status, exchange);
    }
}