package com.plate.boot.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.jwt.JwtProperties;
import com.plate.boot.security.jwt.JwtService;
import com.plate.boot.security.jwt.TokenService;
import com.plate.boot.security.jwt.TokenType;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.ReactiveSessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.SessionLimit;
import org.springframework.security.web.server.authentication.logout.*;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry;
import org.springframework.web.ErrorResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * WebFlux security configuration: session-based browser endpoints and a stateless JWT
 * resource-server chain for the separated frontend. Registers the JWT decoder/encoder,
 * the password authentication manager and the CORS policy used by browser clients.
 */
@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final Oauth2SuccessHandler authenticationSuccessHandler;

    /**
     * Static so UsersService can obtain the encoder without instantiating this config class.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return ContextUtils.createDelegatingPasswordEncoder("bcrypt");
    }

    /**
     * Old-format access tokens carry a user code plus a generation counter; a revoked generation
     * must stop authenticating. New-format stateless tokens have no generation claim and skip it.
     */
    private static Mono<Void> generationCheck(Jwt jwt, TokenService tokenService) {
        String code = jwt.getClaimAsString(JwtService.CLAIM_CODE);
        Object generation = jwt.getClaim("gen");
        if (code == null || !(generation instanceof Number number)) {
            return Mono.empty();
        }
        try {
            return tokenService.isCurrent(UUID.fromString(code), number.longValue())
                    .flatMap(current -> current ? Mono.empty()
                            : Mono.<Void>error(new BadCredentialsException("Token has been revoked")));
        } catch (IllegalArgumentException e) {
            return Mono.error(new BadCredentialsException("Invalid token generation claim"));
        }
    }

    /**
     * Provides the OAuth2 authorized-client service backed by R2DBC.
     */
    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService oAuth2ClientService(DatabaseClient databaseClient,
                                                                     ReactiveClientRegistrationRepository clientRepository) {
        return new R2dbcReactiveOAuth2AuthorizedClientService(databaseClient, clientRepository);
    }

    /**
     * Provides the session registry used by concurrent-session control.
     */
    @Bean
    public <S extends Session> ReactiveSessionRegistry sessionRegistry(
            ReactiveSessionRepository<S> sessionRepository,
            ReactiveFindByIndexNameSessionRepository<S> indexedSessionRepository) {
        return new SpringSessionBackedReactiveSessionRegistry<>(sessionRepository, indexedSessionRepository);
    }

    /**
     * Session-backed security context used by the browser remember-me flow. Stateless JWT
     * requests never touch it; the resource-server filter authenticates them per request.
     */
    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    /**
     * Decoder used by the resource server to verify signatures and expiration. It shares the
     * derived key with {@link JwtService}, so tokens issued by the login endpoint validate here.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(JwtProperties properties) {
        NimbusJwtDecoder delegate = NimbusJwtDecoder
                .withSecretKey(JwtService.signingKey(properties.getSecret()))
                .macAlgorithm(MacAlgorithm.HS256).build();
        delegate.setJwtValidator(new JwtTimestampValidator(Duration.ZERO));
        return token -> Mono.fromCallable(() -> delegate.decode(token));
    }

    /**
     * Encoder injected into {@code TokenService} for the opaque refresh-token transport.
     */
    @Bean
    public JwtEncoder jwtEncoder(JwtProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(JwtService.signingKey(properties.getSecret())));
    }

    /**
     * Maps a verified access token to the authenticated principal. Refresh tokens are rejected
     * here so they can never be used as API credentials; the principal is reloaded on every
     * request so authority changes take effect immediately.
     */

    /**
     * Username/password authentication manager backing the stateless login endpoint.
     */
    @Bean
    @Primary
    public ReactiveAuthenticationManager passwordAuthenticationManager(SecurityManager securityManager,
                                                                       PasswordEncoder passwordEncoder) {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(securityManager);
        manager.setPasswordEncoder(passwordEncoder);
        manager.setUserDetailsPasswordService(securityManager);
        return manager;
    }

    /**
     * Kept as a plain factory method: registering it as a {@code Converter} bean would make
     * {@code R2dbcConfiguration}'s {@code List<Converter<?, ?>>} pull it into repository startup
     * and create a circular dependency through SecurityManager → UsersService → usersRepository.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter(
            SecurityManager securityManager, TokenService tokenService) {
        return jwt -> {
            String tokenType = jwt.getClaimAsString(JwtService.CLAIM_TOKEN_TYPE);
            if (tokenType != null && !TokenType.ACCESS.name().equals(tokenType)) {
                return Mono.error(new BadCredentialsException("Not an access token"));
            }
            return generationCheck(jwt, tokenService)
                    .then(securityManager.loadUserDetails(jwt.getSubject())
                            .ofType(SecurityDetails.class)
                            .switchIfEmpty(Mono.error(new BadCredentialsException("Unknown JWT subject")))
                            .<AbstractAuthenticationToken>map(details -> new UsernamePasswordAuthenticationToken(
                                    details, null, details.getAuthorities())));
        };
    }

    /**
     * Builds the security filter chain. JWT bearer requests are validated by the OAuth2
     * resource server; browser sessions (Basic Auth / OAuth2 login) keep working unchanged.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            SecurityManager securityManager,
            TokenService tokenService,
            ServerSecurityContextRepository securityContextRepository) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/captcha/code", "/oauth2/qr/code", "/oauth2/**").permitAll()
                .matchers(PathRequest.toStaticResources().atCommonLocations().excluding(StaticResourceLocation.FONTS)).permitAll()
                .anyExchange().authenticated());
        http.sessionManagement((sessions) -> sessions
                .concurrentSessions((concurrency) -> concurrency
                        .maximumSessions(SessionLimit.of(1))
                ));
        http.httpBasic(httpBasicSpec -> httpBasicSpec
                .authenticationEntryPoint(new CustomServerAuthenticationEntryPoint()));
        http.formLogin(Customizer.withDefaults());
        http.securityContextRepository(securityContextRepository);
        http.logout(this::setLogout);
        http.oauth2Login(oAuth2LoginSpec ->
                oAuth2LoginSpec.authenticationSuccessHandler(authenticationSuccessHandler));
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtDecoder(jwtDecoder)
                .jwtAuthenticationConverter(jwtAuthenticationConverter(securityManager, tokenService))));
        return http.build();
    }

    private void setLogout(ServerHttpSecurity.LogoutSpec logout) {
        ServerLogoutHandler securityContext = new SecurityContextServerLogoutHandler();
        ClearSiteDataServerHttpHeadersWriter writer = new ClearSiteDataServerHttpHeadersWriter(
                ClearSiteDataServerHttpHeadersWriter.Directive.ALL);
        ServerLogoutHandler clearSiteData = new HeaderWriterServerLogoutHandler(writer);
        DelegatingServerLogoutHandler logoutHandler =
                new DelegatingServerLogoutHandler(securityContext, clearSiteData);
        logout.requiresLogout(new PathPatternParserServerWebExchangeMatcher("/oauth2/logout"));
        logout.logoutHandler(logoutHandler);
        logout.logoutSuccessHandler(new HttpStatusReturningServerLogoutSuccessHandler());
    }

    /**
     * CSRF matcher that skips safe methods, stateless bearer requests and public auth endpoints.
     */
    static class IgnoreRequireCsrfProtectionMatcher implements ServerWebExchangeMatcher {
        private final Set<HttpMethod> allowedMethods = new HashSet<>(
                Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS));
        private final List<ServerWebExchangeMatcher> allowedMatchers = List.of(
                new PathPatternParserServerWebExchangeMatcher("/oauth2/none", HttpMethod.POST)
        );

        @Override
        public @NullMarked Mono<MatchResult> matches(ServerWebExchange exchange) {
            Mono<MatchResult> ignoreMono = new OrServerWebExchangeMatcher(allowedMatchers)
                    .matches(exchange).filter(MatchResult::isMatch)
                    .flatMap(_ -> MatchResult.notMatch())
                    .switchIfEmpty(Mono.defer(MatchResult::match));
            var request = exchange.getRequest();
            return Mono.justOrEmpty(request.getMethod()).filter(allowedMethods::contains)
                    .flatMap((_) -> MatchResult.notMatch())
                    .switchIfEmpty(Mono.defer(() -> isCsrfExempt(exchange) ? MatchResult.notMatch() : ignoreMono));
        }

        private static boolean isCsrfExempt(ServerWebExchange exchange) {
            String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return true;
            }
            String path = exchange.getRequest().getPath().pathWithinApplication().value();
            return path.endsWith("/auth/login") || path.endsWith("/auth/refresh");
        }
    }

    /**
     * Returns a JSON 401 body for AJAX clients instead of the default Basic Auth challenge.
     */
    @Log4j2
    static class CustomServerAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {

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

        private ErrorResponse createErrorResponse(ServerWebExchange exchange, AuthenticationException ex) {
            return ErrorResponse.builder(ex, HttpStatus.UNAUTHORIZED, ex.getMessage())
                    .title("Authentication Failure").type(exchange.getRequest().getURI())
                    .headers(httpHeaders -> httpHeaders.addAll(exchange.getResponse().getHeaders()))
                    .build();

        }
    }
}
