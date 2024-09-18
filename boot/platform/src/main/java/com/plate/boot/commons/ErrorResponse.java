package com.plate.boot.commons;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an error response structure, encapsulating details about a specific error occurrence.
 * This record is designed to be serialized and used for communicating error information between systems.
 *
 * @param requestId A unique identifier for the request that encountered the error.
 * @param code      The HTTP status code representing the type of error.
 * @param path      The endpoint or URI path where the error occurred.
 * @param message   A human-readable description of the error.
 * @param errors    Additional details or objects related to the error, can vary based on the context.
 * @param time      The timestamp indicating when the error response was created.
 */
public record ErrorResponse(String requestId, Integer code, String path,
                            String message, Object errors, LocalDateTime time) implements Serializable {

    /**
     * Creates an instance of {@link ErrorResponse} with the current local date and time.
     *
     * @param requestId A unique identifier for the request associated with this error response.
     * @param code      The HTTP status code reflecting the error type.
     * @param path      The path or endpoint where the error was encountered.
     * @param message   A descriptive message explaining the error.
     * @param errors    Additional error-related data, which could include exception details, validation errors, etc.
     * @return A new {@link ErrorResponse} instance initialized with the provided parameters and the current timestamp.
     */
    public static ErrorResponse of(String requestId, Integer code, String path, String message, Object errors) {
        return new ErrorResponse(requestId, code, path, message, errors, LocalDateTime.now());
    }
}