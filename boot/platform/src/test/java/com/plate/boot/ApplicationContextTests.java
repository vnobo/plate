package com.plate.boot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Application context integration tests.
 * <p>
 * Verifies that the Spring application context loads successfully
 * and all core infrastructure beans are properly configured.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@DisplayName("Application Context Integration Tests")
class ApplicationContextTests extends AbstractIntegrationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertAll("Core infrastructure beans should exist",
                () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                () -> assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue(),
                () -> assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue(),
                () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue(),
                () -> assertThat(applicationContext.containsBean("securityManager")).isTrue()
        );
    }

    @Test
    @DisplayName("Security-related beans should be configured")
    void shouldVerifySecurityBeans() {
        assertAll("Security bean verification",
                () -> assertThat(applicationContext.containsBean("passwordEncoder")).isTrue(),
                () -> assertThat(applicationContext.containsBean("securityManager")).isTrue()
        );
    }

    @Test
    @DisplayName("Infrastructure container beans should be available")
    void shouldVerifyInfrastructureBeans() {
        assertAll("Infrastructure bean verification",
                () -> assertThat(applicationContext.containsBean("redisContainer")).isTrue(),
                () -> assertThat(applicationContext.containsBean("postgresContainer")).isTrue()
        );
    }
}
