package com.plate.boot.security.core.user;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UsersController Integration Test
 *
 * <p>This test class provides comprehensive integration test cases for UsersController, covering the entire request chain.
 * Test scenarios include:</p>
 * <ul>
 *   <li>1) User search functionality</li>
 *   <li>2) User pagination functionality</li>
 *   <li>3) User creation functionality</li>
 *   <li>4) User modification functionality</li>
 *   <li>5) User deletion functionality</li>
 * </ul>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersControllerTest {

    private static final Logger log = LoggerFactory.getLogger(UsersControllerTest.class);

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
    @DisplayName("Should search users successfully")
    void shouldSearchUsersSuccessfully() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("username", "admin")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRes.class)
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                    assertThat(response.getResponseBody()).hasSizeGreaterThan(0);
                });
    }

    @Test
    @Order(2)
    @DisplayName("Should page users successfully")
    void shouldPageUsersSuccessfully() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/page")
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
    @Order(3)
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        UserReq request = new UserReq();
        request.setUsername("testuser");
        request.setName("Test User");
        request.setPassword("123456");

        webTestClient.post()
                .uri("/users/save")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserRes.class)
                .value(user -> {
                    assertThat(user.getUsername()).isEqualTo("testuser");
                    assertThat(user.getName()).isEqualTo("Test User");
                    assertThat(user.getId()).isNotNull();
                });
    }

    @Test
    @Order(4)
    @DisplayName("Should modify user successfully")
    void shouldModifyUserSuccessfully() {
        // First search for a user to modify
        List<UserRes> users = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("username", "testuser")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRes.class)
                .returnResult()
                .getResponseBody();

        if (users != null && !users.isEmpty()) {
            UserRes user = users.get(0);
            UserReq request = new UserReq();
            request.setId(user.getId());
            request.setUsername(user.getUsername());
            request.setName("Modified Test User");
            request.setPassword("123456");

            webTestClient.post()
                    .uri("/users/save")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(UserRes.class)
                    .value(modifiedUser -> {
                        assertThat(modifiedUser.getName()).isEqualTo("Modified Test User");
                    });
        }
    }

    @Test
    @Order(5)
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // First search for a user to delete
        List<UserRes> users = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/search")
                        .queryParam("username", "testuser")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UserRes.class)
                .returnResult()
                .getResponseBody();

        if (users != null && !users.isEmpty()) {
            UserRes user = users.get(0);
            UserReq request = new UserReq();
            request.setId(user.getId());

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/users/delete")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isOk();
        }
    }
}