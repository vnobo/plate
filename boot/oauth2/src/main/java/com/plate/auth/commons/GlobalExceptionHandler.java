package com.plate.auth.commons;

import com.google.common.collect.Lists;
import com.plate.auth.commons.exception.RestServerException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.hibernate.sql.exec.ExecutionException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataAccessException.class})
    public ResponseEntity<ErrorResponse> handleFailureException(HttpServletRequest exchange, RuntimeException ex) {
        List<String> errors = Lists.newArrayList("Database exec exception!");
        if (ex instanceof ExecutionException r2dbcException) {

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
                .body(ErrorResponse.of(exchange.getRequestId(), exchange.getServletPath(),
                        507, ex.getLocalizedMessage(), errors));
    }

    @ExceptionHandler(RestServerException.class)
    public ResponseEntity<ErrorResponse> handleRestServerException(HttpServletRequest exchange, RestServerException ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequestId(), exchange.getServletPath(),
                        ex.getCode(), ex.getLocalizedMessage(), ex.getMsg()));
    }

    @ExceptionHandler({AuthenticationException.class, AccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest exchange, AuthenticationException ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequestId(), exchange.getServletPath(),
                        500, ex.getLocalizedMessage(), ex.getCause().getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest exchange, Exception ex) {
        if (log.isDebugEnabled()) {
            log.error(ex.getLocalizedMessage(), ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequestId(), exchange.getServletPath(),
                        500, ex.getLocalizedMessage(), ex.getCause().getMessage()));
    }

}