package com.plate.boot.commons.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the QueryException class.
 */
@DisplayName("QueryException Tests")
class QueryExceptionTest {

    @Nested
    @DisplayName("Exception Creation")
    class ExceptionCreation {

        @Test
        @DisplayName("Should create QueryException with message and cause")
        void shouldCreateQueryExceptionWithMessageAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            QueryException exception = new QueryException("Query error occurred", cause);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("Query error occurred");
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create QueryException using factory method")
        void shouldCreateQueryExceptionUsingFactoryMethod() {
            Throwable cause = new IllegalArgumentException("Invalid parameter");
            QueryException exception = QueryException.withError("Error executing query", cause);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).contains("Error executing query");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Inheritance")
    class Inheritance {

        @Test
        @DisplayName("Should be instance of RestServerException")
        void shouldBeInstanceOfRestServerException() {
            QueryException exception = QueryException.withError("Test error", new RuntimeException());
            assertThat(exception).isInstanceOf(RestServerException.class);
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            QueryException exception = QueryException.withError("Test error", new RuntimeException());
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}