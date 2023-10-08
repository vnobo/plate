package com.platform.boot.commons;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Error response wrapper class
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public @Data(staticConstructor = "of") class ErrorResponse implements Serializable {
    /**
     * Request ID
     */
    private final String requestId;
    /**
     * Request path
     */
    private final String path;
    /**
     * Error code
     */
    private final Integer code;
    /**
     * Error message
     */
    private final String message;
    /**
     * Error details
     */
    private final Object errors;
    /**
     * Timestamp
     */
    private LocalDateTime time = LocalDateTime.now();
}