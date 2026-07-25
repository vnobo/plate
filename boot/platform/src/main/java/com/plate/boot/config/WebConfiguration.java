package com.plate.boot.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.util.List;

/**
 * WebFlux configuration that registers custom handler-method argument resolvers (e.g. a reactive
 * {@code Pageable} resolver) and applies package-based path prefixes driven by {@link WebfluxProperties}.
 * Implements {@link WebFluxConfigurer} so these settings participate in Spring's WebFlux infrastructure.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({WebfluxProperties.class, HttpCodecsProperties.class})
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
     * Configures path matching by registering package-based path prefixes.
     * Each {@link WebfluxProperties.RouteDefinition} supplies a {@code path} prefix and a {@code basePackage};
     * handler methods in that base package are routed under the configured prefix. The actual prefixes are
     * data-driven from {@code spring.webflux.properties.path-prefixes} and are not hard-coded here.
     *
     * @param configurer The {@link PathMatchConfigurer} used to register the path prefixes.
     */
    @Override
    public void configurePathMatching(@NonNull PathMatchConfigurer configurer) {
        List<WebfluxProperties.RouteDefinition> pathPrefixes = this.webfluxProperties.getPathPrefixes();
        for (WebfluxProperties.RouteDefinition entry : pathPrefixes) {
            configurer.addPathPrefix(entry.getPath(), HandlerTypePredicate.forBasePackage(entry.getBasePackage()));
        }
    }
}