package com.platform.boot.config;

import com.google.common.collect.Lists;
import com.platform.boot.security.core.UserAuditor;
import com.platform.boot.security.core.UserAuditorAware;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
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
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get("r2dbc:..");
    }

    @Override
    public List<Object> getCustomConverters() {
        return Lists.newArrayList(customConverters);
    }

    @Bean
    public ReactiveAuditorAware<UserAuditor> userAuditorProvider() {
        return new UserAuditorAware();
    }

    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService authorizedClientService(DatabaseClient databaseClient,
                                                                         ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new R2dbcReactiveOAuth2AuthorizedClientService(databaseClient, clientRegistrationRepository);
    }
}