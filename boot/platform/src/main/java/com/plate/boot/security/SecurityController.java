package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.AuthenticationToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class SecurityController {

    private final WebSessionServerSecurityContextRepository securityContextRepository =
            new WebSessionServerSecurityContextRepository();

    private final UserSecurityManager userSecurityManager;
    private final PasswordEncoder passwordEncoder;
    private final ServerOAuth2AuthorizedClientRepository clientRepository;

    @GetMapping("token")
    public Mono<AuthenticationToken> token(ServerWebExchange exchange, Authentication authentication) {
        return ReactiveSecurityContextHolder.getContext()
                .delayUntil(cts -> this.securityContextRepository.save(exchange, cts))
                .flatMap(context -> exchange.getSession())
                .flatMap(session -> Mono.just(AuthenticationToken.build(session, authentication)));
    }

    @GetMapping("csrf")
    public Mono<CsrfToken> csrfToken() {
        return Mono.deferContextual((contextView) -> {
            CsrfToken ctk = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
            return Mono.justOrEmpty(ctk);
        });
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
        return this.userSecurityManager.updatePassword(userDetails, newPassword);
    }

    @Data
    public static class ChangePasswordRequest {

        @NotBlank(message = "Password not empty!")
        private String password;

        @NotBlank(message = "New password not empty!")
        private String newPassword;
    }

}