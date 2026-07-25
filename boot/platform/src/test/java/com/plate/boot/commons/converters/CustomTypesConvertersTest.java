package com.plate.boot.commons.converters;

import com.plate.boot.relational.MethodType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the pure converter logic in {@link CustomTypesConverters}.
 * {@code MethodTypeReadConverter} uses {@link MethodType#valueOf(String)} (throwing
 * {@link IllegalArgumentException} on unknown values); {@code MethodTypeWriteConverter}
 * uses {@link MethodType#name()}.
 */
class CustomTypesConvertersTest {

    @Test
    void methodTypeReadConverterResolvesKnownMethod() {
        var converter = new CustomTypesConverters.MethodTypeReadConverter();

        assertThat(converter.convert("POST")).isEqualTo(MethodType.POST);
        assertThat(converter.convert("PUT")).isEqualTo(MethodType.PUT);
    }

    @Test
    void methodTypeReadConverterThrowsForUnknownMethod() {
        var converter = new CustomTypesConverters.MethodTypeReadConverter();

        assertThatThrownBy(() -> converter.convert("NOPE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void methodTypeWriteConverterReturnsName() {
        var converter = new CustomTypesConverters.MethodTypeWriteConverter();

        assertThat(converter.convert(MethodType.DELETE)).isEqualTo("DELETE");
        assertThat(converter.convert(MethodType.UNKNOWN)).isEqualTo("UNKNOWN");
    }
}
