package com.platform.boot.commons;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Error response wrapper class
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public @Data(staticConstructor = "of") class ErrorResponse implements Serializable {

    private final String requestId;

    private final String path;

    private final Integer code;

    private final String message;

    private final Object errors;

    private LocalDateTime time = LocalDateTime.now();
}