package com.platform.boot.commons;

import com.google.common.collect.Lists;
import com.platform.boot.commons.exception.RestServerException;
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
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Log log = LogFactory.getLog(GlobalExceptionHandler.class);

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ErrorResponse> handleBindException(ServerWebExchange exchange, ServerWebInputException ex) {
        List<String> errors = Lists.newArrayList(ex.getLocalizedMessage());
        if (ex instanceof WebExchangeBindException bindException) {
            for (ObjectError objectError : bindException.getBindingResult().getAllErrors()) {
                errors.add("Error object %s message %s.".formatted(objectError.getObjectName(),
                        objectError.getDefaultMessage()));
            }
        } else {
            errors.add("Cause message %s.".formatted(ex.getCause().getMessage()));
            errors.add("Exception reason %s".formatted(ex.getReason()));
        }
        log.error("%s请求参数验证失败! 信息: %s".formatted(exchange.getLogPrefix(), ex.getMessage()));
        if (log.isDebugEnabled()) {
            log.error("请求参数验证失败!", ex);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        4170, "请求参数验证失败!", errors));
    }

    @ExceptionHandler({DataAccessException.class, R2dbcException.class})
    public ResponseEntity<ErrorResponse> handleFailureException(ServerWebExchange exchange, RuntimeException ex) {
        List<String> errors = Lists.newArrayList(ex.getLocalizedMessage());
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
        log.error("%s数据库操作错误! 信息: %S".formatted(exchange.getLogPrefix(), ex.getMessage()));
        if (log.isDebugEnabled()) {
            log.error("数据库操作错误!", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        5070, "数据库操作错误!", errors));
    }

    @ExceptionHandler(RestServerException.class)
    public ResponseEntity<ErrorResponse> handleRestServerException(ServerWebExchange exchange, RestServerException ex) {
        log.error("%s服务器自定义错误! 信息: %s".formatted(exchange.getLogPrefix(), ex.getMessage()));
        if (log.isDebugEnabled()) {
            log.error("服务器自定义错误!", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        ex.getCode(), "服务自定义错误!", ex.getMsg()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(ServerWebExchange exchange, Exception ex) {
        log.error("%s服务器未知错误! 信息: %s".formatted(exchange.getLogPrefix(), ex.getMessage()));
        if (log.isDebugEnabled()) {
            log.error("服务器未知错误!", ex);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                        5000, "服务未知错误!", ex.getMessage()));
    }

}