package com.plate.boot.commons;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents an error response structure that is used to convey detailed information
 * about an error encountered during processing.
 * This record encapsulates metadata such as the request identifier, the endpoint path,
 * the error code, a descriptive message,
 * additional error details, and the timestamp when the error occurred.
 */
public record ErrorResponse(String requestId, String path, Integer code,
                            String message, Object errors, LocalDateTime time) implements Serializable {
    /**
     * Constructs a new {@link ErrorResponse} instance with the provided parameters,
     * automatically setting the timestamp to the current date and time.
     *
     * @param requestId The unique identifier for the request associated with the error.
     * @param path The endpoint path where the error occurred.
     * @param code The error code representing the type of error.
     * @param message A descriptive message explaining the error.
     * @param errors Additional details about the error, can be an object like a list of errors or a structured error message.
     * @return A new {@link ErrorResponse} instance encapsulating the error details including the current timestamp.
     */
    public static ErrorResponse of(String requestId, String path, Integer code, String message, Object errors) {
        return new ErrorResponse(requestId, path, code, message, errors, LocalDateTime.now());
    }
}