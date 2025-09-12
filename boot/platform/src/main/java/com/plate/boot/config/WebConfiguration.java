package com.plate.boot.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.util.List;

/**
 * Configures web-related settings and behaviors for an application, including RSocket setup,
 * scheduling, asynchronous method handling, and custom argument resolvers for reactive environments.
 * This configuration class integrates with Spring's WebFlux features to enable functionalities
 * like scheduling tasks, processing asynchronous requests, and defining custom argument resolving
 * strategies for handler methods.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({WebfluxProperties.class})
@RequiredArgsConstructor
public class WebConfiguration implements WebFluxConfigurer {

    private final WebfluxProperties webfluxProperties;

    /**
     * Configures custom argument resolvers for handler methods in a reactive environment.
     * This method specifically sets up a {@link ReactivePageableHandlerMethodArgumentResolver}
     * to handle {@link Pageable} arguments, limiting the maximum page size and setting a default
     * fallback page size when none is provided.
     *
     * @param configurer The {@link ArgumentResolverConfigurer} used to register custom argument resolvers.
     */
    @Override
    public void configureArgumentResolvers(@NonNull ArgumentResolverConfigurer configurer) {
        ReactivePageableHandlerMethodArgumentResolver pageableResolver =
                new ReactivePageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(webfluxProperties.getMaxPageSize());
        pageableResolver.setFallbackPageable(Pageable.ofSize(webfluxProperties.getDefaultPageSize()));
        configurer.addCustomResolver(pageableResolver);
    }

    /**
     * Configures path matching options for the application.
     * This method uses the {@link PathMatchConfigurer} to add path prefixes for specific base packages.
     * For example, it adds the "/oauth/v1" prefix for handler methods in the "com.plate.boot.security" package
     * and the "/rela/v1" prefix for handler methods in the "com.plate.boot.relational" package.
     *
     * @param configurer The {@link PathMatchConfigurer} used to configure path matching options.
     */
    @Override
    public void configurePathMatching(@NonNull PathMatchConfigurer configurer) {
        List<WebfluxProperties.RouteDefinition> pathPrefixes = this.webfluxProperties.getPathPrefixes();
        if (ObjectUtils.isEmpty(pathPrefixes)) {
            return;
        }
        for (WebfluxProperties.RouteDefinition entry : pathPrefixes) {
            configurer.addPathPrefix(entry.getPath(), HandlerTypePredicate.forBasePackage(entry.getBasePackage()));
        }
    }
}