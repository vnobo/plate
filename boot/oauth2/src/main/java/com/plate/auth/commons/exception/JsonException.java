package com.plate.auth.commons.exception;

import java.io.IOException;

/**
 * Exception class specifically designed to handle errors related to JSON processing within the application.
 * It extends {@link RestServerException} to provide additional context and structure for exceptions
 * that occur during JSON operations, typically involving parsing, serialization, or deserialization.
 */
public class JsonException extends RestServerException {

    /**
     * Constructs a JsonException with a specified IOException instance.
     *
     * @param jsonProcessingException The IOException that triggered this exception, representing a JSON processing error.
     */
    public JsonException(IOException jsonProcessingException) {
        this(5010, "Json processing exception", jsonProcessingException);
    }

    /**
     * Constructs a JsonException with a specified status, message, and additional details.
     *
     * @param status  The HTTP status code to represent the error.
     * @param message A brief description of the JSON-related error.
     * @param msg     Additional details or metadata about the exception, which can be any object.
     */
    public JsonException(int status, String message, Object msg) {
        super(status, message, msg);
    }

    /**
     * Creates and returns a new {@link JsonException} instance wrapping the provided {@link IOException},
     * which represents a JSON processing error. This static method is a convenience alternative
     * to directly instantiating {@link JsonException} with an {@code IOException} argument,
     * streamlining error handling in JSON processing code.
     *
     * @param jsonProcessingException The {@link IOException} that occurred during JSON processing,
     *                                detailing the nature of the error.
     * @return A new instance of {@link JsonException} encapsulating the provided {@code IOException}.
     */
    public static JsonException withError(IOException jsonProcessingException) {
        return new JsonException(jsonProcessingException);
    }
}