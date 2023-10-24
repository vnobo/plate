package com.platform.boot.commons.annotation;

import com.google.common.collect.Lists;
import com.platform.boot.commons.ErrorResponse;
import com.platform.boot.commons.annotation.exception.ClientException;
import com.platform.boot.commons.annotation.exception.RestServerException;
import io.r2dbc.spi.R2dbcException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * This class provides global exception handling for the application.
 * It handles various types of exceptions and returns an appropriate error response.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class);

    /**
     * Handles exceptions thrown when there is an error in the input provided by the client.
     *
     * @param exchange the server web exchange
     * @param ex       the exception thrown
     * @return a response entity with an error response
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleBindException(ServerWebExchange exchange, ServerWebInputException ex) {
        List<String> errors = Lists.newArrayList();
        // Check if exception is WebExchangeBindException
        if (ex instanceof WebExchangeBindException bindException) {
            for (ObjectError objectError : bindException.getBindingResult().getAllErrors()) {
                errors.add("Error object %s message %s.".formatted(objectError.getObjectName(),
                        objectError.getDefaultMessage()));
            }
        } else {
            // Add cause and reason to errors list
            errors.add("Cause message %s.".formatted(ex.getCause().getMessage()));
            errors.add("Exception reason %s".formatted(ex.getReason()));
        }
        // Log error
        log.error("[%s] 请求参数验证失败! 信息: %s".formatted(exchange.getLogPrefix(), errors));
        if (log.isDebugEnabled()) {
            log.error("Server error", ex);
        }
        // Return response entity
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        4070, ex.getMessage(), errors));
    }


    /**
     * Handles exceptions thrown when there is an error in the database operation.
     *
     * @param exchange the server web exchange
     * @param ex       the exception thrown
     * @return a response entity with an error response
     */
    @ExceptionHandler({DataAccessException.class, R2dbcException.class})
    public ResponseEntity<ErrorResponse> handleFailureException(ServerWebExchange exchange, RuntimeException ex) {
        List<String> errors = Lists.newArrayList();
        if (ex instanceof R2dbcException r2dbcException) {
            errors.add(r2dbcException.getMessage());
            errors.add(r2dbcException.getSql());
            errors.add(r2dbcException.getSqlState());
        } else if (ex instanceof BadSqlGrammarException grammarException) {
            errors.add(grammarException.getMessage());
            errors.add(grammarException.getSql());
        } else {
            errors.add(ex.getLocalizedMessage());
        }
        log.error("[%S] 数据库操作错误! 信息: %S".formatted(exchange.getLogPrefix(), errors));
        if (log.isDebugEnabled()) {
            log.error("Server error", ex);
        }
        return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        5070, ex.getMessage(), errors));
    }

    /**
     * Handles exceptions thrown when there is an error in the client request.
     *
     * @param exchange the server web exchange
     * @param ex       the exception thrown
     * @return a response entity with an error response
     */
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<ErrorResponse> handleClientException(ServerWebExchange exchange, ClientException ex) {
        log.error("[%s] 内部服务访问错误! 信息: %s".formatted(exchange.getLogPrefix(), ex.getMessage()));
        if (log.isDebugEnabled()) {
            log.error("Server error", ex);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        ex.getCode(), ex.getMessage(), ex.getMsg()));
    }

    /**
     * Handles exceptions thrown when there is an error in the server.
     *
     * @param exchange the server web exchange
     * @param ex       the exception thrown
     * @return a response entity with an error response
     */
    @ExceptionHandler(RestServerException.class)
    public ResponseEntity<ErrorResponse> handleRestServerException(ServerWebExchange exchange, RestServerException ex) {
        log.error("[%s] 服务器自定义错误. 信息: %s".formatted(exchange.getLogPrefix(), ex.getLocalizedMessage()));
        if (log.isDebugEnabled()) {
            log.error("Server error", ex);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        ex.getCode(), ex.getMessage(), ex.getMsg()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(ServerWebExchange exchange, Exception ex) {
        log.error("[%s] 服务器自定义错误. 信息: %s".formatted(exchange.getLogPrefix(), ex.getLocalizedMessage()));
        if (log.isDebugEnabled()) {
            log.error("Server error", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        5000, ex.getMessage(), ex));
    }

}