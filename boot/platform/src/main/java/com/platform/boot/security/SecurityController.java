package com.platform.boot.security;

import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.core.AuthenticationToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
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
    private final ServerOAuth2AuthorizedClientRepository clientRepository;
    @GetMapping("token")
    public Mono<AuthenticationToken> token(WebSession session) {
        return Mono.defer(() -> Mono.justOrEmpty(AuthenticationToken.build(session)));
    }

    @GetMapping("csrf")
    public Mono<CsrfToken> csrfToken() {
        return Mono.deferContextual((contextView) -> {
            CsrfToken ctk = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
            return Mono.justOrEmpty(ctk);
        });
    }

    @GetMapping("me")
    public Mono<SecurityDetails> me() {
        Mono<SecurityDetails> securityDetailsMono = ContextUtils.securityDetails();
        return securityDetailsMono
                .delayUntil(securityDetails -> this.securityManager.loginSuccess(securityDetails.getUsername()));
    }

    @GetMapping("bind")
    public Mono<Object> bindOauth2(String clientRegistrationId, Authentication authentication, ServerWebExchange exchange) {
        return this.clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange)
                .flatMap(oAuth2AuthorizedClient -> Mono.just(oAuth2AuthorizedClient.getAccessToken()));
    }

    @PostMapping("/change/password")
    public Mono<UserDetails> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        if (!request.getPassword().equals(request.getNewPassword())) {
            throw RestServerException.withMsg("Password and newPassword not match", request);
        }
        String presentedPassword = (String) authentication.getCredentials();
        if (!this.passwordEncoder.matches(presentedPassword, request.getPassword())) {
            throw RestServerException.withMsg(
                    "Password verification failed, presented password not match", presentedPassword);
        }
        String newPassword = this.passwordEncoder.encode(request.getNewPassword());
        UserDetails userDetails = (UserDetails) authentication.getDetails();
        return this.securityManager.updatePassword(userDetails, newPassword);
    }

    @Data
    static class ChangePasswordRequest {

        @NotBlank(message = "Password not empty!")
        private String password;

        @NotBlank(message = "New password not empty!")
        private String newPassword;
    }

}