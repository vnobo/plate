package com.plate.boot.config;

import com.google.common.collect.Lists;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.UserAuditorAware;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.lang.NonNull;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
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

    /**
     * A collection of custom converters used to adapt between various data types when interacting with the database.
     * These converters facilitate the mapping of application-specific objects to database-compatible representations and vice versa.
     */
    private final List<Converter<?, ?>> customConverters;

    /**
     * Establishes and returns the configured R2DBC Connection Factory instance.
     * This method is part of the configuration setup for R2DBC within the application,
     * providing the necessary connection details to interact with the database in a reactive manner.
     *
     * @return A non-null instance of {@link ConnectionFactory} configured for R2DBC connectivity.
     */
    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:..");
    }

    /**
     * Retrieves a list of custom converters that have been configured for the application.
     * These converters are typically used to customize data binding between Java objects and
     * the database, enabling the application to handle specific data types or transformations.
     *
     * @return A non-null list containing instances of custom converters. The list is a fresh copy
     * and modifications to it will not affect the original configuration.
     */
    @Override
    public @NonNull List<Object> getCustomConverters() {
        return Lists.newArrayList(customConverters);
    }

    /**
     * Provides a bean instance of {@link ReactiveAuditorAware} configured to use {@link UserAuditorAware}.
     * This method sets up auditing awareness for reactive contexts, allowing the system to automatically
     * populate audit fields like creator, updater, etc., with details from the current security context.
     *
     * @return A bean instance of {@link ReactiveAuditorAware<UserAuditor>} that supplies the current auditor details.
     */
    @Bean
    public ReactiveAuditorAware<UserAuditor> userAuditorProvider() {
        return new UserAuditorAware();
    }

    @Bean
    public DSLContext dslContext(ConnectionFactory connectionFactory) {
        return DSL.using(
                new TransactionAwareConnectionFactoryProxy(connectionFactory),
                SQLDialect.POSTGRES
        );
    }
}