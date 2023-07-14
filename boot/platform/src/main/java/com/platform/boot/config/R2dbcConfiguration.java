package com.platform.boot.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for R2DBC autoconfiguration.
 * This class extends AbstractR2dbcConfiguration and enables R2DBC auditing and transaction management.
 * It also provides a custom ConnectionFactory and custom converters for JSON nodes and user auditing.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableR2dbcAuditing
@RequiredArgsConstructor
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    private final List<Converter<?, ?>> customConverters;

    /**
     * Returns a ConnectionFactory for R2DBC.
     *
     * @return ConnectionFactory for R2DBC
     */
    @Override
    @NonNull
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:..");
    }

    /**
     * Returns a list of custom converters for JSON nodes and user auditing.
     *
     * @return List of custom converters
     */
    @Override
    @NonNull
    public List<Object> getCustomConverters() {
        return new ArrayList<>(customConverters);
    }
}