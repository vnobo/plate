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
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The SecurityController class is a REST controller responsible for handling security-related endpoints.
 * It manages OAuth2 operations, password changes, and CSRF token retrieval.
 * The class utilizes a WebSession-based security context repository, a security manager, password encoding, and an OAuth2 client repository.
 */
@RestController
@RequestMapping("/oauth2")
public class SecurityController {

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

    /**
     * Retrieves an authentication token for the logged-in user by utilizing the provided server web exchange and authentication objects.
     * The method leverages the security context repository to save the security context and then extracts session information
     * combined with the authentication details to construct an {@link AuthenticationToken}.
     *
     * @param exchange       The {@link ServerWebExchange} representing the current server request and response.
     * @param authentication The {@link Authentication} object representing the authenticated user.
     * @return A {@link Mono} emitting the constructed {@link AuthenticationToken} containing session and authentication details.
     */
    @GetMapping("login")
    public Mono<AuthenticationToken> loginToken(ServerWebExchange exchange, Authentication authentication) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> exchange.getSession())
                .flatMap(session -> Mono.just(AuthenticationToken.build(session, authentication)));
    }

    /**
     * Retrieves the CSRF token from the context.
     * <p>
     * This endpoint is intended for obtaining a CSRF token which can be used to protect
     * against cross-site request forgery attacks. It utilizes the context to fetch the
     * CSRF token that has been stored there by the security framework.
     *
     * @return A Mono emitting the CsrfToken instance if present in the context, otherwise an empty Mono.
     */
    @GetMapping("csrf")
    public Mono<CsrfToken> csrfToken() {
        return Mono.deferContextual((contextView) -> {
            CsrfToken ctk = contextView.get(ContextUtils.CSRF_TOKEN_CONTEXT);
            return Mono.justOrEmpty(ctk);
        });
    }

    /**
     * Binds an OAuth2 authorized client to the current authentication context.
     * <p>
     * This method is responsible for loading the authorized client associated with the provided
     * {@code clientRegistrationId} using the current {@link Authentication} and {@link ServerWebExchange}.
     * It then extracts the access token from the authorized client and returns it as a Mono.
     *
     * @param clientRegistrationId The identifier for the client registration. This is used to look up the
     *                             specific OAuth2 configuration details that the client is registered against.
     * @param authentication       The current authentication context containing user details and credentials.
     * @param exchange             The current server web exchange which holds information about the HTTP request and response.
     * @return A Mono emitting the access token associated with the bound OAuth2 authorized client.
     */
    @GetMapping("bind")
    public Mono<Object> bindOauth2(@NotBlank(message = "ClientRegistrationId cannot be empty") String clientRegistrationId, Authentication authentication, ServerWebExchange exchange) {
        return this.clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange)
                .switchIfEmpty(Mono.defer(() -> Mono.error(RestServerException.withMsg("Client ["
                                + clientRegistrationId + "] not found",
                        new RuntimeException("Client [" + clientRegistrationId + "] not found")))))
                .flatMap(oAuth2AuthorizedClient -> Mono.just(oAuth2AuthorizedClient.getAccessToken()));
    }

    /**
     * Changes the password for the authenticated user.
     *
     * @param request        A {@link ChangePasswordRequest} containing the current password and the new password.
     * @param authentication The authentication object representing the currently authenticated user.
     * @return A {@link Mono} emitting the updated {@link UserDetails} after the password change.
     * @throws RestServerException if the provided password does not match the new password,
     *                             or if the presented password does not match the current stored password.
     */
    @PostMapping("/change/password")
    public Mono<UserDetails> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        if (request.getPassword().equals(request.getNewPassword())) {
            throw RestServerException.withMsg("New password cannot be the same as current password",
                    new IllegalArgumentException("New password cannot be the same as current password"));
        }
        String presentedPassword = request.getPassword();
        String currentEncodedPassword = (String) authentication.getCredentials();
        if (!this.passwordEncoder.matches(presentedPassword, currentEncodedPassword)) {
            throw RestServerException.withMsg(
                    "Current password verification failed",
                    new IllegalArgumentException("Current password verification failed"));
        }
        String newPassword = this.passwordEncoder.encode(request.getNewPassword());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return this.securityManager.updatePassword(userDetails, newPassword);
    }

    /**
     * Represents a request to change a user's password.
     * This class encapsulates the necessary information to perform a password change,
     * including the current password and the new password.
     * <p>
     * The class uses validation annotations to ensure that both the current and new passwords
     * are not empty. This helps prevent invalid password change requests from being processed.
     * </p>
     * <p>
     * Example usage:
     * <pre>
     * ChangePasswordRequest request = new ChangePasswordRequest();
     * request.setPassword("oldPassword");
     * request.setNewPassword("newPassword");
     * </pre>
     * </p>
     */
    @Data
    public static class ChangePasswordRequest {

        /**
         * The user's current password
         * Used to verify the user's identity to ensure that the password change request is made by the legitimate user
         */
        @NotBlank(message = "Password not empty!")
        private String password;

        /**
         * The new password to be set by the user
         * Must be different from the current password and meet the password strength requirements
         */
        @NotBlank(message = "New password not empty!")
        private String newPassword;
    }

}