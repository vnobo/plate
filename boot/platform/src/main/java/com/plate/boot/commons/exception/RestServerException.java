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


    public RestServerException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public RestServerException(String reason, Method handlerMethod, Throwable cause) {
        super(reason, handlerMethod, cause);
    }

    public RestServerException(String reason, MethodParameter parameter, Throwable cause) {
        super(reason, parameter, cause);
    }

    public static RestServerException withMsg(String reason, Throwable cause) {
        var ex = new RestServerException(reason, cause);
        ex.setTitle(reason);
        ex.setDetail(cause.getMessage());
        ex.setStackTrace(cause.getStackTrace());
        return ex;
    }

    public static RestServerException withMsg(String reason, Method handlerMethod, @Nullable Throwable cause) {
        return new RestServerException(reason, handlerMethod, cause);
    }

    public static RestServerException withMsg(String reason, MethodParameter parameter, Throwable cause) {
        return new RestServerException(reason, parameter, cause);
    }

}