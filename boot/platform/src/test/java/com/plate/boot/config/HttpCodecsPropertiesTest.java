package com.plate.boot.config;

import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HttpCodecsProperties}.
 */
class HttpCodecsPropertiesTest {

    @Test
    void defaultsAreUnset() {
        HttpCodecsProperties properties = new HttpCodecsProperties();

        assertThat(properties.isLogRequestDetails()).isFalse();
        assertThat(properties.getMaxInMemorySize()).isNull();
    }

    @Test
    void settersUpdateBothProperties() {
        HttpCodecsProperties properties = new HttpCodecsProperties();

        properties.setLogRequestDetails(true);
        properties.setMaxInMemorySize(DataSize.ofKilobytes(512));

        assertThat(properties.isLogRequestDetails()).isTrue();
        assertThat(properties.getMaxInMemorySize()).isEqualTo(DataSize.ofKilobytes(512));
    }

    @Test
    void maxInMemorySizeCanBeClearedToNull() {
        HttpCodecsProperties properties = new HttpCodecsProperties();
        properties.setMaxInMemorySize(DataSize.ofKilobytes(10));

        properties.setMaxInMemorySize(null);

        assertThat(properties.getMaxInMemorySize()).isNull();
    }
}
