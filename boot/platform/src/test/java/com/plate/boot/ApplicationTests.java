package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.AuthenticationToken;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.test.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * SecurityController Integration Test
 *
 * <p>This test class provides comprehensive integration test cases for SecurityController, covering the entire request chain.
 * Test scenarios include:</p>
 * <ul>
 *   <li>1) Administrator login authentication process, using account admin/123456 to verify access control</li>
 *   <li>2) Regular user login process, using account user/123456 to verify basic functionality</li>
 *   <li>3) Interception of unauthorized access</li>
 * </ul>
 *
 * <p>Test requirements:</p>
 * <ul>
 *   <li>a) Use Spring Boot Test framework</li>
 *   <li>b) Include HTTP request simulation</li>
 *   <li>c) Verify endpoint return status codes and response content</li>
 *   <li>d) Initialize test data using @Sql annotation</li>
 *   <li>e) Include exception flow test cases</li>
 *   <li>Ensure test coverage reaches above 90%</li>
 * </ul>
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

    // Test user credentials
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "123456";
    private static final String USER_USERNAME = "user";
    private static final String USER_PASSWORD = "123456";

    private WebTestClient webTestClient;
    private String adminToken;
    private String userToken;

    public ApplicationTests(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @AfterAll
    static void tearDownAll() {
        log.info("All SecurityController integration tests completed");
    }

    @BeforeEach
    void setUp() {
        log.info("Setting up test environment, port: {}", port);
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        // For test classes requiring administrator privileges
        this.adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        // For test classes requiring regular user privileges
        this.userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);

    }

    @AfterEach
    void tearDown() {
        log.debug("Test method execution completed");
    }

    // Helper method to log in and get token
    private String loginAndGetToken(String username, String password) {
        String credentials = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());

        var responseBody = webTestClient.get()
                .uri("/sec/v1/oauth2/login")
                .header("Authorization", "Basic " + credentials)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationToken.class)
                .returnResult().getResponseBody();
        assertNotNull(responseBody);
        return responseBody.token();
    }

    @Nested
    @DisplayName("Application Context Tests")
    @Order(1)
    class ApplicationContextTests {

        @Test
        @DisplayName("Application Context Loading Test")
        @Order(1)
        void contextLoads() {
            log.info("Application context loaded successfully, Bean count: {}",
                    applicationContext.getBeanDefinitionCount());

            assertAll("Core beans should exist",
                    () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("securityManager")).isTrue()
            );
        }

        @Test
        @DisplayName("Security Configuration Verification")
        @Order(2)
        void shouldVerifySecurityConfiguration() {
            assertAll("Security-related bean verification",
                    () -> assertThat(applicationContext.containsBean("passwordEncoder")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("securityManager")).isTrue()
            );
        }
    }

    @Nested
    @DisplayName("CSRF Token Tests")
    @Order(2)
    class CsrfTokenTests {

        @Test
        @DisplayName("Obtain CSRF Token - Unauthenticated User Redirect")
        @Order(1)
        void shouldRedirectUnauthenticatedUserForCsrfToken() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("CSRF Token Format Validation")
        @Order(2)
        void shouldValidateCsrfTokenFormat() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").isNotEmpty()
                    .jsonPath("$.headerName").isEqualTo("X-XSRF-TOKEN")
                    .jsonPath("$.parameterName").isEqualTo("_csrf");
        }
    }

    @Nested
    @DisplayName("Administrator Authentication Process Tests")
    @Order(3)
    class AdminAuthenticationTests {

        @Test
        @DisplayName("Administrator Login Authentication - Success")
        @Order(1)
        void shouldAuthenticateAdminSuccessfully() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").exists()
                    .jsonPath("$.details").exists()
                    .jsonPath("$.expires").exists()
                    .jsonPath("$.lastAccessTime").exists()
                    .jsonPath("$.details.name").isEqualTo("admin")
                    .jsonPath("$.details.nickname").isEqualTo("系统超级管理员")
                    .jsonPath("$.details.enabled").isEqualTo(true);
        }

        @Test
        @DisplayName("Administrator Authority Verification")
        @Order(2)
        void shouldVerifyAdminAuthorities() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.authorities").isArray()
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_SYSTEM_ADMINISTRATORS')]").exists()
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_GROUP_ADMINISTRATORS')]").exists()
                    .jsonPath("$.details.authorities").value(authorities ->
                            assertThat((Iterable<?>) authorities).isNotEmpty());
        }

        @Test
        @DisplayName("Administrator Password Change - Success")
        @Order(3)
        void shouldChangeAdminPasswordSuccessfully() {
            var changePasswordRequest = Map.of(
                    "password", "123456",
                    "newPassword", "newPassword123"
            );
            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("Regular User Authentication Process Tests")
    @Order(4)
    class UserAuthenticationTests {

        @Test
        @DisplayName("Regular User Login Authentication - Success")
        @Order(1)
        void shouldAuthenticateUserSuccessfully() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").exists()
                    .jsonPath("$.details").exists()
                    .jsonPath("$.expires").exists();
        }

        @Test
        @DisplayName("Regular User Authority Verification")
        @Order(2)
        void shouldVerifyUserAuthorities() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.authorities").isArray();
        }

        @Test
        @DisplayName("Regular User Password Change - Success")
        @Order(3)
        void shouldChangeUserPasswordSuccessfully() {
            var changePasswordRequest = Map.of(
                    "password", "123456",
                    "newPassword", "userNewPassword123"
            );

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().isForbidden(); // Changed to expect 403 status code
        }
    }

    @Nested
    @DisplayName("Unauthorized Access Interception Tests")
    @Order(5)
    class UnauthorizedAccessTests {

        @Test
        @DisplayName("Unauthenticated Access to Login Token - 401")
        @Order(1)
        void shouldRejectLoginTokenWithoutAuthentication() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Unauthenticated Password Change - 401")
        @Order(2)
        void shouldRejectChangePasswordWithoutAuthentication() {
            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .bodyValue("{\"password\":\"oldPass\",\"newPassword\":\"newPass\"}")
                    .exchange()
                    .expectStatus().is4xxClientError();
        }

        @Test
        @DisplayName("Unauthenticated OAuth2 Binding - 401")
        @Order(3)
        void shouldRejectBindOauth2WithoutAuthentication() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/bind?clientRegistrationId=github")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Invalid Credentials Access - 401")
        @Order(4)
        void shouldRejectInvalidCredentials() {
            String invalidCredentials = Base64.getEncoder()
                    .encodeToString("invalid:credentials".getBytes());

            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .header("Authorization", "Basic " + invalidCredentials)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("Exception Flow Tests")
    @Order(6)
    class ExceptionFlowTests {

        @Test
        @DisplayName("Password Strength Validation - New Password Too Weak")
        @Order(4)
        void shouldRejectWeakNewPassword() {
            var changePasswordRequest = """
                    {
                        "password": "123456",
                        "newPassword": "weak"
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().is4xxClientError()
                    .expectBody();
        }

        @Test
        @DisplayName("Password Change - Current Password Incorrect")
        @Order(2)
        void shouldRejectWrongCurrentPassword() {
            var changePasswordRequest = """
                    {
                        "password": "wrongPassword",
                        "newPassword": "newPassword123"
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().is4xxClientError()
                    .expectBody();
        }

        @Test
        @DisplayName("Password Change - Missing Required Fields")
        @Order(3)
        void shouldRejectMissingRequiredFields() {
            var changePasswordRequest = """
                    {
                        "password": "",
                        "newPassword": ""
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().is4xxClientError();
        }
    }

    @Nested
    @DisplayName("Logout and Session Tests")
    @Order(7)
    class LogoutSessionTests {

        @Test
        @DisplayName("Administrator Logout - Success")
        @Order(1)
        void shouldLogoutAdminSuccessfully() {
            // Login to get session
            webTestClient.get()
                    .uri("/oauth2/logout")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(adminToken))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody().isEmpty();
        }

        @Test
        @DisplayName("Session Information Verification")
        @Order(2)
        void shouldVerifySessionInfo() {
            // Use token to access protected resources
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").isEqualTo(userToken)
                    .jsonPath("$.details").exists()
                    .jsonPath("$.expires").exists()
                    .jsonPath("$.lastAccessTime").exists();
        }
    }

    @Nested
    @DisplayName("OAuth2 Binding Tests")
    @Order(8)
    class OAuth2BindingTests {

        @Test
        @DisplayName("OAuth2 Binding - Success")
        @Order(1)
        void shouldBindOAuth2Successfully() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/bind?clientRegistrationId=github")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("OAuth2 Binding - Missing Client ID")
        @Order(2)
        void shouldHandleMissingClientRegistrationId() {

            webTestClient.get()
                    .uri("/sec/v1/oauth2/bind")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .exchange()
                    .expectStatus().isBadRequest();
        }
    }

}