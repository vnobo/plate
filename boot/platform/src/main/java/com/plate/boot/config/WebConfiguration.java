package com.plate.boot.config;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

/**
 * Configures web-related settings and behaviors for an application, including RSocket setup,
 * scheduling, asynchronous method handling, and custom argument resolvers for reactive environments.
 * This configuration class integrates with Spring's WebFlux features to enable functionalities
 * like scheduling tasks, processing asynchronous requests, and defining custom argument resolving
 * strategies for handler methods.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableAsync
public class WebConfiguration implements WebFluxConfigurer {

    @Value("${server.port:8080}")
    private Integer serverPort;

    /**
     * Creates and configures an RSocketRequester for establishing RSocket connections with authentication and specific setup parameters.
     *
     * @param requesterBuilder The builder instance to construct the RSocketRequester.
     * @param handler          The RSocketMessageHandler for handling incoming and outgoing messages.
     * @return An RSocketRequester configured with the provided parameters, ready to establish WebSocket connections.
     */
    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder requesterBuilder, RSocketMessageHandler handler) {
        MimeType authenticationMimeType =
                MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
        UsernamePasswordMetadata credentials = new UsernamePasswordMetadata("admin", "123456");
        URI url = URI.create("http://localhost:" + serverPort + "/rsocket");
        return requesterBuilder.setupData("CommandClient").setupRoute("connect.setup")
                .setupMetadata(credentials, authenticationMimeType)
                .rsocketStrategies(strategies->strategies.encoder(new SimpleAuthenticationEncoder()))
                .rsocketConnector(connector -> connector.acceptor(handler.responder())
                        .keepAlive(Duration.ofSeconds(5),Duration.ofMinutes(100))
                        .reconnect(Retry.backoff(100, Duration.ofMillis(500))))
                .websocket(url);
    }

    /**
     * Configures custom argument resolvers for handler methods in a reactive environment.
     * This method specifically sets up a {@link ReactivePageableHandlerMethodArgumentResolver}
     * to handle {@link Pageable} arguments, limiting the maximum page size and setting a default
     * fallback page size when none is provided.
     *
     * @param configurer The {@link ArgumentResolverConfigurer} used to register custom argument resolvers.
     */
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        ReactivePageableHandlerMethodArgumentResolver pageableResolver =
                new ReactivePageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(100);
        pageableResolver.setFallbackPageable(Pageable.ofSize(25));
        configurer.addCustomResolver(pageableResolver);
    }

}