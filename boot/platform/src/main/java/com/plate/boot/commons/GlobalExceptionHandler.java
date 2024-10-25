package com.plate.boot.commons;

import com.google.common.collect.Lists;
import com.plate.boot.commons.exception.RestServerException;
import io.r2dbc.spi.R2dbcException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * GlobalExceptionHandler is a centralized exception handler that intercepts and processes
 * various exceptions thrown during the execution of REST endpoints within an application.
 * It provides tailored responses for different types of exceptions, ensuring consistent
 * and informative error messages are returned to the client.
 * <p>
 * Key Features:
 * - Handles {@link ServerWebInputException} to manage binding errors, including validation failures.
 * - Manages data access exceptions such as {@link DataAccessException} and {@link R2dbcException}.
 * - Custom exception handling for {@link RestServerException}, designed for application-specific errors.
 * - Generic exception handling for uncaught {@link Exception}s to maintain robustness.
 * <p>
 * Each exception handler method transforms the exception into a standardized {@link ErrorResponse}
 * format before returning it in the body of a {@link ResponseEntity} with the appropriate HTTP status code.
 */
@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions related to binding issues by creating an appropriate error response.
     * This method is specifically designed to process {@link ServerWebInputException} and its subclasses,
     * extracting error details to construct an {@link ErrorResponse} instance, which is then returned
     * as part of a {@link ResponseEntity} with a status of {@link HttpStatus#EXPECTATION_FAILED}.
     *
     * @param exchange The current server web exchange containing request/response details.
     * @param ex       The exception that has been thrown during the binding process, providing insights into the failure.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} which encapsulates
     * the error details including the request ID, request path, HTTP status code, reason for the failure,
     * and a list of error messages detailing the fields in error and their respective messages.
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleBindException(ServerWebExchange exchange, ServerWebInputException ex) {
        List<String> errors = Lists.newArrayList(ex.getReason());
        if (ex instanceof WebExchangeBindException bindException) {
            for (ObjectError objectError : bindException.getBindingResult().getAllErrors()) {
                errors.add("Error field: %s, msg: %s.".formatted(objectError.getObjectName(),
                        objectError.getDefaultMessage()));
            }
        } else {
            errors.add("Exception reason %s".formatted(ex.getReason()));
            errors.add("Cause message %s.".formatted(ex.getCause().getMessage()));
        }
        if (log.isDebugEnabled()) {
            log.error(ex.getReason(), ex);
        }
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), HttpStatus.BAD_REQUEST.value(),
                        exchange.getRequest().getPath().value(), ex.getReason(), errors));
    }


    /**
     * Handles specific types of failure exceptions by creating an appropriate error response.
     * This method is designed to catch and process {@link DataAccessException} and {@link R2dbcException},
     * extracting useful information for logging and client response.
     *
     * @param exchange The current server web exchange containing the request and response details.
     * @param ex       The runtime exception that has been caught, which should be either a
     *                 {@link DataAccessException} or an {@link R2dbcException}.
     * @return A {@link ResponseEntity} containing an {@link ErrorResponse} with details about the failure,
     * including a HTTP status of 507 (INSUFFICIENT_STORAGE), application/json content type,
     * and specifics of the error extracted from the exception and exchange.
     */
    @ExceptionHandler({DataAccessException.class, R2dbcException.class})
    public ResponseEntity<ErrorResponse> handleFailureException(ServerWebExchange exchange, RuntimeException ex) {
        List<String> errors = Lists.newArrayList("Database exec exception!");
        if (ex instanceof R2dbcException r2dbcException) {
            errors.add(r2dbcException.getSql());
            errors.add(r2dbcException.getSqlState());
            errors.add(r2dbcException.getMessage());
        } else if (ex instanceof BadSqlGrammarException grammarException) {
            errors.add(grammarException.getMessage());
            errors.add(grammarException.getSql());
        } else {
            errors.add(ex.getLocalizedMessage());
        }
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
            log.error(ex.getCause().getMessage(), ex.getCause());
        }
        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), HttpStatus.INSUFFICIENT_STORAGE.value(),
                        exchange.getRequest().getPath().value(), ex.getLocalizedMessage(), errors));
    }

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
    @ExceptionHandler(RestServerException.class)
    public ResponseEntity<ErrorResponse> handleRestServerException(ServerWebExchange exchange, RestServerException ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), ex.getStatusCode().value(),
                        exchange.getRequest().getPath().value(), ex.getLocalizedMessage(), ex.getErrors()));
    }

    /**
     * Handles exceptions of type {@link IllegalArgumentException} by creating an appropriate error response.
     * This method is designed to be used within a Spring MVC controller advice context to manage
     * exceptions that occur during the execution of RESTful server operations.
     *
     * @param exchange The current server web exchange containing request and response information.
     *                 This is used to extract details necessary for constructing the
     *                 error response.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(ServerWebExchange exchange,
                                                                        IllegalArgumentException ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), HttpStatus.BAD_REQUEST.value(),
                        exchange.getRequest().getPath().value(), ex.getLocalizedMessage(), ex.getStackTrace()));
    }


}