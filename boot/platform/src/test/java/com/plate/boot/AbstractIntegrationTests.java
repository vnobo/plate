package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.config.TestPathProperties;
import com.plate.boot.security.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Abstract base class for integration tests.
 * <p>
 * Provides shared infrastructure setup (Testcontainers), WebTestClient configuration,
 * and common authentication helper methods. All concrete integration test classes
 * should extend this base class.
 * </p>
 * <p>
 * API endpoint paths are injected from {@code test.paths.*} configuration properties
 * via {@link TestPathProperties}, ensuring a single source of truth and consistency
 * with the runtime path-prefix and API-version configuration.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
@EnableConfigurationProperties(TestPathProperties.class)
public abstract class AbstractIntegrationTests {

    private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTests.class);

    // Test user credentials
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "123456";
    protected static final String USER_USERNAME = "user";
    protected static final String USER_PASSWORD = "123456";

    /** Test endpoint paths, injected from {@code test.paths.*} configuration. */
    @Autowired
    protected TestPathProperties paths;

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        InfrastructureConfiguration.POSTGRES_CONTAINER.start();
        var postgres = InfrastructureConfiguration.POSTGRES_CONTAINER;
        // Configure R2DBC connection
        registry.add("spring.r2dbc.url", () -> String.format("r2dbc:postgresql://%s:%d/%s",
                postgres.getHost(), postgres.getFirstMappedPort(), postgres.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        // Configure Flyway (JDBC) connection
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);

        InfrastructureConfiguration.REDIS_CONTAINER.start();
        var redis = InfrastructureConfiguration.REDIS_CONTAINER;
        // Configure Redis connection
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);

        log.info("Test infrastructure containers started");
    }

    @BeforeEach
    void setUpWebTestClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Logs in with the given credentials using HTTP Basic authentication
     * and returns the session token.
     *
     * @param username the username
     * @param password the password
     * @return the authentication token string
     */
    protected String loginAndGetToken(String username, String password) {
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        var responseBody = webTestClient.get().uri(paths.getOauth2Base() + "/login")
                .header("Authorization", "Basic " + credentials)
                .exchange().expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .returnResult().getResponseBody();
        assertNotNull(responseBody, "Login response should not be null for user: " + username);
        return responseBody.token();
    }
}
