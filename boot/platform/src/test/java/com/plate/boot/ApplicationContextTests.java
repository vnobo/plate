package com.plate.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application context integration tests.
 * <p>
 * Verifies that the Spring application context loads successfully
 * and all core infrastructure beans are properly configured.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Application Context")
class ApplicationContextTests extends AbstractIntegrationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Nested
    @DisplayName("Core Infrastructure Beans")
    class CoreInfrastructureTests {

        @Test
        @DisplayName("should load R2DBC and Redis infrastructure beans")
        void shouldLoadInfrastructureBeans() {
            assertThat(applicationContext.containsBean("connectionFactory")).isTrue();
            assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue();
            assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue();
        }

        @Test
        @DisplayName("should load Spring Security filter chain")
        void shouldLoadSecurityFilterChain() {
            assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue();
        }
    }

    @Nested
    @DisplayName("Security Beans")
    class SecurityBeansTests {

        @Test
        @DisplayName("should configure password encoder and security manager")
        void shouldConfigureSecurityBeans() {
            assertThat(applicationContext.containsBean("passwordEncoder")).isTrue();
            assertThat(applicationContext.containsBean("securityManager")).isTrue();
        }
    }

    @Nested
    @DisplayName("Testcontainers Beans")
    class ContainerBeansTests {

        @Test
        @DisplayName("should register Redis and PostgreSQL container beans")
        void shouldRegisterContainerBeans() {
            assertThat(applicationContext.containsBean("redisContainer")).isTrue();
            assertThat(applicationContext.containsBean("postgresContainer")).isTrue();
        }
    }
}
