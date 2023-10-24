package com.platform.boot.commons;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Error response wrapper class
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record ErrorResponse(String requestId, String path, Integer code,
                            String message, Object errors, LocalDateTime time) implements Serializable {
    public static ErrorResponse of(String requestId, String path, Integer code, String message, Object errors) {
        return new ErrorResponse(requestId, path, code, message, errors, LocalDateTime.now());
    }
}