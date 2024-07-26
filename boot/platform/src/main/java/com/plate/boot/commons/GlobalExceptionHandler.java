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

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

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
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        417, ex.getReason(), errors));
    }

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
        }
        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        507, ex.getLocalizedMessage(), errors));
    }

    @ExceptionHandler(RestServerException.class)
    public ResponseEntity<ErrorResponse> handleRestServerException(ServerWebExchange exchange, RestServerException ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        ex.getCode(), ex.getLocalizedMessage(), ex.getMsg()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(ServerWebExchange exchange, Exception ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        500, ex.getLocalizedMessage(), ex.getCause().getMessage()));
    }

}