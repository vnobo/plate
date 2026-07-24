package com.plate.boot.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test Infrastructure Configuration
 *
 * <p>
 * This configuration sets up the necessary infrastructure components for testing,
 * including Redis and PostgreSQL containers managed by Spring Boot Testcontainers.
 * Connection details are auto-configured via {@link ServiceConnection @ServiceConnection},
 * eliminating the need for manual {@code @DynamicPropertySource} registration.
 * </p>
 *
 * <h3>Auto-configured connections</h3>
 * <ul>
 *   <li>{@code JdbcConnectionDetails} — for Flyway migrations</li>
 *   <li>{@code R2dbcConnectionDetails} — for R2DBC reactive data access</li>
 *   <li>{@code DataRedisConnectionDetails} — for Redis session and cache</li>
 * </ul>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@TestConfiguration(proxyBeanMethods = false)
public class InfrastructureConfiguration {

    @Bean(destroyMethod = "close")
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(
                DockerImageName.parse("alexbob/postgres").asCompatibleSubstituteFor("postgres"))
                .waitingFor(Wait.forLogMessage("^.*database system is ready to accept connections.*$", 2));
    }

    @Bean(destroyMethod = "close")
    @ServiceConnection(name = "redis")
    RedisContainer redisContainer() {
        return new RedisContainer("redis:latest");
    }
}
