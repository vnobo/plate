package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
@ActiveProfiles("test")
@Sql("/db/migration/V1.0.4__InitTestData.sql")
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
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @AfterEach
    void tearDown() {
        log.debug("测试方法执行完成");
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
        @DisplayName("获取 CSRF 令牌 - 成功")
        @Order(1)
        void shouldGetCsrfToken() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").exists()
                    .jsonPath("$.headerName").isEqualTo("X-CSRF-TOKEN")
                    .jsonPath("$.parameterName").isEqualTo("_csrf");
        }

        @Test
        @DisplayName("CSRF 令牌格式验证")
        @Order(2)
        void shouldValidateCsrfTokenFormat() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").value(token -> {
                        assertNotNull(token);
                        assertTrue(token.toString().length() > 10);
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
            String credentials = Base64.getEncoder()
                    .encodeToString((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes());

            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo(ADMIN_USERNAME)
                    .jsonPath("$.authorities").exists()
                    .jsonPath("$.sessionId").exists();
        }

        @Test
        @DisplayName("管理员权限验证")
        @Order(2)
        void shouldVerifyAdminAuthorities() {
            String credentials = Base64.getEncoder()
                    .encodeToString((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes());

            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .header("Authorization", "Basic " + credentials)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.authorities[?(@.authority == 'ROLE_SYSTEM_ADMINISTRATORS')]").exists();
        }

        @Test
        @DisplayName("管理员修改密码 - 成功")
        @Order(3)
        void shouldChangeAdminPasswordSuccessfully() {
            String credentials = Base64.getEncoder()
                    .encodeToString((ADMIN_USERNAME + ":" + ADMIN_PASSWORD).getBytes());

            var changePasswordRequest = """
                    {
                        "password": "123456",
                        "newPassword": "newPassword123"
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .header("Authorization", "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo(ADMIN_USERNAME);
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
                    .jsonPath("$.username").isEqualTo(USER_USERNAME)
                    .jsonPath("$.authorities").exists()
                    .jsonPath("$.sessionId").exists();
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
                    .jsonPath("$.authorities[?(@.authority == 'ROLE_USER')]").exists();
        }

        @Test
        @DisplayName("普通用户修改密码 - 成功")
        @Order(3)
        void shouldChangeUserPasswordSuccessfully() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            var changePasswordRequest = """
                    {
                        "password": "123456",
                        "newPassword": "userNewPassword123"
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .header("Authorization", "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo(USER_USERNAME);
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
                    .expectStatus().isUnauthorized();
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
        @DisplayName("密码修改 - 新旧密码相同")
        @Order(1)
        void shouldRejectSamePassword() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            var changePasswordRequest = """
                    {
                        "password": "123456",
                        "newPassword": "123456"
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .header("Authorization", "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().is4xxClientError()
                    .expectBody()
                    .jsonPath("$.message").exists()
                    .consumeWith(result -> {
                        log.info("新旧密码相同异常处理正确");
                    });
        }

        @Test
        @DisplayName("密码修改 - 当前密码错误")
        @Order(2)
        void shouldRejectWrongCurrentPassword() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            var changePasswordRequest = """
                    {
                        "password": "wrongPassword",
                        "newPassword": "newPassword123"
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .header("Authorization", "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().is4xxClientError()
                    .expectBody()
                    .jsonPath("$.message").exists()
                    .consumeWith(result -> {
                        log.info("当前密码错误异常处理正确");
                    });
        }

        @Test
        @DisplayName("密码修改 - 缺少必填字段")
        @Order(3)
        void shouldRejectMissingRequiredFields() {
            String credentials = Base64.getEncoder()
                    .encodeToString((USER_USERNAME + ":" + USER_PASSWORD).getBytes());

            var changePasswordRequest = """
                    {
                        "password": "",
                        "newPassword": ""
                    }
                    """;

            webTestClient.post()
                    .uri("/sec/v1/oauth2/change/password")
                    .header("Authorization", "Basic " + credentials)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(changePasswordRequest))
                    .exchange()
                    .expectStatus().is4xxClientError();
        }
    }
}
