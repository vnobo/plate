package com.plate.boot.commons.exception;

import java.io.IOException;

/**
 * Exception class specifically designed to handle JSON processing errors, extending {@link RestServerException}.
 * This exception is typically thrown when there is an issue with parsing or generating JSON data.
 */
public class JsonException extends RestServerException {

    /**
     * Constructs a new {@code JsonException} instance initialized with a default status code of 500 and a predefined error message,
     * wrapping the provided {@link IOException} which represents a JSON processing error.
     *
     * @param jsonProcessingException The {@link IOException} that was encountered during JSON processing, providing specifics about the processing failure.
     */
    public JsonException(IOException jsonProcessingException) {
        this(500, "Json processing exception", jsonProcessingException);
    }

    /**
     * Constructs a new {@code JsonException} with a specified status code, error message, and additional details.
     * This exception is typically utilized to wrap issues encountered during JSON processing, adding a layer of specificity
     * over the broader {@link RestServerException}.
     *
     * @param status  The HTTP status code representing the type of error occurred. This helps in categorizing the exception.
     * @param message A human-readable description of the error, providing context about what went wrong during JSON processing.
     * @param msg     An optional object containing further information or metadata related to the error, which can assist in diagnosing the issue.
     */
    public JsonException(int status, String message, Object msg) {
        super(status, message, msg);
    }

    /**
     * Constructs and returns a new {@code JsonException} instance wrapping the provided {@link IOException},
     * which is indicative of a JSON processing error. This method is a convenience factory for creating
     * {@code JsonException} objects without needing to explicitly reference the constructor arguments.
     *
     * @param jsonProcessingException The {@link IOException} that occurred during JSON processing,
     *                                providing details about the processing error.
     * @return A new instance of {@code JsonException} initialized with the given {@code IOException}
     * as the cause, carrying a default status code and message indicative of a JSON processing failure.
     */
    public static JsonException withError(IOException jsonProcessingException) {
        return new JsonException(jsonProcessingException);
    }
}