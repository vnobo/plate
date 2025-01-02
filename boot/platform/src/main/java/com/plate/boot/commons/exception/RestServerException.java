package com.plate.boot.commons.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ServerErrorException;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Represents a custom exception class for handling REST server errors, which includes an error message, a status code,
 * and additional details. This exception extends {@link RuntimeException} and implements {@link Serializable} to support
 * serialization when transmitted across networks or persisted.
 * <p>
 * It provides factory methods to conveniently create instances with predefined or custom error messages and codes,
 * facilitating standardization of error responses in a RESTful API context.
 *
 * <h3>Features:</h3>
 * <ul>
 *     <li>Custom error code to supplement HTTP status codes.</li>
 *     <li>Holds an arbitrary object ({@code msg}) for detailed error information.</li>
 *     <li>Convenience static factory methods for instantiation.</li>
 * </ul>
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RestServerException extends ServerErrorException {

    /**
     * Constructs a new RestServerException with the specified detail message and cause.
     *
     * @param reason the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param cause  the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public RestServerException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * Constructs a new RestServerException with the specified detail message, handler method, and cause.
     *
     * @param reason        the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param handlerMethod the method that caused the exception.
     * @param cause         the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public RestServerException(String reason, Method handlerMethod, Throwable cause) {
        super(reason, handlerMethod, cause);
    }

    /**
     * Constructs a new RestServerException with the specified detail message, method parameter, and cause.
     *
     * @param reason    the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param parameter the method parameter that caused the exception.
     * @param cause     the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public RestServerException(String reason, MethodParameter parameter, Throwable cause) {
        super(reason, parameter, cause);
    }

    /**
     * Creates a new RestServerException with the specified detail message and cause.
     *
     * @param reason the detail message.
     * @param cause  the cause.
     * @return a new instance of RestServerException.
     */
    public static RestServerException withMsg(String reason, Throwable cause) {
        var ex = new RestServerException(reason, cause);
        ex.setTitle(reason);
        ex.setDetail(cause.getMessage());
        ex.setStackTrace(cause.getStackTrace());
        return ex;
    }

    /**
     * Creates a new RestServerException with the specified detail message, handler method, and cause.
     *
     * @param reason        the detail message.
     * @param handlerMethod the method that caused the exception.
     * @param cause         the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @return a new instance of RestServerException.
     */
    public static RestServerException withMsg(String reason, Method handlerMethod, @Nullable Throwable cause) {
        return new RestServerException(reason, handlerMethod, cause);
    }

    /**
     * Creates a new RestServerException with the specified detail message, method parameter, and cause.
     *
     * @param reason    the detail message.
     * @param parameter the method parameter that caused the exception.
     * @param cause     the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     * @return a new instance of RestServerException.
     */
    public static RestServerException withMsg(String reason, MethodParameter parameter, Throwable cause) {
        return new RestServerException(reason, parameter, cause);
    }

}