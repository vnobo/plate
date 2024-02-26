package com.platform.boot.security;

import com.platform.boot.commons.exception.RestServerException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    /**
     * This endpoint is used to generate an authentication token.
     *
     * @param session        The current web session.
     * @param authentication The authentication object containing the user's credentials.
     * @return A Mono<AuthenticationToken> object containing the authentication token.
     * &#064;GetMapping  annotation is used to handle GET type requests. This endpoint is mapped to "/oauth2/token".
     * The method uses the WebSession and Authentication parameters to build an AuthenticationToken.
     * The building of the AuthenticationToken is deferred until subscription time to ensure that it is built with the most up-to-date session and authentication information.
     * <p>
     * The built AuthenticationToken is then wrapped in a Mono and returned.
     */
    @GetMapping("token")
    public Mono<AuthenticationToken> token(WebSession session, Authentication authentication) {
        return Mono.defer(() -> Mono.just(AuthenticationToken.build(session, authentication)));
    }

    /**
     * Retrieves the CSRF token from the current context.
     *
     * @return A Mono<CsrfToken> object containing the CSRF token, or an empty Mono if no CSRF token is found.
     */
    @GetMapping("csrf")
    public Mono<CsrfToken> csrfToken() {
        // Defer the retrieval of the CSRF token to subscription time.
        // This allows the CSRF token to be retrieved from the current context when the Mono is subscribed to.
        return Mono.deferContextual((contextView) -> {
            // Retrieve the CSRF token from the current context.
            CsrfToken ctk = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
            // Return the CSRF token wrapped in a Mono, or an empty Mono if no CSRF token is found.
            return Mono.justOrEmpty(ctk);
        });
    }

    /**
     * Binds an OAuth2 client to the authenticated user.
     *
     * @param clientRegistrationId The registration ID of the OAuth2 client.
     * @param authentication       The authentication object containing the user's credentials.
     * @param exchange             The current server web exchange.
     * @return A Mono<Object> object containing the access token of the OAuth2 client.
     */
    @GetMapping("bind")
    public Mono<Object> bindOauth2(String clientRegistrationId, Authentication authentication, ServerWebExchange exchange) {
        // Load the authorized OAuth2 client using the client registration ID, authentication object, and server web exchange.
        // Then, retrieve the access token of the OAuth2 client.
        return this.clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange)
                .flatMap(oAuth2AuthorizedClient -> Mono.just(oAuth2AuthorizedClient.getAccessToken()));
    }

    /**
     * Changes the password of the authenticated user.
     *
     * @param request        The request object containing the current and new password.
     * @param authentication The authentication object containing the user's credentials.
     * @return A Mono<UserDetails> object of the updated user.
     * @throws RestServerException if the new password is the same as the current password.
     * @throws RestServerException if the presented password does not match the current password.
     */
    public Mono<UserDetails> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        // Check if the new password is the same as the current password.
        if (!request.getPassword().equals(request.getNewPassword())) {
            // Throw an exception if the new password is the same as the current password.
            throw RestServerException.withMsg("Password and newPassword not match", request);
        }
        // Retrieve the presented password from the authentication object.
        String presentedPassword = (String) authentication.getCredentials();
        // Check if the presented password matches the current password.
        if (!this.passwordEncoder.matches(presentedPassword, request.getPassword())) {
            // Throw an exception if the presented password does not match the current password.
            throw RestServerException.withMsg(
                    "Password verification failed, presented password not match", presentedPassword);
        }
        // Encode the new password.
        String newPassword = this.passwordEncoder.encode(request.getNewPassword());
        // Retrieve the UserDetails from the authentication object.
        UserDetails userDetails = (UserDetails) authentication.getDetails();
        // Update the user's password and return the updated UserDetails.
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