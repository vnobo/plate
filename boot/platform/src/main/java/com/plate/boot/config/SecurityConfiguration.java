package com.plate.boot.config;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.RememberMeService;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.jwt.JwtProperties;
import com.plate.boot.security.jwt.TokenService;
import com.plate.boot.security.oauth2.Oauth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.StaticResourceLocation;
import org.springframework.boot.security.autoconfigure.web.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.*;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

/**
 * SecurityConfiguration configures the application's security settings by defining beans and rules for authentication,
 * authorization, session management, CSRF protection, logout behavior, and more. It leverages Spring Security features
 * to secure both RESTful endpoints and RSocket interactions.
 */
@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final Oauth2SuccessHandler authenticationSuccessHandler;
    private final RememberMeService rememberMeService;

    /**
     * Shared with {@link com.plate.boot.security.SecurityController#refresh}, which writes a rebuilt
     * authentication into the WebSession; both must use the same repository or the session is not found.
     */
    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    /**
     * Configures and provides an instance of {@link ReactiveOAuth2AuthorizedClientService} which is primarily
     * responsible for managing OAuth2 authorized client details, storing and retrieving them from a reactive data
     * source via {@link DatabaseClient} and handling client registrations defined in
     * {@link ReactiveClientRegistrationRepository}.
     *
     * @param databaseClient   The reactive database client used for interacting with the data source to persist OAuth2
     *                         authorization data.
     * @param clientRepository The repository containing the definitions of all registered clients for OAuth2
     *                         authentication.
     * @return A configured instance of {@link R2dbcReactiveOAuth2AuthorizedClientService} capable of handling OAuth2
     * authorized client services within a reactive context.
     */
    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService oAuth2ClientService(DatabaseClient databaseClient,
                                                                     ReactiveClientRegistrationRepository clientRepository) {
        return new R2dbcReactiveOAuth2AuthorizedClientService(databaseClient, clientRepository);
    }

    /**
     * Provides a PasswordEncoder bean that utilizes a delegating strategy for encoding passwords. This delegating
     * encoder can handle different password encoding schemes based on encoded passwords' ids. It consults a set of
     * encoders and picks the one matching the id prepended to the encoded password.
     *
     * @return A {@link PasswordEncoder} instance which is capable of delegating to the appropriate password encoding
     * strategy.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return ContextUtils.createDelegatingPasswordEncoder("bcrypt");
    }

    /**
     * Signs access tokens with the shared HS256 secret, making them self-signed: no key server,
     * no external issuer.
     *
     * @param properties the JWT settings holding the signing secret
     * @return an encoder producing HS256 compact tokens
     */
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties properties) {
        return NimbusJwtEncoder.withSecretKey(properties.secretKey()).build();
    }

    /**
     * Verifies signature and expiry of incoming access tokens. Revocation is not checked here but in
     * the authentication converter, because it needs a reactive Redis round trip.
     *
     * @param properties the JWT settings holding the signing secret
     * @return a decoder restricted to HS256
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(JwtProperties properties) {
        return NimbusReactiveJwtDecoder.withSecretKey(properties.secretKey())
                .macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Turns a verified access token into an authenticated principal.
     * <p>
     * The token is rejected when its generation is older than the user's current one, which is how
     * logout and refresh-token replay revoke every session of that user. The principal is a
     * {@link SecurityDetails} so auditing ({@code UserAuditorAware}) and tenant scoping keep working
     * exactly as they did with session based authentication.
     *
     * @param securityManager loads the user with authorities, groups and tenants
     * @param tokenService    checks the revocation generation
     * @return the converter used by the resource server
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter(
            SecurityManager securityManager, TokenService tokenService) {
        return jwt -> {
            UUID userCode;
            long generation;
            try {
                userCode = UUID.fromString(Objects.requireNonNull(jwt.getClaimAsString("code")));
                generation = ((Number) Objects.requireNonNull(jwt.getClaim("gen"))).longValue();
            } catch (RuntimeException e) {
                return Mono.error(new InvalidBearerTokenException("Malformed token claims"));
            }
            return tokenService.isCurrent(userCode, generation)
                    .flatMap(current -> current
                            ? securityManager.details(jwt.getSubject())
                            : Mono.error(new InvalidBearerTokenException("Token has been revoked")))
                    .map(details -> (AbstractAuthenticationToken)
                            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
        };
    }

    /**
     * Configures and returns a SecurityWebFilterChain bean for Spring Security's WebFlux setup. This method customizes
     * various aspects of security including authorization rules, session management, basic authentication entry point,
     * form login, CSRF protection, logout handling, and OAuth2 login support.
     *
     * @param http The ServerHttpSecurity builder used to configure web security settings.
     * @return A configured SecurityWebFilterChain ready to be used in the application's security filter chain.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder,
                                                            SecurityManager securityManager, TokenService tokenService) {

        Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthConverter =
                this.jwtAuthenticationConverter(securityManager, tokenService);
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/captcha/code", "/oauth2/qr/code").permitAll()
                // the refresh token itself is the credential here, there is no access token yet;
                // matched by suffix because the module prefix / api version are added by path matching
                .matchers(exchange -> {
                    var request = exchange.getRequest();
                    boolean refreshing = HttpMethod.POST.equals(request.getMethod())
                            && request.getPath().pathWithinApplication().value().endsWith("/oauth2/refresh");
                    if (refreshing) {
                        return ServerWebExchangeMatcher.MatchResult.match();
                    }
                    return ServerWebExchangeMatcher.MatchResult.notMatch();
                }).permitAll()
                .matchers(PathRequest.toStaticResources().atCommonLocations().excluding(StaticResourceLocation.FONTS)).permitAll()
                .anyExchange().authenticated());
        // 无状态 JWT 下 session 互斥已无意义：认证不从 session 恢复（见下一行 NoOp），
        // 单会话/撤销语义改由 TokenService 的用户代际计数保证，因此不再配置 sessionManagement。
        http.securityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        http.httpBasic(httpBasicSpec -> httpBasicSpec
                .authenticationEntryPoint(new CustomServerAuthenticationEntryPoint()));
        http.formLogin(Customizer.withDefaults());
        http.logout(logout -> this.setLogout(logout, tokenService));
        http.oauth2Login(oAuth2LoginSpec ->
                oAuth2LoginSpec.authenticationSuccessHandler(authenticationSuccessHandler));
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthConverter)));
        return http.build();
    }

    /**
     * Configures the logout behavior for the server HTTP security.
     * <p>
     * This method sets up the logout process by adding a security context logout handler, a handler to clear site data
     * through headers, and delegates these handlers to manage the logout process. It also specifies the URL that
     * triggers the logout action and defines the success handler to return a HTTP status upon successful logout.
     *
     * @param logout The LogoutSpec instance to configure logout specifics of ServerHttpSecurity.
     */
    private void setLogout(ServerHttpSecurity.LogoutSpec logout, TokenService tokenService) {
        ServerLogoutHandler securityContext = new SecurityContextServerLogoutHandler();
        ClearSiteDataServerHttpHeadersWriter writer = new ClearSiteDataServerHttpHeadersWriter(
                ClearSiteDataServerHttpHeadersWriter.Directive.ALL);
        ServerLogoutHandler clearSiteData = new HeaderWriterServerLogoutHandler(writer);
        // JWT 是无状态的，不清除服务端状态就没有真正的登出：提升代际让该用户所有 access/refresh 一起失效
        ServerLogoutHandler revokeTokens = (_, authentication) ->
                authentication.getPrincipal() instanceof SecurityDetails details
                        ? tokenService.revokeAll(details.getCode()).then()
                        : Mono.empty();
        // 主动退出必须吊销记住我的 token：cookie 是 HttpOnly，前端拿不到也删不掉，
        // 只清浏览器端的话，那份 token 在 Redis 里还有 2 天可用
        ServerLogoutHandler revokeRememberMe =
                (exchange, _) -> this.rememberMeService.revoke(exchange.getExchange());
        DelegatingServerLogoutHandler logoutHandler =
                new DelegatingServerLogoutHandler(securityContext, clearSiteData, revokeRememberMe, revokeTokens);
        logout.requiresLogout(new PathPatternParserServerWebExchangeMatcher("/oauth2/logout"));
        logout.logoutHandler(logoutHandler);
        logout.logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler());
    }

    /**
     * CustomServerAuthenticationEntryPoint is a specialized entry point for handling authentication failures in a
     * reactive server environment. It extends the HttpBasicServerAuthenticationEntryPoint to customize the behavior
     * when an authentication exception occurs. Specifically, it differentiates between standard requests and
     * XMLHttpRequests (AJAX calls) to provide appropriate responses.
     *
     * <p>This class overrides the commence method to log authentication failure details and customize the response
     * based on the nature of the incoming request. If the request is identified as an AJAX call, it will return a JSON
     * formatted error response with an UNAUTHORIZED status code. Otherwise, it falls back to the default behavior
     * provided by its superclass.</p>
     *
     * <p>Notable Methods:</p>
     * <ul>
     *   <li>{@link #commence(ServerWebExchange, AuthenticationException)}: Handles the commencement of the authentication failure handling.</li>
     *   <li>{@link #createErrorResponse(ServerWebExchange, AuthenticationException)}: Constructs an error response object for AJAX request failures.</li>
     * </ul>
     */
    @Log4j2
    static class CustomServerAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {

        /**
         * Handles the commencement of failure handling due to an authentication exception during a server web exchange.
         * Determines the nature of the request and delegates to the appropriate failure handling method.
         *
         * @param exchange The current server web exchange containing the request and response objects.
         * @param e        The authentication exception that triggered this failure handling.
         * @return A Mono that, when subscribed to, signals the completion of the failure handling, typically without a
         * value (Void).
         */
        @Override
        public @NullMarked Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
            log.error("Authentication Failure: {}", e.getMessage(), e);
            ErrorResponse errorResponse = createErrorResponse(exchange, e);
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(errorResponse.getStatusCode());
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            var body = errorResponse.getBody();
            var bytes = BeanUtils.objectToBytes(body);
            var buffer = response.bufferFactory().wrap(bytes);
            return response.writeAndFlushWith(Mono.just(Mono.just(buffer)))
                    .doFinally(_ -> DataBufferUtils.release(buffer));
        }

        /**
         * Constructs an error response object for authentication failures.
         *
         * @param exchange The ServerWebExchange associated with the request that triggered the error.
         * @param ex       The AuthenticationException that caused the error response to be generated.
         * @return An ErrorResponse instance representing the authentication failure, including details from the
         * original request and the exception message.
         */
        private ErrorResponse createErrorResponse(ServerWebExchange exchange, AuthenticationException ex) {
            return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
                    .title("Authentication Failure").type(exchange.getRequest().getURI())
                    .headers(httpHeaders -> httpHeaders.addAll(exchange.getResponse().getHeaders()))
                    .build();

        }
    }
}