package com.plate.boot.relational.logger;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Base64;

/**
 * LoggersController Integration Test
 *
 * <p>This test class provides comprehensive integration test cases for LoggersController, covering the entire request chain.
 * Test scenarios include:</p>
 * <ul>
 *   <li>1) Logger pagination functionality</li>
 *   <li>2) Logger search functionality</li>
 * </ul>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoggersControllerTest {

    private static final Logger log = LoggerFactory.getLogger(LoggersControllerTest.class);

    private WebTestClient webTestClient;
    private String adminToken;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .responseTimeout(Duration.ofSeconds(30))
                .build();

        // Login as admin to get token
        String credentials = Base64.getEncoder()
                .encodeToString(("admin:123456").getBytes());

        var responseBody = webTestClient.get()
                .uri("/sec/v1/oauth2/login")
                .header("Authorization", "Basic " + credentials)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .returnResult().getResponseBody();

        this.adminToken = responseBody.token();
    }

    @Test
    @Order(1)
    @DisplayName("Should page loggers successfully")
    void shouldPageLoggersSuccessfully() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/loggers/page")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.totalElements").isNumber();
    }

    @Test
    @Order(2)
    @DisplayName("Should search loggers by operator")
    void shouldSearchLoggersByOperator() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/loggers/page")
                        .queryParam("operator", "admin")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray();
    }
}