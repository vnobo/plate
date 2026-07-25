package com.plate.boot.commons.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link RestServerException} (no Spring / container required).
 */
class RestServerExceptionTest {

    private final Throwable cause = new IllegalStateException("root cause");

    /**
     * A non-null {@link MethodParameter} so the Spring {@code ServerErrorException} base
     * constructor does not NPE (it dereferences the parameter internally).
     */
    private static final MethodParameter SAMPLE_PARAMETER;

    static {
        try {
            SAMPLE_PARAMETER = new MethodParameter(
                    RestServerExceptionTest.class.getDeclaredMethod("sample", String.class), 0);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressWarnings("unused")
    void sample(String param) {
    }

    @Test
    void constructorWithReasonAndCauseStoresMessageAndCause() {
        RestServerException ex = new RestServerException("boom", cause);

        assertThat(ex.getReason()).isEqualTo("boom");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void constructorWithHandlerMethodAndCauseDoesNotThrow() {
        Method method = RestServerExceptionTest.class.getDeclaredMethods()[0];
        RestServerException ex = new RestServerException("handler boom", method, cause);

        assertThat(ex.getReason()).isEqualTo("handler boom");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void constructorWithMethodParameterAndCauseDoesNotThrow() {
        RestServerException ex = new RestServerException("param boom", SAMPLE_PARAMETER, cause);

        assertThat(ex.getReason()).isEqualTo("param boom");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void withMsgStoresReasonAndIsARestServerException() {
        RestServerException ex = RestServerException.withMsg("with-msg", cause);

        assertThat(ex).isInstanceOf(RestServerException.class);
        assertThat(ex.getReason()).isEqualTo("with-msg");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void withMsgWithHandlerMethodReturnsInstance() {
        Method method = RestServerExceptionTest.class.getDeclaredMethods()[0];
        RestServerException ex = RestServerException.withMsg("m", method, cause);

        assertThat(ex.getReason()).isEqualTo("m");
    }

    @Test
    void withMsgWithMethodParameterReturnsInstance() {
        RestServerException ex = RestServerException.withMsg("p", SAMPLE_PARAMETER, cause);

        assertThat(ex.getReason()).isEqualTo("p");
    }

    @Test
    void equalsAndHashCodeHonourReasonAndCause() {
        RestServerException a = new RestServerException("same", cause);
        RestServerException c = new RestServerException("different", cause);

        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo("not an exception");
    }

    @Test
    void isARuntimeExceptionSoCanBeThrownAnywhere() {
        assertThatThrownBy(() -> {
            throw new RestServerException("x", cause);
        }).isInstanceOf(RuntimeException.class);
    }
}
