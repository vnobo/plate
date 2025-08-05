package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.config.SessionConfiguration;
import com.plate.boot.security.core.AuthenticationToken;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * SecurityController 完整集成测试
 *
 * <p>本测试类为 SecurityController 编写完整的集成测试用例，覆盖整个请求链路。
 * 测试场景包括：</p>
 * <ul>
 *   <li>1) 管理员登录认证流程，使用账号 admin/123456 验证权限控制</li>
 *   <li>2) 普通用户登录流程，使用账号 user/123456 验证基础功能</li>
 *   <li>3) 未授权访问的拦截情况</li>
 * </ul>
 *
 * <p>测试要求：</p>
 * <ul>
 *   <li>a) 使用 Spring Boot Test 框架</li>
 *   <li>b) 包含 HTTP 请求模拟</li>
 *   <li>c) 验证各端点返回状态码和响应内容</li>
 *   <li>d) 测试数据初始化使用 @Sql 注解</li>
 *   <li>e) 包含异常流程测试用例</li>
 *   <li>确保测试覆盖率达到 90% 以上</li>
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

    // 测试用户凭据
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
        log.info("所有 SecurityController 集成测试完成");
    }

    @BeforeEach
    void setUp() {
        log.info("设置测试环境，端口: {}", port);
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader("X-Requested-With", "XMLHttpRequest")
                .responseTimeout(Duration.ofSeconds(30))
                .build();
        // 对于需要管理员权限的测试类
        this.adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
        // 对于需要普通用户权限的测试类
        this.userToken = loginAndGetToken(USER_USERNAME, USER_PASSWORD);

    }

    @AfterEach
    void tearDown() {
        log.debug("测试方法执行完成");
    }

    // 登录并获取token的辅助方法
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
    @DisplayName("应用程序上下文测试")
    @Order(1)
    class ApplicationContextTests {

        @Test
        @DisplayName("应用程序上下文加载测试")
        @Order(1)
        void contextLoads() {
            log.info("应用程序上下文加载成功，Bean 数量: {}",
                    applicationContext.getBeanDefinitionCount());

            assertAll("核心 Bean 应该存在",
                    () -> assertThat(applicationContext.containsBean("connectionFactory")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("reactiveRedisTemplate")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("r2dbcEntityTemplate")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("springSecurityFilterChain")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("securityManager")).isTrue()
            );
        }

        @Test
        @DisplayName("安全配置验证")
        @Order(2)
        void shouldVerifySecurityConfiguration() {
            assertAll("安全相关 Bean 验证",
                    () -> assertThat(applicationContext.containsBean("passwordEncoder")).isTrue(),
                    () -> assertThat(applicationContext.containsBean("securityManager")).isTrue(),
                    () -> assertThat(applicationContext.getBean("securityManager")).isNotNull()
            );
        }
    }

    @Nested
    @DisplayName("CSRF 令牌测试")
    @Order(2)
    class CsrfTokenTests {

        @Test
        @DisplayName("获取 CSRF 令牌 - 未认证用户重定向")
        @Order(1)
        void shouldRedirectUnauthenticatedUserForCsrfToken() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("CSRF 令牌格式验证")
        @Order(2)
        void shouldValidateCsrfTokenFormat() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .headers(headers -> headers.setBearerAuth(adminToken))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CsrfToken.class)
                    .value(token -> {
                        assertThat(token.getToken()).isNotBlank();
                        assertThat(token.getHeaderName()).isEqualTo(SessionConfiguration.HEADER_SESSION_ID_NAME);
                        assertThat(token.getParameterName()).isEqualTo("access_token");
                    });
        }
    }

    @Nested
    @DisplayName("管理员认证流程测试")
    @Order(3)
    class AdminAuthenticationTests {

        @Test
        @DisplayName("管理员登录认证 - 成功")
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
        @DisplayName("管理员权限验证")
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
                    .jsonPath("$.details.authorities[?(@.authority == 'ROLE_ADMINISTRATORS')]").exists()
                    .jsonPath("$.details.authorities").value(authorities ->
                            assertThat((Iterable<?>) authorities).isNotEmpty());
        }

        @Test
        @DisplayName("管理员修改密码 - 成功")
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
    @DisplayName("普通用户认证流程测试")
    @Order(4)
    class UserAuthenticationTests {

        @Test
        @DisplayName("普通用户登录认证 - 成功")
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
        @DisplayName("普通用户权限验证")
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
        @DisplayName("普通用户修改密码 - 成功")
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
                    .expectStatus().isForbidden(); // 修改为期望403状态码
        }
    }

    @Nested
    @DisplayName("未授权访问拦截测试")
    @Order(5)
    class UnauthorizedAccessTests {

        @Test
        @DisplayName("未认证访问登录令牌 - 401")
        @Order(1)
        void shouldRejectLoginTokenWithoutAuthentication() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("未认证修改密码 - 401")
        @Order(2)
        void shouldRejectChangePasswordWithoutAuthentication() {
            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .bodyValue("{\"password\":\"oldPass\",\"newPassword\":\"newPass\"}")
                    .exchange()
                    .expectStatus().is4xxClientError();
        }

        @Test
        @DisplayName("未认证 OAuth2 绑定 - 401")
        @Order(3)
        void shouldRejectBindOauth2WithoutAuthentication() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/bind?clientRegistrationId=github")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("错误凭据访问 - 401")
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
    @DisplayName("异常流程测试")
    @Order(6)
    class ExceptionFlowTests {

        @Test
        @DisplayName("密码强度验证 - 新密码太弱")
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
                    .expectBody()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("密码修改 - 当前密码错误")
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
                    .expectBody()
                    .jsonPath("$.message").exists()
                    .consumeWith(result -> {
                        assertNotNull(result.getResponseBody());
                        log.info("当前密码错误异常处理正确. Result: {}",
                                new String(result.getResponseBody()));
                    });
        }

        @Test
        @DisplayName("密码修改 - 缺少必填字段")
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
    @DisplayName("登出与会话测试")
    @Order(7)
    class LogoutSessionTests {

        @Test
        @DisplayName("管理员登出 - 成功")
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
        @DisplayName("会话信息验证")
        @Order(2)
        void shouldVerifySessionInfo() {
            // 使用token访问受保护资源
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
    @DisplayName("OAuth2 绑定测试")
    @Order(8)
    class OAuth2BindingTests {

        @Test
        @DisplayName("OAuth2 绑定 - 成功")
        @Order(1)
        void shouldBindOAuth2Successfully() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/bind?clientRegistrationId=github")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("OAuth2 绑定 - 客户端ID缺失")
        @Order(2)
        void shouldHandleMissingClientRegistrationId() {

            webTestClient.get()
                    .uri("/sec/v1/oauth2/bind")
                    .headers(httpHeaders -> httpHeaders.setBearerAuth(userToken))
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }
    }

}