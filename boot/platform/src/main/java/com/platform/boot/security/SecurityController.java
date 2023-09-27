package com.platform.boot.security;

import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.commons.utils.ContextUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityManager securityManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * * Retrieves the authentication token for the given web session.
     *
     * @param session the web session to retrieve the authentication token from
     * @return a {@link Mono} emitting the {@link AuthenticationToken} if it exists in the given web session,
     * {@link Mono#empty()} otherwise
     */
    @GetMapping("token")
    public Mono<AuthenticationToken> token(WebSession session) {
        return Mono.defer(() -> Mono.justOrEmpty(AuthenticationToken.build(session)));
    }

    /**
     * This endpoint is used to get the CSRF token from the current context.
     *
     * @return A {@link Mono} containing a {@link CsrfToken} object.
     */
    @GetMapping("csrf")
    public Mono<CsrfToken> csrfToken() {
        return Mono.deferContextual((contextView) -> {
            CsrfToken ctk = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
            return Mono.justOrEmpty(ctk);
        });
    }

    /**
     * Retrieves the security details of the current user.
     *
     * @return a {@link Mono} emitting the {@link SecurityDetails} of the current user, once the user has
     * been authenticated successfully by the {@link SecurityManager}
     */
    @GetMapping("me")
    public Mono<SecurityDetails> me() {
        // Retrieve the security details of the current user from the ContextHolder
        Mono<SecurityDetails> securityDetailsMono = ContextUtils.securityDetails();
        // Once the security details are retrieved, delay until the loginSuccess operation is performed by the SecurityManager
        return securityDetailsMono
                .delayUntil(securityDetails -> this.securityManager.loginSuccess(securityDetails.getUsername()));
    }

    /**
     * This endpoint is used to change the password of the current user.
     *
     * @param request        修改密码请求
     * @param authentication 当前用户身份验证信息
     * @return Mono<UserDetails> 用户详细信息
     */
    @PostMapping("/change/password")
    public Mono<UserDetails> changePassword(@RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        // 验证新密码和确认密码是否匹配
        if (!request.getPassword().equals(request.getNewPassword())) {
            throw RestServerException.withMsg("Password and newPassword not match", request);
        }
        // 获取当前用户提供的密码
        String presentedPassword = (String) authentication.getCredentials();
        // 验证提供的密码是否与当前密码匹配
        if (!this.passwordEncoder.matches(presentedPassword, request.getPassword())) {
            throw RestServerException.withMsg(
                    "Password verification failed, presented password not match", presentedPassword);
        }
        // 编码新密码
        String newPassword = this.passwordEncoder.encode(request.getNewPassword());
        // 更新密码并返回用户详细信息
        UserDetails userDetails = (UserDetails) authentication.getDetails();
        return this.securityManager.updatePassword(userDetails, newPassword);
    }

    @Data
    public static class ChangePasswordRequest {

        @NotBlank(message = "Password not empty!")
        private String password;

        @NotBlank(message = "New password not empty!")
        private String newPassword;
    }

}