package com.plate.boot.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the stateless session policy in {@link SessionConfiguration}.
 */
class SessionConfigurationTest {

    @Test
    void securityContextRepositoryIsStateless() {
        ServerSecurityContextRepository repository = new SessionConfiguration().securityContextRepository();

        assertThat(repository).isInstanceOf(NoOpServerSecurityContextRepository.class);
    }

    @Test
    void repositoryNeverLoadsOrPersistsAContext() {
        ServerSecurityContextRepository repository = new SessionConfiguration().securityContextRepository();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path").build());
        var context = new SecurityContextImpl(
                UsernamePasswordAuthenticationToken.authenticated("admin", null, java.util.List.of()));

        StepVerifier.create(repository.save(exchange, context)).verifyComplete();
        StepVerifier.create(repository.load(exchange)).verifyComplete();
    }
}
