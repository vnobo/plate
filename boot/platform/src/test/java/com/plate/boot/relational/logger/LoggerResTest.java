package com.plate.boot.relational.logger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerRes} (no Spring / container required).
 */
class LoggerResTest {

    @Test
    void rankAccessorRoundTrip() {
        LoggerRes res = new LoggerRes();
        res.setRank(0.75D);
        res.setOperator("admin");

        assertThat(res.getRank()).isEqualTo(0.75D);
        assertThat(res.getOperator()).isEqualTo("admin");
    }

    @Test
    void equalsAndHashCodeIncludeRank() {
        LoggerRes a = new LoggerRes();
        a.setId(1L);
        a.setRank(0.5D);

        LoggerRes b = new LoggerRes();
        b.setId(1L);
        b.setRank(0.5D);

        LoggerRes c = new LoggerRes();
        c.setId(1L);
        c.setRank(0.9D);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsRank() {
        LoggerRes res = new LoggerRes();
        res.setRank(1.5D);
        assertThat(res.toString()).contains("1.5");
    }
}
