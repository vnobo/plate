package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Spring Boot Application Startup Test
 *
 * <p>This comprehensive test suite verifies that the Spring Boot application can start successfully
 * with all required dependencies and configurations. It includes integration tests for database,
 * Redis, security, and application endpoints.</p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
public class ApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(ApplicationTests.class);

    private final ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    public ApplicationTests(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @BeforeEach
    void setUp() {
        log.info("Setting up test context with port: {}", port);
    }

    @Test
    @DisplayName("Context Loading Test")
    void contextLoads() {
        log.info("Application context loaded successfully with {} beans",
                applicationContext.getBeanDefinitionCount());

        assertAll("Essential beans should be present",
                () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                () -> assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue(),
                () -> assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue(),
                () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue()
        );
    }
}