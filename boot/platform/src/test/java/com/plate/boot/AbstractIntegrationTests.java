package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.config.TestPathProperties;
import com.plate.boot.security.AuthenticationToken;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

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

    // ---- Test credentials ----
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "123456";
    protected static final String USER_USERNAME = "user";
    protected static final String USER_PASSWORD = "123456";

    // ---- Path configuration (private — read-only access via methods) ----
    @Autowired
    private TestPathProperties paths;

    @LocalServerPort
    protected int port;

    protected WebTestClient webTestClient;

    /**
     * Validates that required configuration properties were successfully injected.
     * Fails fast with a clear message rather than deferring to a NullPointerException at usage time.
     */
    @PostConstruct
    void validatePaths() {
        assertThat(paths).as("TestPathProperties bean").isNotNull();
        assertThat(paths.getOauth2Base()).as("test.paths.oauth2-base").isNotBlank();
        assertThat(paths.getCaptchaBase()).as("test.paths.captcha-base").isNotBlank();
    }

    // ---- Read-only path accessors ----

    /** Security API path prefix, e.g. {@code "/sec/v1"}. */
    protected String secPrefix() { return paths.getSecPrefix(); }

    /** OAuth2 endpoint base path, e.g. {@code "/sec/v1/oauth2"}. */
    protected String oauth2Base() { return paths.getOauth2Base(); }

    /** Captcha endpoint base path, e.g. {@code "/sec/v1/captcha"}. */
    protected String captchaBase() { return paths.getCaptchaBase(); }

    /** Relational API path prefix, e.g. {@code "/rel/v1"}. */
    protected String relPrefix() { return paths.getRelPrefix(); }

    // ---- Infrastructure setup ----

    /**
     * Container infrastructure is managed by {@link InfrastructureConfiguration}
     * via {@code @ServiceConnection} — no manual property registration needed.
     */

    @BeforeEach
    void setUpWebTestClient() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    // ---- Authentication helpers ----

    /**
     * Logs in with the given credentials using HTTP Basic authentication
     * and returns the session token.
     *
     * @param username the username
     * @param password the password
     * @return the authentication token string (never null)
     */
    protected String loginAndGetToken(String username, String password) {
        var responseBody = loginWithBasicAuth(username, password);
        assertThat(responseBody).as("Login response for user '%s'", username).isNotNull();
        assertThat(responseBody.token()).as("Token for user '%s'", username).isNotBlank();
        return responseBody.token();
    }

    /**
     * Logs in with the given credentials using HTTP Basic authentication
     * and returns the full {@link AuthenticationToken} response body.
     *
     * @param username the username
     * @param password the password
     * @return the full authentication token response
     */
    protected AuthenticationToken loginWithBasicAuth(String username, String password) {
        String credentials = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return webTestClient.get().uri(oauth2Base() + "/login")
                .header("Authorization", "Basic " + credentials)
                .exchange().expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .returnResult().getResponseBody();
    }

    /**
     * Encodes username:password as a Base64 Basic Auth credential string.
     *
     * @param username the username
     * @param password the password
     * @return the Base64-encoded credential string (without "Basic " prefix)
     */
    protected String encodeBasicCredentials(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }
}
