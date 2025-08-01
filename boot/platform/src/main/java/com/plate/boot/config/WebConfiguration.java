package com.plate.boot.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.util.ObjectUtils;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import reactor.netty.http.Http3SslContextSpec;
import reactor.netty.http.HttpProtocol;

import java.time.Duration;
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

    /**
     * Holds the webflux properties configuration.
     */
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

    //@Component
    static class Http3NettyWebServerCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

        @Override
        public void customize(NettyReactiveWebServerFactory factory) {
            factory.addServerCustomizers(server -> {
                SslBundle sslBundle = factory.getSslBundles().getBundle("plate-bundle");
                Http3SslContextSpec sslContextSpec =
                        Http3SslContextSpec.forServer(sslBundle.getManagers().getKeyManagerFactory(),
                                sslBundle.getKey().getPassword());

                return server
                        // Configure HTTP/3 protocol
                        .protocol(HttpProtocol.HTTP3)
                        // Configure HTTP/3 SslContext
                        .secure(spec -> spec.sslContext(sslContextSpec))
                        // Configure HTTP/3 settings
                        .http3Settings(spec -> spec
                                .idleTimeout(Duration.ofSeconds(5))
                                .maxData(10_000_000)
                                .maxStreamDataBidirectionalRemote(1_000_000)
                                .maxStreamsBidirectional(100));
            });
        }
    }
}