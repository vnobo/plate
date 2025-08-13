package com.plate.boot.security.core.group;

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
 * GroupsController Integration Test
 *
 * <p>This test class provides comprehensive integration test cases for GroupsController, covering the entire request chain.
 * Test scenarios include:</p>
 * <ul>
 *   <li>1) Group search functionality</li>
 *   <li>2) Group pagination functionality</li>
 *   <li>3) Group creation functionality</li>
 *   <li>4) Group deletion functionality</li>
 * </ul>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GroupsControllerTest {

    private static final Logger log = LoggerFactory.getLogger(GroupsControllerTest.class);

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
    @DisplayName("Should search groups successfully")
    void shouldSearchGroupsSuccessfully() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/groups/search")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Group.class)
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                });
    }

    @Test
    @Order(2)
    @DisplayName("Should page groups successfully")
    void shouldPageGroupsSuccessfully() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/groups/page")
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
    @DisplayName("Should create group successfully")
    void shouldCreateGroupSuccessfully() {
        GroupReq request = new GroupReq();
        request.setName("Test Group");

        webTestClient.post()
                .uri("/groups/save")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Group.class)
                .value(group -> {
                    assertThat(group.getName()).isEqualTo("Test Group");
                    assertThat(group.getId()).isNotNull();
                });
    }

    @Test
    @Order(4)
    @DisplayName("Should delete group successfully")
    void shouldDeleteGroupSuccessfully() {
        // First search for a group to delete
        List<Group> groups = webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/groups/search")
                        .queryParam("name", "Test Group")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Group.class)
                .returnResult()
                .getResponseBody();

        if (groups != null && !groups.isEmpty()) {
            Group group = groups.get(0);
            GroupReq request = new GroupReq();
            request.setId(group.getId());

            webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                    .uri("/groups/delete")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .body(BodyInserters.fromValue(request))
                    .exchange()
                    .expectStatus().isOk();
        }
    }
}