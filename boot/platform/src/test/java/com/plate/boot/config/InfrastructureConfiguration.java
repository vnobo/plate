package com.plate.boot.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test Infrastructure Configuration
 *
 * <p>
 * This configuration sets up the necessary infrastructure components for
 * testing,
 * including Redis and PostgreSQL containers.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@TestConfiguration(proxyBeanMethods = false)
public class InfrastructureConfiguration {

    public static final RedisContainer REDIS_CONTAINER;
    public static final PostgreSQLContainer POSTGRES_CONTAINER;

    static {
        REDIS_CONTAINER = new RedisContainer("redis:latest");
        var postgresImage = DockerImageName.parse("alexbob/postgres")
                .asCompatibleSubstituteFor("postgres");
        POSTGRES_CONTAINER = new PostgreSQLContainer(postgresImage)
                .waitingFor(Wait.forLogMessage("^.*数据库系统准备接受连接.*$", 2));
    }

    @Bean
    public RedisContainer redisContainer() {
        return REDIS_CONTAINER;
    }

    @Bean
    public PostgreSQLContainer postgresContainer() {
        return POSTGRES_CONTAINER;
    }
}
