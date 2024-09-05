package com.plate.boot.commons.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

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

    /**
     * Encapsulates additional metadata or details about the error condition.
     * This field can hold any type of object, enabling the transmission of structured
     * information alongside the exception, such as maps, lists, or custom objects that
     * provide context for the error scenario.
     */
    protected Object msg;
    /**
     * The error code associated with this {@link RestServerException}.
     * This field represents a custom error code that provides more granular information
     * about the specific error scenario beyond standard HTTP status codes.
     * It is intended for use in identifying and differentiating between various error conditions.
     */
    protected int code;

    /**
     * Constructs a new instance of {@code RestServerException} with specified error code, message, and additional details.
     * This exception is intended for conveying REST server-side error conditions, providing a more granular error code
     * alongside a standard exception message and an optional object that can carry detailed contextual information.
     *
     * @param code    The custom error code associated with the exception. This can be used to differentiate between
     *                various error scenarios beyond the standard HTTP status codes.
     * @param message The human-readable error message explaining the exception circumstances. Should be concise and informative.
     * @param msg     An optional object containing additional metadata or details about the error. Can be any type,
     *                facilitating the passing of structured error information (e.g., maps, lists, or domain-specific objects).
     */
    public RestServerException(int code, String message, Object msg) {
        super(message);
        this.msg = msg;
        this.code = code;
    }

    /**
     * Creates a new instance of {@code RestServerException} with a predefined HTTP status code of 500 (Internal Server Error),
     * a custom message, and additional details encapsulated in the {@code msg} parameter.
     * This method serves as a convenience factory for generating exceptions that indicate a generic server error
     * along with specific contextual information.
     *
     * @param message A descriptive message explaining the reason for the exception.
     * @param msg     An arbitrary object containing additional details about the error. This can be used to provide more
     *                extensive error context or metadata.
     * @return A new instance of {@code RestServerException} initialized with the provided message, a status code of 500,
     * and the additional details object.
     */
    public static RestServerException withMsg(String message, Object msg) {
        return withMsg(500, message, msg);
    }

    /**
     * Creates a new instance of {@code RestServerException} with a specified error code, message, and additional details.
     * This static factory method allows for customization of the error response by providing a unique error code and
     * a message along with an arbitrary object that can contain further information about the error condition.
     *
     * @param code    The custom error code to identify the specific error scenario. This code supplements the HTTP status code.
     * @param message The error message describing the exception's nature. Should be informative for debugging purposes.
     * @param msg     An optional object holding additional metadata or details related to the error. Can be any type.
     * @return A new instance of {@code RestServerException} initialized with the provided code, message, and additional details.
     */
    public static RestServerException withMsg(int code, String message, Object msg) {
        return new RestServerException(code, message, msg);
    }

}