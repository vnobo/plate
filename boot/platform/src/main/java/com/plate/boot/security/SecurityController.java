package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.jwt.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.naming.NameNotFoundException;
import java.time.Instant;

/**
 * The SecurityController class is a REST controller responsible for handling security-related endpoints.
 * It manages OAuth2 operations, password changes, and CSRF token retrieval.
 * The class utilizes a WebSession-based security context repository, a security manager, password encoding, and an OAuth2 client repository.
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
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
     * Issues and revokes the HttpOnly remember-me cookie, backed by a 2-day token in Redis.
     */
    private final RememberMeService rememberMeService;
    /**
     * Used by {@link #refresh} to write the rebuilt authentication into the WebSession.
     */
    private final ServerSecurityContextRepository securityContextRepository;
    /**
     * Issues and rotates the JWT access token / refresh token pair.
     */
    private final TokenService tokenService;

    /**
     * Issues the access token / refresh token pair for the authenticated user.
     * <p>
     * Authentication itself is still performed by the filter chain (HTTP Basic or the OAuth2 login),
     * so the existing user and authority loading is reused unchanged. When {@code rememberMe} is
     * requested a remember-me cookie is also issued so the session can be restored later without the
     * password.
     *
     * @param authentication The current user's authentication information, containing principal and authority information
     * @param exchange       The current exchange, used to write the remember-me cookie
     * @param rememberMe     Whether the browser should be remembered for 2 days
     * @return An AuthenticationToken holding the JWT access token and its one-time refresh token
     */
    @GetMapping("login")
    public Mono<AuthenticationToken> loginToken(Authentication authentication, ServerWebExchange exchange,
                                                @RequestParam(required = false, defaultValue = "false")
                                                boolean rememberMe) {
        if (!(authentication.getPrincipal() instanceof SecurityDetails details)) {
            return Mono.error(RestServerException.withMsg("Unsupported principal type",
                    new IllegalStateException("Principal is not a SecurityDetails")));
        }
        Mono<Void> remember = rememberMe
                ? this.rememberMeService.issue(exchange, details.getUsername())
                : Mono.empty();
        return this.tokenService.issue(details)
                .map(pair -> AuthenticationToken.of(pair.accessToken(), pair.refreshToken(), pair.expiresIn(),
                        Instant.now().getEpochSecond(), details))
                .flatMap(remember::thenReturn);
    }

    /**
     * Rotates a refresh token: the presented token is consumed and a brand new pair is issued.
     * <p>
     * Replaying an already consumed token is treated as theft and revokes every session of the user.
     *
     * @param request the one-time refresh token to consume
     * @return the new token pair, or 401 when the token is unknown, expired, revoked or replayed
     */
    @PostMapping("refresh")
    public Mono<ResponseEntity<AuthenticationToken>> rotate(@Valid @RequestBody RefreshTokenRequest request) {
        return this.tokenService.consume(request.getRefreshToken())
                .flatMap(rotation -> this.securityManager.details(rotation.username())
                        .flatMap(details -> this.tokenService.rotate(details, rotation)
                                .map(pair -> ResponseEntity.ok(AuthenticationToken.of(pair.accessToken(),
                                        pair.refreshToken(), pair.expiresIn(),
                                        Instant.now().getEpochSecond(), details)))))
                .onErrorResume(InvalidBearerTokenException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .build()));
    }

    /**
     * Restores a session from the remember-me cookie, so a returning user lands inside the app without retyping
     * the password. No cookie, expired or revoked → 401 and the frontend stays on the login page.
     *
     * @param session  The current WebSession, holding the id returned in the token
     * @param exchange The current exchange, used to read the cookie and save the security context
     * @return 200 with a fresh {@link AuthenticationToken}, or 401 when there is nothing to restore
     */
    @GetMapping("refresh")
    public Mono<ResponseEntity<AuthenticationToken>> refresh(WebSession session, ServerWebExchange exchange) {
        return this.rememberMeService.username(exchange)
                .flatMap(this.securityManager::findByUsername)
                .map(user -> new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()))
                .flatMap(authentication -> this.securityContextRepository
                        .save(exchange, new SecurityContextImpl(authentication))
                        .thenReturn(authentication))
                .map(authentication -> ResponseEntity.ok(
                        AuthenticationToken.build(session, authentication.getPrincipal())))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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
    public Mono<Object> bindOauth2(@NotBlank(message = "ClientRegistrationId cannot be empty") String clientRegistrationId,
                                   Authentication authentication, ServerWebExchange exchange) {
        return this.clientRepository.loadAuthorizedClient(clientRegistrationId, authentication, exchange)
                .switchIfEmpty(Mono.defer(() -> Mono.error(RestServerException.withMsg("Client ["
                                + clientRegistrationId + "] not found",
                        new NameNotFoundException("Client [" + clientRegistrationId + "] not found")))))
                .flatMap(oAuth2AuthorizedClient -> Mono.just(oAuth2AuthorizedClient.getAccessToken()));
    }

    /**
     * Changes the password for the authenticated user.
     *
     * @param request        A {@link ChangePasswordRequest} containing the current password and the new password.
     * @param authentication The authentication object representing the currently authenticated user.
     * @return A {@link Mono} emitting the updated {@link UserDetails} after the password change.
     * @throws RestServerException if the new password equals the current password,
     *                             or if the presented current password does not match the stored credential.
     */
    @PostMapping("/change/password")
    public Mono<UserDetails> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        if (request.getPassword().equals(request.getNewPassword())) {
            throw RestServerException.withMsg("New password cannot be the same as current password",
                    new IllegalArgumentException("New password cannot be the same as current password"));
        }
        String newPassword = this.passwordEncoder.encode(request.getNewPassword());
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw RestServerException.withMsg("User details not found",
                    new IllegalStateException("Principal is not an instance of UserDetails"));
        }
        // JWT 认证没有 credentials，改为从库里取当前密码校验，否则无状态令牌下改密码会失效
        Mono<String> currentEncodedPassword = authentication.getCredentials() instanceof String encoded
                ? Mono.just(encoded)
                : this.securityManager.loadByUsername(authentication.getName()).map(User::getPassword);
        return currentEncodedPassword
                .filter(encoded -> this.passwordEncoder.matches(request.getPassword(), encoded))
                .switchIfEmpty(Mono.error(RestServerException.withMsg(
                        "Current password verification failed",
                        new IllegalArgumentException("Current password verification failed"))))
                .flatMap(encoded -> this.securityManager.updatePassword(userDetails, newPassword));
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

    /**
     * The one-time refresh token presented to {@link #rotate(RefreshTokenRequest)}.
     */
    @Data
    public static class RefreshTokenRequest {

        @NotBlank(message = "Refresh token not empty!")
        private String refreshToken;
    }

}