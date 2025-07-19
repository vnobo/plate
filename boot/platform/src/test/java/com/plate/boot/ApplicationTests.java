package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot Application Startup Test
 *
 * <p>This test verifies that the Spring Boot application can start successfully
 * with all required dependencies and configurations.</p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
class ApplicationTests {
    private static final Logger log = LoggerFactory.getLogger(ApplicationTests.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    /**
     * Basic context load test - verifies that the Spring application context
     * can start successfully with all required beans.
     */
    @Test
    void contextLoads() {
        log.info("Application context loaded successfully with {} beans",
                applicationContext.getBeanDefinitionCount());

        // Verify that essential beans are available
        assertThat(applicationContext.containsBean("connectionFactory")).isTrue();
        assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue();
    }

    /**
     *
     * Verifies that the infrastructure components (PostgreSQL, Redis)
     * are properly connected and available.
     */
    @Test
    void infrastructureComponentsAvailable() {
        // Verify database connection
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        Mono<String> result = client.sql("select version()").map(row -> "Connected").first();

        StepVerifier.create(result)
                .expectNext("Connected")
                .verifyComplete();

        log.info("Database connection verified");

        // Verify Redis connection
        Mono<Boolean> pingResult = reactiveRedisTemplate.execute(connection -> connection.serverCommands().info())
                .map(Properties::elements).hasElements();

        StepVerifier.create(pingResult)
                .expectNext(true)
                .verifyComplete();

        log.info("Redis connection verified");
    }
}