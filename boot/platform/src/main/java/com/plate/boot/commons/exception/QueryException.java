package com.plate.boot.commons.exception;

/**
 * Custom exception class for handling query-related errors.
 * Extends the RestServerException to provide additional context for query errors.
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * try {
 *     // some query logic
 * } catch (Exception e) {
 *     throw QueryException.withError("Error executing query", e);
 * }
 * }
 * </pre>
 *
 * <p>This class provides a static method to create instances of QueryException with a specific error message and cause.</p>
 *
 * <p>Author: <a href="https://github.com/vnobo">Alex Bob</a></p>
 */
public class QueryException extends RestServerException {

    /**
     * Constructs a new QueryException with the specified detail message and cause.
     *
     * @param message   the detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
     * @param throwable the cause (which is saved for later retrieval by the {@link Throwable#getCause()} method).
     */
    public QueryException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Creates a new QueryException with the specified detail message and cause.
     *
     * @param message   the detail message.
     * @param throwable the cause.
     * @return a new instance of QueryException.
     */
    public static QueryException withError(String message, Throwable throwable) {
        return new QueryException(message, throwable);
    }
}