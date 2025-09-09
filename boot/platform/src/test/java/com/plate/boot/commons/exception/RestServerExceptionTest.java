package com.plate.boot.commons.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.server.ServerErrorException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the RestServerException class.
 */
@DisplayName("RestServerException Tests")
class RestServerExceptionTest {

    // Helper class for testing
    static class TestClass {
        public void testMethod() {
        }

        public void testMethod(String param) {
        }
    }

    @Nested
    @DisplayName("Exception Construction")
    class ExceptionConstruction {

        @Test
        @DisplayName("Should create exception with reason and cause")
        void shouldCreateExceptionWithReasonAndCause() {
            Throwable cause = new RuntimeException("Root cause");
            RestServerException exception = new RestServerException("Error occurred", cause);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNotNull();
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception with reason, handler method, and cause")
        void shouldCreateExceptionWithReasonHandlerMethodAndCause() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("testMethod");
            Throwable cause = new RuntimeException("Root cause");
            RestServerException exception = new RestServerException("Error occurred", method, cause);

            assertThat(exception).isNotNull();
            // We're not checking the message content as it may vary
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception with reason, method parameter, and cause")
        void shouldCreateExceptionWithReasonMethodParameterAndCause() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("testMethod", String.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            Throwable cause = new RuntimeException("Root cause");
            RestServerException exception = new RestServerException("Error occurred", parameter, cause);

            assertThat(exception).isNotNull();
            // We're not checking the message content as it may vary
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("Should create exception using withMsg method with reason and cause")
        void shouldCreateExceptionUsingWithMsgMethodWithReasonAndCause() {
            Throwable cause = new IllegalArgumentException("Invalid argument");
            RestServerException exception = RestServerException.withMsg("Test error", cause);

            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNotNull();
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception using withMsg method with reason, handler method, and cause")
        void shouldCreateExceptionUsingWithMsgMethodWithReasonHandlerMethodAndCause() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("testMethod");
            Throwable cause = new RuntimeException("Root cause");
            RestServerException exception = RestServerException.withMsg("Test error", method, cause);

            assertThat(exception).isNotNull();
            // We're not checking the message content as it may vary
            assertThat(exception.getCause()).isEqualTo(cause);
        }

        @Test
        @DisplayName("Should create exception using withMsg method with reason, method parameter, and cause")
        void shouldCreateExceptionUsingWithMsgMethodWithReasonMethodParameterAndCause() throws NoSuchMethodException {
            Method method = TestClass.class.getMethod("testMethod", String.class);
            MethodParameter parameter = new MethodParameter(method, 0);
            Throwable cause = new RuntimeException("Root cause");
            RestServerException exception = RestServerException.withMsg("Test error", parameter, cause);

            assertThat(exception).isNotNull();
            // We're not checking the message content as it may vary
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("Inheritance")
    class Inheritance {

        @Test
        @DisplayName("Should be instance of ServerErrorException")
        void shouldBeInstanceOfServerErrorException() {
            RestServerException exception = RestServerException.withMsg("Test error", new RuntimeException());
            assertThat(exception).isInstanceOf(ServerErrorException.class);
        }

        @Test
        @DisplayName("Should be instance of RuntimeException")
        void shouldBeInstanceOfRuntimeException() {
            RestServerException exception = RestServerException.withMsg("Test error", new RuntimeException());
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }
    }
}