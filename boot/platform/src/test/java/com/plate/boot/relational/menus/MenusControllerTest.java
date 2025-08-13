package com.plate.boot.relational.menus;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MenusController Integration Test
 *
 * <p>This test class provides comprehensive integration test cases for MenusController, covering the entire request chain.
 * Test scenarios include:</p>
 * <ul>
 *   <li>1) Menu search functionality</li>
 *   <li>2) Menu pagination functionality</li>
 *   <li>3) Personalized menu loading</li>
 *   <li>4) Menu saving functionality</li>
 *   <li>5) Menu deletion functionality</li>
 * </ul>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MenusControllerTest {

    private static final Logger log = LoggerFactory.getLogger(MenusControllerTest.class);

    @Autowired
    private MenusService menusService;

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
    @DisplayName("Should search menus successfully")
    void shouldSearchMenusSuccessfully() {
        MenuReq request = new MenuReq();
        request.setName("系统");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/menus/search")
                        .queryParam("name", "系统")
                        .build())
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Menu.class)
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                    assertThat(response.getResponseBody()).hasSizeGreaterThan(0);
                });
    }

    @Test
    @Order(2)
    @DisplayName("Should page menus successfully")
    void shouldPageMenusSuccessfully() {
        MenuReq request = new MenuReq();
        request.setName("系统");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/menus/page")
                        .queryParam("name", "系统")
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
    @DisplayName("Should load personalized menus successfully")
    void shouldLoadPersonalizedMenusSuccessfully() {
        webTestClient.get()
                .uri("/menus/me")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Menu.class)
                .consumeWith(response -> {
                    assertThat(response.getResponseBody()).isNotNull();
                    assertThat(response.getResponseBody()).hasSizeGreaterThan(0);
                });
    }

    @Test
    @Order(4)
    @DisplayName("Should save new menu successfully")
    void shouldSaveNewMenuSuccessfully() {
        MenuReq request = new MenuReq();
        request.setName("Test Menu");
        request.setAuthority("ROLE_TEST_MENU");
        request.setType(Menu.MenuType.MENU);
        request.setPcode(UUID.randomUUID());

        webTestClient.post()
                .uri("/menus/save")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Menu.class)
                .value(menu -> {
                    assertThat(menu.getName()).isEqualTo("Test Menu");
                    assertThat(menu.getAuthority()).isEqualTo("ROLE_TEST_MENU");
                    assertThat(menu.getId()).isNotNull();
                });
    }

    @Test
    @Order(5)
    @DisplayName("Should delete menu successfully")
    void shouldDeleteMenuSuccessfully() {
        // First create a menu to delete
        MenuReq createRequest = new MenuReq();
        createRequest.setName("ToDelete Menu");
        createRequest.setAuthority("ROLE_TO_DELETE_MENU");
        createRequest.setType(Menu.MenuType.MENU);
        createRequest.setPcode(UUID.randomUUID());

        Menu createdMenu = webTestClient.post()
                .uri("/menus/save")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .body(BodyInserters.fromValue(createRequest))
                .exchange()
                .expectStatus().isOk()
                .expectBody(Menu.class)
                .returnResult()
                .getResponseBody();

        // Now delete the menu
        MenuReq deleteRequest = new MenuReq();
        deleteRequest.setId(createdMenu.getId());
        deleteRequest.setCode(createdMenu.getCode());

        webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/menus/delete")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .body(BodyInserters.fromValue(deleteRequest))
                .exchange()
                .expectStatus().isOk();
    }
}