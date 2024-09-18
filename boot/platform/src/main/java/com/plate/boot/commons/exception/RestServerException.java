package com.plate.boot.commons.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.io.Serializable;

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
@Data
@EqualsAndHashCode(callSuper = true)
public class RestServerException extends RuntimeException implements Serializable {

    protected final HttpStatusCode statusCode;
    protected final Object errors;

    public RestServerException(HttpStatusCode code, String message, Object msg) {
        super(message);
        this.statusCode = code;
        this.errors = msg;
    }

    public RestServerException(String message, Throwable throwable) {
        this(HttpStatus.INTERNAL_SERVER_ERROR, message, throwable.fillInStackTrace());
    }

    public static RestServerException withMsg(String message, Object errors) {
        return withMsg(HttpStatusCode.valueOf(500), message, errors);
    }

    public static RestServerException withMsg(HttpStatusCode code, String message, Object errors) {
        return new RestServerException(code, message, errors);
    }

}