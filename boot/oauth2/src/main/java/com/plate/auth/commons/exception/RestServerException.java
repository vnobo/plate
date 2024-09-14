// Define the package name
package com.plate.auth.commons.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Represents a REST server-side exception that includes a custom error message and an error code.
 * This exception is designed to be used in RESTful web services to provide clients with detailed
 * error information. It is both serializable and capable of being constructed with varying levels
 * of detail, supporting the conveyance of both standard error messages and application-specific
 * data (msg).
 *
 * <p>The class provides static factory methods for convenience in creating instances with common
 * configurations, allowing for快速 instantiation based on typical error scenarios.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RestServerException extends RuntimeException implements Serializable {

    protected Object msg;

    protected int code;

    /**
     * Constructs a new RestServerException with a specified error code, message, and additional details.
     *
     * @param code    The error code associated with the exception.
     * @param message A brief description of the error.
     * @param msg     Additional details or metadata about the exception, which can be any object.
     */
    public RestServerException(int code, String message, Object msg) {
        super(message);
        this.msg = msg;
        this.code = code;
    }

    /**
     * Creates a new instance of {@link RestServerException} with a default error code of 500 and the provided message and details.
     *
     * @param message A descriptive message explaining the exception occurrence.
     * @param msg     An arbitrary object containing additional details about the error, enhancing the exception's context.
     * @return A configured {@link RestServerException} instance ready to be thrown, encapsulating the specified error information.
     */
    public static RestServerException withMsg(String message, Object msg) {
        return withMsg(500, message, msg);
    }

    /**
     * Creates a new instance of {@link RestServerException} with a specified error code, message, and additional details.
     * This static method serves as a convenience constructor, allowing for the instantiation of a {@link RestServerException}
     * with explicit control over the error code, message, and an arbitrary object carrying additional information (msg).
     *
     * @param code    The numeric error code associated with the exception, useful for categorizing errors on the client-side.
     * @param message A human-readable description of the error, providing context about what went wrong.
     * @param msg     An optional object containing additional metadata or details related to the exception.
     * @return A new instance of {@link RestServerException} initialized with the provided parameters.
     */
    public static RestServerException withMsg(int code, String message, Object msg) {
        return new RestServerException(code, message, msg);
    }

}