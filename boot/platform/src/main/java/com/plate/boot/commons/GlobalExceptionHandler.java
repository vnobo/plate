package com.plate.boot.commons;

import com.plate.boot.commons.exception.RestServerException;
import io.r2dbc.spi.R2dbcException;
import lombok.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerErrorException;
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
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    /**
     * Handles exceptions of type {@link RestServerException} by creating an appropriate error response.
     * This method is designed to be used within a Spring MVC controller advice context to manage
     * exceptions that occur during the execution of RESTful server operations.
     *
     * @param exchange The current server web exchange containing request and response information.
     *                 This is used to extract details necessary for constructing the error response.
     * @param ex       The {@link RestServerException} instance that was thrown, encapsulating
     *                 error details such as error code, message, and any additional info.
     * @return A {@link ResponseEntity} with status {@link HttpStatus#INTERNAL_SERVER_ERROR},
     * content type set to {@link MediaType#APPLICATION_JSON}, and body containing
     * an {@link ErrorResponse} object representing the details of the exception.
     * The error response includes the request ID, the request path, the error code,
     * the localized error message, and any custom message provided by the exception.
     */
    @Override
    protected @NonNull Mono<ResponseEntity<Object>> handleServerErrorException(@NonNull ServerErrorException ex,
                                                                               @NonNull HttpHeaders headers,
                                                                               @NonNull HttpStatusCode status,
                                                                               @NonNull ServerWebExchange exchange) {
        if (logger.isDebugEnabled()) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
        return handleExceptionInternal(ex, null, headers, status, exchange);
    }

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
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("Bad Sql Grammar Data Access Exception");
        problemDetail.setType(exchange.getRequest().getURI());
        return handleExceptionInternal(ex, problemDetail, exchange.getRequest().getHeaders(),
                HttpStatus.INSUFFICIENT_STORAGE, exchange);
    }

    /**
     * Overrides the handleExceptionInternal method to provide custom exception handling.
     * This method logs the exception message if debug logging is enabled and then calls the superclass's
     * handleExceptionInternal method to handle the exception.
     *
     * @param ex       The exception that was thrown.
     * @param body     The body of the response.
     * @param headers  The headers of the response.
     * @param status   The HTTP status code of the response.
     * @param exchange The current server web exchange.
     * @return A Mono containing a ResponseEntity with the exception details.
     */
    @Override
    protected @NonNull Mono<ResponseEntity<Object>> handleExceptionInternal(@NonNull Exception ex, Object body,
                                                                            HttpHeaders headers,
                                                                            @NonNull HttpStatusCode status,
                                                                            @NonNull ServerWebExchange exchange) {
        if (logger.isDebugEnabled()) {
            logger.error(ex.getLocalizedMessage(), ex);
        }
        return super.handleExceptionInternal(ex, body, headers, status, exchange);
    }

}