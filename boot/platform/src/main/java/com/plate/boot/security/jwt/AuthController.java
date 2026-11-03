package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Stateless JWT authentication endpoints serving the platform's three client channels:
 * <ul>
 *   <li><b>PC</b> and native <b>App</b> clients sign in with username and password via {@code POST /auth/login}.</li>
 *   <li><b>WeChat mini-program</b> clients sign in with a {@code wx.login} code via {@code POST /auth/wechat/miniapp}.</li>
 *   <li>Any client exchanges an expired access token for a fresh pair via {@code POST /auth/refresh}.</li>
 * </ul>
 * <p>
 * All endpoints are reachable without a prior session and return a {@link TokenPair}; API
 * requests then authenticate with {@code Authorization: Bearer <accessToken>} as resolved by
 * {@link JwtSecurityContextRepository}.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final WechatMiniAppUserService wechatMiniAppUserService;
    private final SecurityManager securityManager;

    /**
     * Authenticates a PC or native-app user with username and password and issues a token pair.
     *
     * @param request the login credentials, optionally naming the client platform
     * @return a {@link Mono} emitting the issued {@link TokenPair}
     */
    @PostMapping("login")
    public Mono<TokenPair> login(@Valid @RequestBody LoginRequest request) {
        var token = UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password());
        return this.authenticationManager.authenticate(token)
                .map(Authentication::getPrincipal)
                .ofType(SecurityDetails.class)
                .flatMap(details -> this.jwtService.issueTokenPair(details, request.resolveClientType()));
    }

    /**
     * Authenticates a WeChat mini-program user via the {@code wx.login} code exchange and
     * issues a token pair. First-time users are registered automatically.
     *
     * @param request the mini-program login request holding the temporary login code
     * @return a {@link Mono} emitting the issued {@link TokenPair}
     */
    @PostMapping("wechat/miniapp")
    public Mono<TokenPair> wechatMiniAppLogin(@Valid @RequestBody WechatMiniAppLoginRequest request) {
        return this.wechatMiniAppUserService.login(request.code())
                .flatMap(details -> this.jwtService.issueTokenPair(details, ClientType.MINIAPP));
    }

    /**
     * Exchanges a valid refresh token for a fresh access/refresh pair, keeping the original
     * client platform and user identity.
     *
     * @param request the refresh request holding the refresh token
     * @return a {@link Mono} emitting the rotated {@link TokenPair}
     */
    @PostMapping("refresh")
    public Mono<TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return this.jwtService.parse(request.refreshToken())
                .filter(payload -> TokenType.REFRESH == payload.tokenType())
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid refresh token")))
                .flatMap(payload -> this.securityManager.findByUsername(payload.username())
                        .ofType(SecurityDetails.class)
                        .flatMap(details -> this.jwtService.issueTokenPair(details, payload.clientType())));
    }

    /**
     * Username/password login request for PC and native-app clients.
     *
     * @param username   the login name, must not be blank
     * @param password   the raw password, must not be blank
     * @param clientType the client platform, defaults to {@link ClientType#PC} when omitted
     */
    public record LoginRequest(@NotBlank(message = "Username not empty!") String username,
                               @NotBlank(message = "Password not empty!") String password,
                               ClientType clientType) {

        /**
         * Resolves the effective client type, defaulting to {@link ClientType#PC}.
         *
         * @return the requested client type, or {@link ClientType#PC} when unspecified
         */
        public ClientType resolveClientType() {
            return Objects.requireNonNullElse(clientType, ClientType.PC);
        }
    }

    /**
     * WeChat mini-program login request holding the temporary {@code wx.login} code.
     *
     * @param code the login code to exchange for the user's WeChat identity
     */
    public record WechatMiniAppLoginRequest(@NotBlank(message = "Code not empty!") String code) {
    }

    /**
     * Refresh request holding a previously issued refresh token.
     *
     * @param refreshToken the refresh token to rotate
     */
    public record RefreshRequest(@NotBlank(message = "Refresh token not empty!") String refreshToken) {
    }
}
