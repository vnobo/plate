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
     * @param exception The {@link IOException} that was encountered during JSON processing, providing specifics about the processing failure.
     */
    public JsonException(Throwable exception) {
        this("Json processing exception", exception);
    }

    /**
     * Constructs a new {@code JsonException} with a specified error message and the cause of the exception.
     * This constructor allows for detailed customization of the exception message while preserving the original
     * exception's stack trace for debugging purposes.
     *
     * @param message   A descriptive message explaining the reason for the exception.
     * @param exception The underlying {@link Throwable} that caused this exception to be thrown, providing additional context.
     */
    public JsonException(String message, Throwable exception) {
        super(message, exception);
    }

    /**
     * Constructs and returns a new {@code JsonException} instance wrapping the provided {@link IOException},
     * which is indicative of a JSON processing error. This method is a convenience factory for creating
     * {@code JsonException} objects without needing to explicitly reference the constructor arguments.
     *
     * @param error The {@link IOException} that occurred during JSON processing,
     *              providing details about the processing error.
     * @return A new instance of {@code JsonException} initialized with the given {@code IOException}
     * as the cause, carrying a default status code and message indicative of a JSON processing failure.
     */
    public static JsonException withError(Throwable error) {
        return new JsonException(error);
    }

    public static JsonPointerException withError(String message, Throwable throwable) {
        return new JsonPointerException(message, throwable);
    }
}