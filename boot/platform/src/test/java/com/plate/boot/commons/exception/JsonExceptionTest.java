package com.plate.boot.commons.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JsonException} (no Spring / container required).
 */
class JsonExceptionTest {

    private final Throwable cause = new RuntimeException("parse failure");

    @Test
    void constructorWithThrowableUsesDefaultMessage() {
        JsonException ex = new JsonException(cause);

        assertThat(ex.getReason()).isEqualTo("Json processing exception");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void constructorWithMessageAndThrowableStoresBoth() {
        JsonException ex = new JsonException("custom json error", cause);

        assertThat(ex.getReason()).isEqualTo("custom json error");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void withErrorThrowableReturnsJsonException() {
        JsonException ex = JsonException.withError(cause);

        assertThat(ex).isInstanceOf(JsonException.class);
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void withErrorMessageAndThrowableReturnsJsonPointerException() {
        JsonPointerException ex = JsonException.withError("pointer error", cause);

        assertThat(ex).isInstanceOf(JsonPointerException.class);
        assertThat(ex.getReason()).isEqualTo("pointer error");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void isThrowAsRuntimeException() {
        assertThatThrownBy(() -> {
            throw new JsonException(cause);
        }).isInstanceOf(RuntimeException.class);
    }
}
