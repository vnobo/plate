package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.config.TestPathProperties;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for integration tests.
 * <p>
 * Provides shared infrastructure setup (Testcontainers), WebTestClient configuration,
 * and a JWT login helper. API endpoint paths are injected from {@code test.paths.*} via
 * {@link TestPathProperties}.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
@EnableConfigurationProperties(TestPathProperties.class)
public abstract class AbstractIntegrationTests {

    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "123456";
    protected static final String USER_USERNAME = "user";
    protected static final String USER_PASSWORD = "123456";

    @Autowired
    private TestPathProperties paths;

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    /**
     * Validates that required configuration properties were successfully injected.
     */
    @PostConstruct
    void validatePaths() {
        assertThat(paths).as("TestPathProperties bean").isNotNull();
        assertThat(paths.getOauth2Base()).as("test.paths.oauth2-base").isNotBlank();
        assertThat(paths.getCaptchaBase()).as("test.paths.captcha-base").isNotBlank();
    }

    protected String secPrefix() { return paths.getSecPrefix(); }

    protected String oauth2Base() { return paths.getOauth2Base(); }

    protected String captchaBase() { return paths.getCaptchaBase(); }

    protected String relPrefix() { return paths.getRelPrefix(); }

    @BeforeEach
    void setUpWebTestClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Logs in with the given credentials via the JWT login endpoint and returns the access
     * token to be used as {@code Authorization: Bearer <token>}.
     */
    protected String loginAndGetToken(String username, String password) {
        Map<?, ?> body = webTestClient.post().uri(secPrefix() + "/oauth2/login")
                .bodyValue(Map.of("username", username, "password", password))
                .exchange().expectStatus().isOk()
                .expectBody(Map.class)
                .returnResult().getResponseBody();
        assertThat(body).as("Login response for user '%s'", username).isNotNull();
        String token = String.valueOf(body.get("accessToken"));
        assertThat(token).as("Access token for user '%s'", username).isNotBlank();
        return token;
    }
}
