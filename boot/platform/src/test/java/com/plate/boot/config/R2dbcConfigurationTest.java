package com.plate.boot.config;

import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link R2dbcConfiguration}.
 */
class R2dbcConfigurationTest {

    @Test
    void connectionFactoryReturnsInjectedFactory() {
        ConnectionFactory factory = mock(ConnectionFactory.class);
        R2dbcConfiguration configuration = new R2dbcConfiguration(List.of(), factory);

        assertThat(configuration.connectionFactory()).isSameAs(factory);
    }

    @Test
    void getCustomConvertersReturnsDefensiveCopy() {
        Converter<?, ?> converter = mock(Converter.class);
        ConnectionFactory factory = mock(ConnectionFactory.class);
        R2dbcConfiguration configuration = new R2dbcConfiguration(List.of(converter), factory);

        List<Object> result = configuration.getCustomConverters();

        assertThat(result).containsExactly(converter);
        assertThat(result).isNotSameAs(List.of(converter));
    }

    @Test
    void afterPropertiesSetCompletesWithoutThrowing() {
        R2dbcConfiguration configuration = new R2dbcConfiguration(List.of(), mock(ConnectionFactory.class));

        configuration.afterPropertiesSet();
    }
}
