package com.plate.boot.commons.exception;

import java.io.IOException;

/**
 * Represents an exception that occurs when attempting to access a non-existent path within a JSON structure.
 * This exception extends {@link JsonException}, specifically catering to errors involving JSON pointer operations
 * which fail due to invalid or unreachable paths within the JSON data.
 *
 * <p>
 * This class provides constructors to initialize the exception with a descriptive message and an underlying
 * {@link IOException} that led to the JSON pointer error, allowing for detailed diagnostics of the issue.
 * Additionally, it includes a static factory method for conveniently creating instances with an error message
 * and the associated {@linkplain IOException}.
 * </p>
 */
public class JsonPointerException extends JsonException {

    /**
     * Constructs a new {@code JsonPointerException} with a specified error message and an {@link IOException} cause.
     * This exception is thrown when attempting to access a non-existent path within a JSON structure using a JSON pointer,
     * indicating that the referenced location could not be found or accessed due to structural issues within the JSON data.
     *
     * @param message   A human-readable description of the error, explaining the context of the JSON pointer operation that failed.
     * @param exception The {@link IOException} that triggered this exception, providing additional context or details about the failure.
     */
    public JsonPointerException(String message, Throwable exception) {
        super(message, exception);
    }

    /**
     * Creates a new instance of {@code JsonPointerException} with a specified error message and an {@link IOException}.
     * This static method serves as a convenience factory for initializing {@code JsonPointerException} instances,
     * encapsulating both a descriptive error message and the original {@linkplain IOException} that caused the failure.
     * It is particularly useful when the exception is due to an issue encountered while accessing a JSON structure,
     * such as a malformed JSON pointer or inaccessible data.
     *
     * @param message   A human-readable message describing the error context. This should explain the problem encountered with the JSON pointer operation.
     * @param exception The {@link IOException} that represents the underlying cause of the JSON pointer error. It provides deeper insight into why the operation failed.
     * @return A new {@code JsonPointerException} instance initialized with the given message and {@code IOException}.
     */
    public static JsonPointerException withError(String message, Throwable exception) {
        return new JsonPointerException(message, exception);
    }
}
