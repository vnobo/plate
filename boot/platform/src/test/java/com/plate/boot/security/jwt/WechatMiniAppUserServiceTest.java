package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link WechatMiniAppUserService}: first-login provisioning, returning
 * users and upstream failure propagation.
 */
@DisplayName("WechatMiniAppUserService")
@ExtendWith(MockitoExtension.class)
class WechatMiniAppUserServiceTest {

    private static final String OPENID = "o-wechat-open-id";
    private static final String UNIONID = "o-wechat-union-id";
    private static final String LOGIN_CODE = "wx-login-code-1";

    @Mock
    private SecurityManager securityManager;

    @Mock
    private WechatClient wechatClient;

    @InjectMocks
    private WechatMiniAppUserService service;

    @Test
    @DisplayName("should auto-register a user bound to the WeChat identity on first login")
    void shouldRegisterNewUserOnFirstLogin() {
        WechatSession session = new WechatSession(OPENID, "session-key", UNIONID);
        User user = user("wechat_miniapp#" + OPENID);
        SecurityDetails details = details("wechat_miniapp#" + OPENID);

        when(this.wechatClient.code2Session(LOGIN_CODE)).thenReturn(Mono.just(session));
        when(this.securityManager.loadByOauth2("wechat_miniapp", OPENID)).thenReturn(Mono.empty());
        when(this.securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(user));
        when(this.securityManager.findByUsername(user.getUsername())).thenReturn(Mono.just(details));

        StepVerifier.create(this.service.login(LOGIN_CODE))
                .assertNext(result -> assertThat(result).isSameAs(details))
                .verifyComplete();

        ArgumentCaptor<UserReq> captor = ArgumentCaptor.forClass(UserReq.class);
        verify(this.securityManager).registerOrModifyUser(captor.capture());
        UserReq request = captor.getValue();
        assertThat(request.getUsername()).isEqualTo("wechat_miniapp#" + OPENID);
        assertThat(request.getPassword()).isNotBlank();
        assertThat(request.getName()).isNotBlank();
        String extendJson = String.valueOf(request.getExtend());
        assertThat(extendJson).contains("wechat_miniapp").contains(OPENID).contains(UNIONID);
    }

    @Test
    @DisplayName("should load the existing bound user without registering again")
    void shouldLoadExistingUserWithoutRegistering() {
        WechatSession session = new WechatSession(OPENID, "session-key", null);
        User user = user("wechat_miniapp#" + OPENID);
        SecurityDetails details = details("wechat_miniapp#" + OPENID);

        when(this.wechatClient.code2Session(LOGIN_CODE)).thenReturn(Mono.just(session));
        when(this.securityManager.loadByOauth2("wechat_miniapp", OPENID)).thenReturn(Mono.just(user));
        when(this.securityManager.findByUsername(user.getUsername())).thenReturn(Mono.just(details));

        StepVerifier.create(this.service.login(LOGIN_CODE))
                .assertNext(result -> assertThat(result).isSameAs(details))
                .verifyComplete();

        verify(this.securityManager, never()).registerOrModifyUser(any(UserReq.class));
    }

    @Test
    @DisplayName("should propagate authentication failures from the WeChat exchange")
    void shouldPropagateWechatAuthenticationFailures() {
        when(this.wechatClient.code2Session(LOGIN_CODE))
                .thenReturn(Mono.error(new BadCredentialsException("WeChat login code invalid")));

        StepVerifier.create(this.service.login(LOGIN_CODE))
                .expectErrorMatches(error -> error instanceof BadCredentialsException)
                .verify();

        verify(this.securityManager, never()).loadByOauth2(eq("wechat_miniapp"), eq(OPENID));
    }

    private static User user(String username) {
        User user = new User();
        user.setId(1L);
        user.setCode(UUID.randomUUID());
        user.setUsername(username);
        return user;
    }

    private static SecurityDetails details(String username) {
        var granted = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityDetails details = new SecurityDetails(granted, Map.of("username", username), "username");
        details.setCode(UUID.randomUUID());
        details.setUsername(username);
        details.setPassword("$2a$10$not-a-real-hash");
        details.setDisabled(false);
        details.setAccountExpired(false);
        details.setAccountLocked(false);
        details.setCredentialsExpired(false);
        return details;
    }
}
