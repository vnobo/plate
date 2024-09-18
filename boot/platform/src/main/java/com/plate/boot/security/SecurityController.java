package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.AuthenticationToken;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
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
 * Handles security-related endpoints for OAuth2 operations, password changes, and CSRF token retrieval.
 * Utilizes WebSession-based security context repository, security manager, password encoding, and OAuth2 client repository.
 */
@RestController
@RequestMapping("/oauth2")
public class SecurityController {

    /**
     * Repository responsible for managing the security context within the server's web sessions.
     * It stores and retrieves the security context associated with each user's session, ensuring
     * that security-related information persists across requests within the same session.
     */
    private final WebSessionServerSecurityContextRepository securityContextRepository =
            new WebSessionServerSecurityContextRepository();

    /**
     * The {@code securityManager} field is a final instance of {@link SecurityManager}, responsible for handling
     * security-related operations such as user authentication, password management, and authority provisioning within
     * the application. It serves as the central authority for managing user details, roles, and permissions, ensuring
     * secure access control based on defined security policies.
     * <p>
     * This component is injected via constructor dependency injection, providing reactive services for fetching user
     * details, updating passwords, registering or modifying users, loading users by OAuth2 bindings, and more, thereby
     * reinforcing the security infrastructure of the {@link SecurityController}.
     */
    private final SecurityManager securityManager;
    /**
     * Encoder used for encoding and validating passwords securely.
     * This field is responsible for hashing passwords upon user registration or password updates,
     * and verifying passwords during authentication processes to ensure they match the stored hash.
     */
    private final PasswordEncoder passwordEncoder;
    /**
     * Repository responsible for storing and retrieving authorized client information for OAuth2 server-side authorization.
     * This instance specifically manages the authorized clients within the server context, ensuring secure access and
     * persistence of client details necessary for OAuth2 flows.
     */
    private final ServerOAuth2AuthorizedClientRepository clientRepository;

    /**
     * Constructs a new instance of SecurityController.
     *
     * @param securityManager  The SecurityManager instance responsible for security operations.
     * @param passwordEncoder  The PasswordEncoder used for encoding and verifying passwords.
     * @param clientRepository The ServerOAuth2AuthorizedClientRepository instance for managing OAuth2 authorized clients.
     */
    public SecurityController(SecurityManager securityManager, PasswordEncoder passwordEncoder,
                              ServerOAuth2AuthorizedClientRepository clientRepository) {
        this.securityManager = securityManager;
        this.passwordEncoder = passwordEncoder;
        this.clientRepository = clientRepository;
    }

    @GetMapping("login")
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