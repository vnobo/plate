package com.plate.boot.config;

import com.google.common.collect.Lists;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.UserAuditorAware;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * Configures R2DBC (Reactive Relational Database Connectivity) settings, converters, and auditing for the application.
 * This class sets up the R2DBC connection factory, custom converters, enables transaction management,
 * and provides an auditor aware component for reactive auditing purposes.
 */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableR2dbcAuditing
@RequiredArgsConstructor
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    private final List<Converter<?, ?>> customConverters;

    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:..");
    }

    @Override
    public @NonNull List<Object> getCustomConverters() {
        return Lists.newArrayList(customConverters);
    }

    @Bean
    public ReactiveAuditorAware<UserAuditor> userAuditorProvider() {
        return new UserAuditorAware();
    }

}