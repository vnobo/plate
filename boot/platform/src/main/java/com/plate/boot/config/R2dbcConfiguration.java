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
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableR2dbcAuditing
@RequiredArgsConstructor
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    private final List<Converter<?, ?>> customConverters;

    @Override
    public @lombok.NonNull ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:..");
    }

    @Override
    public @lombok.NonNull List<Object> getCustomConverters() {
        return Lists.newArrayList(customConverters);
    }

    @Bean
    public ReactiveAuditorAware<UserAuditor> userAuditorProvider() {
        return new UserAuditorAware();
    }

}