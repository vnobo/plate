package com.plate.boot.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Test Infrastructure Configuration
 *
 * <p>This configuration sets up the necessary infrastructure components for testing,
 * including Redis and PostgresSQL containers.</p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@TestConfiguration(proxyBeanMethods = false)
public class InfrastructureConfiguration {

    @Bean
    public RedisContainer redisContainer() {
        return new RedisContainer("redis:latest");
    }

    @Bean
    public PostgreSQLContainer<?> postgresContainer() {
        var postgresImage = DockerImageName.parse("alexbob/postgres")
                .asCompatibleSubstituteFor("postgres");
        return new PostgreSQLContainer<>(postgresImage)
                .waitingFor(Wait.forLogMessage("^.*数据库系统准备接受连接.*$", 2));
    }
}
