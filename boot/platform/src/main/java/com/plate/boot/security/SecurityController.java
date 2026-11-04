package com.plate.boot.security;

import com.plate.boot.security.core.user.User;
import com.plate.boot.security.jwt.ClientType;
import com.plate.boot.security.jwt.JwtService;
import com.plate.boot.security.jwt.TokenPair;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The single authentication entry point. Every flow ends in the same stateless JWT
 * {@link TokenPair}; JWT is the only credential the rest of the API accepts.
 *
 * <ul>
 *   <li>{@code POST /oauth2/login} — local username/password login.</li>
 *   <li>{@code POST /oauth2/refresh} — rotate a refresh JWT.</li>
 *   <li>{@code POST /oauth2/login/{registrationId}} — login with an already authorized
 *       third-party identity; the account must be bound first.</li>
 *   <li>{@code POST /oauth2/bind} — verify local credentials and associate a third-party
 *       identity with that account.</li>
 * </ul>
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class SecurityController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityManager securityManager;

    /**
     * Authenticates local username/password and issues an access/refresh JWT pair.
     */
    @PostMapping("login")
    public Mono<TokenPair> login(@Valid @RequestBody LoginRequest request) {
        Authentication credentials = UsernamePasswordAuthenticationToken
                .unauthenticated(request.username(), request.password());
        return this.authenticationManager.authenticate(credentials)
                .map(Authentication::getPrincipal)
                .ofType(SecurityDetails.class)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Unsupported principal type")))
                .flatMap(details -> this.jwtService.issueTokenPair(details, request.resolveClientType()));
    }

    /**
     * Issues a fresh pair from a valid refresh JWT.
     */
    @PostMapping("refresh")
    public Mono<TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return this.jwtService.refresh(request.refreshToken());
    }

    /**
     * Completes an OAuth2 login: the third-party authorization itself happens at the provider
     * (browser/app), and the resulting {@code openid} is exchanged here for a local JWT pair.
     * An unbound identity is rejected so it must go through {@link #bind(BindRequest)} first.
     *
     * @param registrationId provider / registration id, e.g. {@code github}
     */
    @PostMapping("login/{registrationId}")
    public Mono<TokenPair> oauth2Login(@PathVariable String registrationId,
                                       @Valid @RequestBody Oauth2LoginRequest request) {
        return this.securityManager.loadByOauth2(registrationId, request.openid())
                .switchIfEmpty(Mono.error(new BadCredentialsException(
                        "Third-party account is not bound to a local account")))
                .flatMap((User user) -> this.securityManager.loadUserDetails(user.getUsername()))
                .ofType(SecurityDetails.class)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Bound local user no longer exists")))
                .flatMap(details -> this.jwtService.issueTokenPair(details, request.resolveClientType()));
    }

    /**
     * Associates a third-party identity with a local account after verifying that account's
     * password. Returns a freshly issued JWT pair so a successful bind also signs the user in.
     */
    @PostMapping("bind")
    public Mono<TokenPair> bind(@Valid @RequestBody BindRequest request) {
        Authentication credentials = UsernamePasswordAuthenticationToken
                .unauthenticated(request.username(), request.password());
        return this.authenticationManager.authenticate(credentials)
                .map(Authentication::getPrincipal)
                .ofType(SecurityDetails.class)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Unsupported principal type")))
                .flatMap(details -> this.securityManager
                        .bindOauth2(details.getUsername(), request.registrationId(), request.openid())
                        .flatMap(updated -> updated == 0
                                ? Mono.error(new BadCredentialsException("Local account not found"))
                                : this.jwtService.issueTokenPair(details, request.resolveClientType())));
    }

    /**
     * Local login request; {@code clientType} defaults to {@link ClientType#PC}.
     */
    public record LoginRequest(@NotBlank(message = "Username not empty!") String username,
                               @NotBlank(message = "Password not empty!") String password,
                               ClientType clientType) {

        public ClientType resolveClientType() {
            return Objects.requireNonNullElse(clientType, ClientType.PC);
        }
    }

    /**
     * Refresh request holding a previously issued refresh JWT.
     */
    public record RefreshRequest(@NotBlank(message = "Refresh token not empty!") String refreshToken) {
    }

    /**
     * Third-party login request. {@code openid} is the provider's stable user identifier
     * obtained from the completed OAuth2 authorization.
     */
    public record Oauth2LoginRequest(@NotBlank(message = "Openid not empty!") String openid,
                                     ClientType clientType) {

        public ClientType resolveClientType() {
            return Objects.requireNonNullElse(clientType, ClientType.PC);
        }
    }

    /**
     * Binding request: local credentials plus the third-party identity to associate.
     */
    public record BindRequest(@NotBlank(message = "Username not empty!") String username,
                              @NotBlank(message = "Password not empty!") String password,
                              @NotBlank(message = "RegistrationId not empty!") String registrationId,
                              @NotBlank(message = "Openid not empty!") String openid,
                              ClientType clientType) {

        public ClientType resolveClientType() {
            return Objects.requireNonNullElse(clientType, ClientType.PC);
        }
    }
}
