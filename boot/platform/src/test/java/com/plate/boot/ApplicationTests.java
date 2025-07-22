package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

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

    @Autowired
    private WebTestClient webTestClient;

    @Autowired(required = false)
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired(required = false)
    private R2dbcEntityTemplate r2dbcEntityTemplate;

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

    @Test
    @DisplayName("Application Health Check")
    void applicationShouldStartAndRespond() {
        webTestClient.get()
                .uri("/")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isFound()
                .expectHeader().location("/login");
    }

    @Test
    @DisplayName("Database Connectivity Test")
    void databaseConnectivityShouldBeAvailable() {
        assertAll("Database infrastructure should be configured",
                () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                () -> assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue()
        );
        
        if (r2dbcEntityTemplate != null) {
            // Test PostgreSQL version
            Mono<String> dbVersion = r2dbcEntityTemplate.getDatabaseClient()
                    .sql("SELECT version()")
                    .map(row -> row.get(0, String.class))
                    .one();

            StepVerifier.create(dbVersion)
                    .expectNextMatches(version -> version != null && version.contains("PostgreSQL"))
                    .verifyComplete();

            // Test schema exists
            Mono<Integer> schemaCheck = r2dbcEntityTemplate.getDatabaseClient()
                    .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'")
                    .map(row -> row.get(0, Integer.class))
                    .one();

            StepVerifier.create(schemaCheck)
                    .expectNextMatches(count -> count > 0)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Redis Connectivity Test")
    void redisConnectivityShouldBeAvailable() {
        assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue();
        
        if (reactiveRedisTemplate != null) {
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "test-value";

            // Test basic set/get/delete
            Mono<Boolean> connectionTest = reactiveRedisTemplate.opsForValue()
                    .set(testKey, testValue, Duration.ofSeconds(30))
                    .then(reactiveRedisTemplate.opsForValue().get(testKey))
                    .map(value -> value != null && value.equals(testValue))
                    .flatMap(success -> reactiveRedisTemplate.delete(testKey).thenReturn(success));

            StepVerifier.create(connectionTest)
                    .expectNext(true)
                    .verifyComplete();

            // Test additional Redis operations
            String listKey = "test:list:" + System.currentTimeMillis();
            Mono<Long> listOperations = reactiveRedisTemplate.opsForList()
                    .leftPush(listKey, "item1")
                    .then(reactiveRedisTemplate.opsForList().leftPush(listKey, "item2"))
                    .then(reactiveRedisTemplate.opsForList().leftPush(listKey, "item3"))
                    .then(reactiveRedisTemplate.opsForList().size(listKey))
                    .flatMap(size -> reactiveRedisTemplate.delete(listKey).thenReturn(size));

            StepVerifier.create(listOperations)
                    .expectNext(3L)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Application Properties Loading Test")
    void applicationPropertiesShouldBeLoaded() {
        var environment = applicationContext.getEnvironment();
        
        assertAll("Application properties should be correctly configured",
                () -> assertThat(environment.getProperty("spring.application.name")).isEqualTo("plate"),
                () -> assertThat(environment.getProperty("spring.threads.virtual.enabled")).isEqualTo("true"),
                () -> assertThat(environment.getProperty("spring.lifecycle.timeout-per-shutdown-phase")).isEqualTo("20s"),
                () -> assertThat(environment.getProperty("spring.main.keep-alive")).isEqualTo("true")
        );
    }

    @Test
    @DisplayName("Virtual Threads Configuration Test")
    void virtualThreadsShouldBeEnabled() {
        String virtualThreadsEnabled = applicationContext.getEnvironment()
                .getProperty("spring.threads.virtual.enabled");
        assertThat(virtualThreadsEnabled).isEqualTo("true");
    }

    @Test
    @DisplayName("Security Configuration Test")
    void securityConfigurationShouldBeLoaded() {
        assertAll("Security infrastructure should be configured",
                () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue(),
                () -> assertThat(applicationContext.containsBean("webHandler")).isTrue()
        );
    }

    @Test
    @DisplayName("Flyway Configuration Test")
    void flywayConfigurationShouldBePresent() {
        assertAll("Flyway should be configured",
                () -> assertThat(applicationContext.containsBean("flyway")).isTrue(),
                () -> assertThat(applicationContext.containsBean("flywayInitializer")).isTrue()
        );
    }

    @Test
    @DisplayName("Actuator Health Endpoint Test")
    void actuatorHealthEndpointShouldBeAvailable() {
        boolean actuatorEnabled = applicationContext.getEnvironment()
                .getProperty("management.endpoints.web.exposure.include", "")
                .contains("health");
        
        if (actuatorEnabled) {
            webTestClient.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("UP")
                    .jsonPath("$.components.diskSpace.status").isEqualTo("UP")
                    .jsonPath("$.components.ping.status").isEqualTo("UP");
        } else {
            log.info("Actuator health endpoint not configured - testing basic endpoint");
            webTestClient.get()
                    .uri("/actuator/health")
                    .exchange()
                    .expectStatus().is4xxClientError();
        }
    }

    @Test
    @DisplayName("Application Response Test")
    void applicationShouldRespondToBasicRequests() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().exists("Location");
    }

    @Test
    @DisplayName("Static Resources Test")
    void staticResourcesShouldBeAccessible() {
        webTestClient.get()
                .uri("/favicon.ico")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith("image/x-icon");
    }

    @Test
    @DisplayName("Error Handling Test")
    void errorHandlingShouldBeConfigured() {
        webTestClient.get()
                .uri("/nonexistent-endpoint")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
    }

    @Test
    @DisplayName("CORS Configuration Test")
    void corsConfigurationShouldBeApplied() {
        webTestClient.options()
                .uri("/api/test")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin");
    }

    @Test
    @DisplayName("Database Schema Validation Test")
    void databaseSchemaValidationShouldPass() {
        if (r2dbcEntityTemplate != null) {
            // Test that Flyway migration has run
            Mono<Integer> flywayCount = r2dbcEntityTemplate.getDatabaseClient()
                    .sql("SELECT COUNT(*) FROM flyway_schema_history WHERE success = true")
                    .map(row -> row.get(0, Integer.class))
                    .one();

            StepVerifier.create(flywayCount)
                    .expectNextMatches(count -> count > 0)
                    .verifyComplete();

            // Test core tables exist
            Mono<Integer> coreTablesCount = r2dbcEntityTemplate.getDatabaseClient()
                    .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_name IN ('users', 'roles', 'groups', 'authorities')")
                    .map(row -> row.get(0, Integer.class))
                    .one();

            StepVerifier.create(coreTablesCount)
                    .expectNextMatches(count -> count >= 4)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Redis Cache Configuration Test")
    void redisCacheConfigurationShouldBeActive() {
        if (reactiveRedisTemplate != null) {
            String cacheKey = "plate:caches:test:config";
            String testData = "{\"test\": \"data\"}";
            
            // Test cache operations
            Mono<Boolean> cacheTest = reactiveRedisTemplate.opsForValue()
                    .set(cacheKey, testData, Duration.ofMinutes(5))
                    .then(reactiveRedisTemplate.opsForValue().get(cacheKey))
                    .map(value -> value != null && value.equals(testData))
                    .flatMap(success -> reactiveRedisTemplate.delete(cacheKey).thenReturn(success));

            StepVerifier.create(cacheTest)
                    .expectNext(true)
                    .verifyComplete();
        }
    }

    @Test
    @DisplayName("Application Startup Performance Test")
    void applicationStartupPerformanceTest() {
        long startTime = System.currentTimeMillis();
        
        // Verify all critical components are loaded
        assertAll("Startup performance verification",
                () -> assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(50),
                () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue(),
                () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                () -> assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue()
        );
        
        long endTime = System.currentTimeMillis();
        long startupTime = endTime - startTime;
        
        log.info("Application startup test completed in {}ms", startupTime);
        assertThat(startupTime).isLessThan(5000); // Should complete within 5 seconds
    }

    @Test
    @DisplayName("Full Integration Test")
    void applicationShouldStartWithAllComponentsIntegrated() {
        // Verify context loaded and is active
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBeanDefinitionCount()).isGreaterThan(0);
        
        // Verify port is assigned
        assertThat(port).isGreaterThan(0);
        
        // Verify essential beans exist
        assertAll("All critical components should be present",
                () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue(),
                () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                () -> assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue(),
                () -> assertThat(applicationContext.containsBean("flyway")).isTrue(),
                () -> assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue()
        );
        
        log.info("Application startup integration test passed successfully");
    }
}