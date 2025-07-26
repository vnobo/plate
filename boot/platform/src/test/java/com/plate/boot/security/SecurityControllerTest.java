package com.plate.boot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.SecurityController.ChangePasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SecurityController的全面单元测试类
 *  admin 具有超级管理员权限  密码 123456
 *  user 具有普通用户权限 密码 123456
 *  测试使用以上两个账号测试
 * <p>测试覆盖SecurityController的所有公共方法，包括：</p>
 * <ul>
 *   <li>登录令牌获取 - loginToken</li>
 *   <li>CSRF令牌获取 - csrfToken</li>
 *   <li>OAuth2客户端绑定 - bindOauth2</li>
 *   <li>密码修改 - changePassword</li>
 * </ul>
 *
 * <p>每个方法都包含正常场景和异常场景的测试用例，确保全面的测试覆盖。</p>
 *
 * @author Alex
 */
@WebFluxTest(controllers = SecurityController.class)
@Import({SecurityController.class, InfrastructureConfiguration.class})
class SecurityControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SecurityManager securityManager;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ServerOAuth2AuthorizedClientRepository clientRepository;

    private UserDetails testUser;
    private List<GrantedAuthority> testAuthorities;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testAuthorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        testUser = User.builder()
                .username("admin")
                .password("123456")
                .authorities(testAuthorities)
                .build();
    }

    @Nested
    @DisplayName("登录令牌获取测试")
    class LoginTokenTests {

        @Test
        @DisplayName("成功获取认证令牌 - 已认证用户")
        @WithMockUser(username = "admin", password = "123456", authorities = {"ROLE_USER", "ROLE_ADMIN"})
        void shouldReturnAuthenticationTokenForAuthenticatedUser() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").value(token -> assertThat(token).isNotNull())
                    .jsonPath("$.expires").value(expires -> assertThat(expires).isInstanceOf(Number.class))
                    .jsonPath("$.lastAccessTime").value(lastAccessTime -> assertThat(lastAccessTime).isInstanceOf(Number.class))
                    .jsonPath("$.details.username").isEqualTo("admin")
                    .jsonPath("$.details.authorities").value(authorities -> assertThat(authorities).isInstanceOf(List.class));
        }

        @Test
        @DisplayName("未认证用户访问应返回302重定向")
        void shouldReturn302ForUnauthenticatedUser() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }

        @Test
        @DisplayName("不同用户获取不同的令牌")
        @WithMockUser(username = "anotheruser", password = "password", authorities = {"ROLE_USER"})
        void shouldReturnDifferentTokenForDifferentUser() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.details.username").isEqualTo("anotheruser")
                    .jsonPath("$.details.authorities").isArray()
                    .jsonPath("$.details.authorities.length()").isEqualTo(1)
                    .jsonPath("$.details.authorities[0].authority").isEqualTo("ROLE_USER");
        }
    }

    @Nested
    @DisplayName("CSRF令牌获取测试")
    class CsrfTokenTests {

        @Test
        @DisplayName("成功获取CSRF令牌")
        @WithMockUser(username = "admin")
        void shouldReturnCsrfTokenWhenPresent() {
            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.headerName").isEqualTo("X-CSRF-TOKEN")
                    .jsonPath("$.parameterName").isEqualTo("_csrf")
                    .jsonPath("$.token").isNotEmpty();
        }

        @Test
        @DisplayName("CSRF令牌不存在时返回空")
        @WithMockUser(username = "testuser")
        void shouldReturnEmptyWhenCsrfTokenNotPresent() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Nested
    @DisplayName("OAuth2客户端绑定测试")
    class BindOauth2Tests {

        @Test
        @DisplayName("成功绑定OAuth2客户端并返回访问令牌")
        @WithMockUser(username = "admin")
        void shouldReturnAccessTokenWhenClientBindingSuccessful() {
            // 准备测试数据
            String clientRegistrationId = "github";
            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "test-access-token",
                    Instant.now(),
                    Instant.now().plusSeconds(3600)
            );

            ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(clientRegistrationId)
                    .clientId("test-client-id")
                    .clientSecret("test-client-secret")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:8080/login/oauth2/code/github")
                    .authorizationUri("https://github.com/login/oauth/authorize")
                    .tokenUri("https://github.com/login/oauth/access_token")
                    .build();

            OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                    clientRegistration,
                    "admin",
                    accessToken
            );

            // 模拟客户端仓库行为
            when(clientRepository.loadAuthorizedClient(eq(clientRegistrationId), any(), any()))
                    .thenReturn(Mono.just(authorizedClient));

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sec/v1/oauth2/bind")
                            .queryParam("clientRegistrationId", clientRegistrationId)
                            .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.tokenValue").isEqualTo("test-access-token")
                    .jsonPath("$.tokenType.value").isEqualTo("Bearer");

            // 验证方法调用
            verify(clientRepository).loadAuthorizedClient(eq(clientRegistrationId), any(), any());
        }

        @Test
        @DisplayName("客户端不存在时返回空结果")
        @WithMockUser(username = "admin")
        void shouldReturnEmptyWhenClientNotFound() {
            String clientRegistrationId = "nonexistent";

            when(clientRepository.loadAuthorizedClient(eq(clientRegistrationId), any(), any()))
                    .thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sec/v1/oauth2/bind")
                            .queryParam("clientRegistrationId", clientRegistrationId)
                            .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .isEmpty();
        }

        @Test
        @DisplayName("未认证用户绑定客户端应返回302重定向")
        void shouldReturn302ForUnauthenticatedUserBinding() {
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sec/v1/oauth2/bind")
                            .queryParam("clientRegistrationId", "github")
                            .build())
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }

        @Test
        @DisplayName("客户端仓库异常时应正确处理")
        @WithMockUser(username = "admin")
        void shouldHandleClientRepositoryError() {
            String clientRegistrationId = "github";

            when(clientRepository.loadAuthorizedClient(eq(clientRegistrationId), any(), any()))
                    .thenReturn(Mono.error(new RuntimeException("客户端仓库错误")));

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sec/v1/oauth2/bind")
                            .queryParam("clientRegistrationId", clientRegistrationId)
                            .build())
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("密码修改测试")
    class ChangePasswordTests {

        @Test
        @DisplayName("成功修改密码")
        @WithMockUser(username = "admin", password = "123456")
        void shouldChangePasswordSuccessfully() throws Exception {
            // 准备测试数据
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("123456");
            request.setNewPassword("newPassword");

            UserDetails updatedUser = User.builder()
                    .username("admin")
                    .password("newEncodedPassword")
                    .authorities(testAuthorities)
                    .build();

            // 模拟密码编码器和安全管理器行为
            when(passwordEncoder.matches("123456", "123456")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            when(securityManager.updatePassword(any(UserDetails.class), eq("newEncodedPassword")))
                    .thenReturn(Mono.just(updatedUser));

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo("admin")
                    .jsonPath("$.password").isEqualTo("newEncodedPassword")
                    .jsonPath("$.authorities").isArray();

            // 验证方法调用
            verify(passwordEncoder).matches("123456", "123456");
            verify(passwordEncoder).encode("newPassword");
            verify(securityManager).updatePassword(any(UserDetails.class), eq("newEncodedPassword"));
        }

        @Test
        @DisplayName("密码和新密码相同时应抛出异常")
        @WithMockUser(username = "admin", password = "123456")
        void shouldThrowExceptionWhenPasswordsAreSame() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("samePassword");
            request.setNewPassword("samePassword");

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().is5xxServerError();

            // 验证没有调用密码相关方法
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(securityManager, never()).updatePassword(any(), anyString());
        }

        @Test
        @DisplayName("当前密码验证失败时应抛出异常")
        @WithMockUser(username = "admin", password = "123456")
        void shouldThrowExceptionWhenCurrentPasswordVerificationFails() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("wrongPassword");
            request.setNewPassword("newPassword");

            // 模拟密码验证失败
            when(passwordEncoder.matches("wrongPassword", "123456")).thenReturn(false);

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().is5xxServerError();

            // 验证只调用了密码匹配，没有进行后续操作
            verify(passwordEncoder).matches("wrongPassword", "123456");
            verify(passwordEncoder, never()).encode(anyString());
            verify(securityManager, never()).updatePassword(any(), anyString());
        }

        @Test
        @DisplayName("请求参数验证失败 - 密码为空")
        @WithMockUser(username = "admin", password = "123456")
        void shouldFailValidationWhenPasswordIsEmpty() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword(""); // 空密码
            request.setNewPassword("newPassword");

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("请求参数验证失败 - 新密码为空")
        @WithMockUser(username = "admin", password = "123456")
        void shouldFailValidationWhenNewPasswordIsEmpty() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("123456");
            request.setNewPassword(""); // 空新密码

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("请求参数验证失败 - 密码为null")
        @WithMockUser(username = "admin", password = "1263456")
        void shouldFailValidationWhenPasswordIsNull() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword(null); // null密码
            request.setNewPassword("newPassword");

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("未认证用户修改密码应返回302重定向")
        void shouldReturn302ForUnauthenticatedUserPasswordChange() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("oldPassword");
            request.setNewPassword("newPassword");

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }

        @Test
        @DisplayName("安全管理器更新密码失败时应正确处理")
        @WithMockUser(username = "admin", password = "123456")
        void shouldHandleSecurityManagerUpdateError() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("123456");
            request.setNewPassword("newPassword");

            // 模拟密码验证成功但更新失败
            when(passwordEncoder.matches("123456", "123456")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            when(securityManager.updatePassword(any(UserDetails.class), eq("newEncodedPassword")))
                    .thenReturn(Mono.error(new RuntimeException("数据库更新失败")));

            webTestClient
                    .mutateWith(SecurityMockServerConfigurers.csrf())
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().is5xxServerError();

            // 验证所有方法都被调用了
            verify(passwordEncoder).matches("123456", "123456");
            verify(passwordEncoder).encode("newPassword");
            verify(securityManager).updatePassword(any(UserDetails.class), eq("newEncodedPassword"));
        }

        @Test
        @DisplayName("缺少CSRF令牌时应返回403")
        @WithMockUser(username = "admin", password = "123456")
        void shouldReturn403WhenCsrfTokenMissing() throws Exception {
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setPassword("123456");
            request.setNewPassword("newPassword");

            webTestClient
                    .post()
                    .uri("/sec/v1/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(objectMapper.writeValueAsString(request))
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("权限验证测试")
    class AuthorizationTests {

        @Test
        @DisplayName("具有ADMIN权限的用户可以访问所有端点")
        @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
        void shouldAllowAdminUserAccessToAllEndpoints() {
            // 测试登录令牌端点
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().isOk();

            // 测试CSRF令牌端点
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isOk();

            // 测试OAuth2绑定端点
            when(clientRepository.loadAuthorizedClient(anyString(), any(), any()))
                    .thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/sec/v1/oauth2/bind")
                            .queryParam("clientRegistrationId", "github")
                            .build())
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("具有USER权限的用户可以访问基本端点")
        @WithMockUser(username = "user", authorities = {"ROLE_USER"})
        void shouldAllowUserAccessToBasicEndpoints() {
            // 测试登录令牌端点
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().isOk();

            // 测试CSRF令牌端点
            webTestClient.get()
                    .uri("/sec/v1/oauth2/csrf")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("无权限用户应被拒绝访问")
        void shouldDenyAccessToUnauthorizedUser() {
            webTestClient.get()
                    .uri("/sec/v1/oauth2/login")
                    .exchange()
                    .expectStatus().is3xxRedirection();
        }
    }
}