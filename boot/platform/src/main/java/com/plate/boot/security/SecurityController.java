package com.plate.boot.security;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.jwt.ClientType;
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
import java.util.Objects;

/**
 * Single entry point for the platform's authentication endpoints.
 *
 * <p>{@code /auth/*} serves stateless, separated frontends: credentials are exchanged for a
 * signed access/refresh JWT pair, which the SPA then sends as {@code Authorization: Bearer}.
 *
 * <p>{@code /oauth2/*} keeps the browser/session flow (HTTP Basic, remember-me cookie, Redis
 * backed refresh-token rotation) working unchanged for the existing UI.
 */
@RestController
@RequiredArgsConstructor
public class SecurityController {

    private final SecurityManager securityManager;
    private final PasswordEncoder passwordEncoder;
    private final ServerOAuth2AuthorizedClientRepository clientRepository;
    private final RememberMeService rememberMeService;
    private final ServerSecurityContextRepository securityContextRepository;
    private final TokenService tokenService;

    /**
     * Issues the access token / refresh token pair for the already authenticated browser user.
     * Authentication itself is performed by the filter chain (HTTP Basic or the OAuth2 login).
     *
     * @param authentication The current user's authentication information
     * @param exchange       The current exchange, used to write the remember-me cookie
     * @param rememberMe     Whether the browser should be remembered for 2 days
     * @return An AuthenticationToken holding the JWT access token and its one-time refresh token
     */
    @GetMapping("/oauth2/login")
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
     * Rotates a one-time refresh token into a brand new pair. Replay revokes every session of the user.
     *
     * @param request the one-time refresh token to consume
     * @return the new token pair, or 401 when the token is unknown, expired, revoked or replayed
     */
    @PostMapping("/oauth2/refresh")
    public Mono<ResponseEntity<AuthenticationToken>> rotate(@Valid @RequestBody RefreshTokenRequest request) {
        return this.tokenService.consume(request.getRefreshToken())
                .flatMap(rotation -> this.securityManager.loadUserDetails(rotation.username())
                        .ofType(SecurityDetails.class)
                        .flatMap(details -> this.tokenService.rotate(details, rotation)
                                .map(pair -> ResponseEntity.ok(AuthenticationToken.of(pair.accessToken(),
                                        pair.refreshToken(), pair.expiresIn(),
                                        Instant.now().getEpochSecond(), details)))))
                .onErrorResume(InvalidBearerTokenException.class,
                        _ -> Mono.just(
                                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()));
    }

    /**
     * Restores a session from the remember-me cookie. No cookie, expired or revoked → 401.
     *
     * @param session  The current WebSession, holding the id returned in the token
     * @param exchange The current exchange, used to read the cookie and save the security context
     * @return 200 with a fresh {@link AuthenticationToken}, or 401 when there is nothing to restore
     */
    @GetMapping("/oauth2/refresh")
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
     *
     * @param clientRegistrationId The identifier for the client registration
     * @param authentication       The current authentication context
     * @param exchange             The current server web exchange
     * @return The access token associated with the bound OAuth2 authorized client
     */
    @GetMapping("/oauth2/bind")
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
     * @param request        A {@link ChangePasswordRequest} containing the current and new password
     * @param authentication The authentication object representing the currently authenticated user
     * @return A {@link Mono} emitting the updated {@link UserDetails}
     */
    @PostMapping("/oauth2/change/password")
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
        Mono<String> currentEncodedPassword = authentication.getCredentials() instanceof String encoded
                ? Mono.just(encoded)
                : this.securityManager.loadByUsername(authentication.getName()).map(User::getPassword);
        return currentEncodedPassword
                .filter(encoded -> this.passwordEncoder.matches(request.getPassword(), encoded))
                .switchIfEmpty(Mono.error(RestServerException.withMsg(
                        "Current password verification failed",
                        new IllegalArgumentException("Current password verification failed"))))
                .flatMap(_ -> this.securityManager.updatePassword(userDetails, newPassword));
    }

    /**
     * Username/password login request for PC and native-app clients.
     */
    public record LoginRequest(@NotBlank(message = "Username not empty!") String username,
                               @NotBlank(message = "Password not empty!") String password,
                               ClientType clientType) {

        public ClientType resolveClientType() {
            return Objects.requireNonNullElse(clientType, ClientType.PC);
        }
    }

    /**
     * Refresh request holding a previously issued refresh token.
     */
    public record RefreshRequest(@NotBlank(message = "Refresh token not empty!") String refreshToken) {
    }

    /**
     * Represents a request to change a user's password.
     */
    @Data
    public static class ChangePasswordRequest {

        @NotBlank(message = "Password not empty!")
        private String password;

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
