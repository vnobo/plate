package com.plate.boot.commons.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JsonPointerException} (no Spring / container required).
 */
class JsonPointerExceptionTest {

    private final Throwable cause = new RuntimeException("missing path");

    @Test
    void constructorStoresMessageAndCause() {
        JsonPointerException ex = new JsonPointerException("pointer failure", cause);

        assertThat(ex.getReason()).isEqualTo("pointer failure");
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex).isInstanceOf(JsonException.class);
    }

    @Test
    void withErrorFactoryReturnsInstance() {
        JsonPointerException ex = JsonPointerException.withError("pointer failure", cause);

        assertThat(ex.getReason()).isEqualTo("pointer failure");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void equalsBasedOnMessageAndCause() {
        JsonPointerException a = new JsonPointerException("m", cause);
        JsonPointerException c = new JsonPointerException("other", cause);

        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo("not an exception");
    }
}
