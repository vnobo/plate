package com.plate.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;

/**
 * Stateless session policy. JWT authentication is validated on every request by the resource
 * server, so no security context is persisted between requests and no session is created.
 */
@Configuration(proxyBeanMethods = false)
public class SessionConfiguration {

    /**
     * A repository that never loads or saves a security context, making the filter chain
     * effectively stateless (the WebFlux equivalent of {@code STATELESS} session creation).
     */
    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return NoOpServerSecurityContextRepository.getInstance();
    }
}
