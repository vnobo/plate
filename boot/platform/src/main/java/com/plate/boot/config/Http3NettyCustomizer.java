package com.plate.boot.config;

import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import reactor.netty.http.Http3SslContextSpec;
import reactor.netty.http.HttpProtocol;

import java.time.Duration;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
//@Configuration(proxyBeanMethods = false)
public class Http3NettyCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    @Override
    public void customize(NettyReactiveWebServerFactory factory) {
        factory.addServerCustomizers(server -> {
            SslBundle sslBundle = factory.getSslBundles().getBundle("server-http3");
            Http3SslContextSpec sslContextSpec =
                    Http3SslContextSpec.forServer(sslBundle.getManagers().getKeyManagerFactory(),
                            sslBundle.getKey().getPassword());

            return server
                    // Configure HTTP/3 protocol
                    .protocol(HttpProtocol.HTTP3)
                    // Configure HTTP/3 SslContext
                    .secure(spec -> spec.sslContext(sslContextSpec))
                    // Configure HTTP/3 settings
                    .http3Settings(spec -> spec.idleTimeout(Duration.ofSeconds(5))
                            .maxData(10_000_000)
                            .maxStreamDataBidirectionalRemote(1_000_000)
                            .maxStreamsBidirectional(100));
        });
    }
}
