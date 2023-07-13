package com.platform.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

/**
 * This configuration class sets up a Snowflake ID generator using Redis, and also configures a custom argument resolver for Reactive Spring WebFlux.
 * The Snowflake ID generator is used to generate unique IDs for entities in the application, and the custom argument resolver allows for easier pagination of large data sets.
 *
 * @author billb
 */
@Configuration(proxyBeanMethods = false)
public class WebConfiguration implements WebFluxConfigurer {

    /**
     * Configures a custom argument resolver for Reactive Spring WebFlux.
     * The custom argument resolver allows for easier pagination of large data sets.
     *
     * @param configurer the argument resolver configurer
     */
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        ReactivePageableHandlerMethodArgumentResolver pageableResolver =
                new ReactivePageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(1000);
        configurer.addCustomResolver(pageableResolver);
    }

}