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

    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        ReactivePageableHandlerMethodArgumentResolver pageableResolver =
                new ReactivePageableHandlerMethodArgumentResolver();
        pageableResolver.setMaxPageSize(100);
        pageableResolver.setFallbackPageable(Pageable.ofSize(25));
        configurer.addCustomResolver(pageableResolver);
    }

}