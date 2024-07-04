package com.platform.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.net.URI;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableAsync
public class WebConfiguration implements WebFluxConfigurer {

    @Value("${server.port:8080}")
    private Integer serverPort;

    @Bean
    public RSocketRequester rSocketRequester(RSocketRequester.Builder requesterBuilder, RSocketMessageHandler handler) {
        URI url = URI.create("http://localhost:" + serverPort + "/rsocket");
        return requesterBuilder.setupData("CommandClient").setupRoute("connect.setup")
                .rsocketConnector(connector -> connector.acceptor(handler.responder())).websocket(url);
    }

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        ReactivePageableHandlerMethodArgumentResolver pageableResolver =
                new ReactivePageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(100);
        pageableResolver.setFallbackPageable(Pageable.ofSize(25));
        configurer.addCustomResolver(pageableResolver);
    }

}