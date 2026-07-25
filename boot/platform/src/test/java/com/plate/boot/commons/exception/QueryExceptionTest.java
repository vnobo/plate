package com.plate.boot.commons.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link QueryException} (no Spring / container required).
 */
class QueryExceptionTest {

    private final Throwable cause = new RuntimeException("db failure");

    @Test
    void constructorStoresMessageAndCause() {
        QueryException ex = new QueryException("bad query", cause);

        assertThat(ex.getReason()).isEqualTo("bad query");
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex).isInstanceOf(RestServerException.class);
    }

    @Test
    void withErrorFactoryReturnsEquivalentInstance() {
        QueryException ex = QueryException.withError("bad query", cause);

        assertThat(ex.getReason()).isEqualTo("bad query");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void equalsBasedOnMessageAndCause() {
        QueryException a = new QueryException("m", cause);
        QueryException c = new QueryException("other", cause);

        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo("not an exception");
    }
}
