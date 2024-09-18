package com.plate.boot.config;

import com.plate.boot.commons.ErrorResponse;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.security.oauth2.Oauth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.R2dbcReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.*;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.plate.boot.commons.utils.ContextUtils.RULE_ADMINISTRATORS;
import static com.plate.boot.config.SessionConfiguration.XML_HTTP_REQUEST;
import static com.plate.boot.config.SessionConfiguration.X_REQUESTED_WITH;

/**
 * SecurityConfiguration configures the application's security settings by defining beans and rules
 * for authentication, authorization, session management, CSRF protection, logout behavior, and more.
 * It leverages Spring Security features to secure both RESTful endpoints and RSocket interactions.
 */
@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
@EnableRSocketSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final Oauth2SuccessHandler authenticationSuccessHandler;

    /**
     * Configures and provides an instance of {@link ReactiveOAuth2AuthorizedClientService} which is primarily responsible
     * for managing OAuth2 authorized client details, storing and retrieving them from a reactive data source via
     * {@link DatabaseClient} and handling client registrations defined in {@link ReactiveClientRegistrationRepository}.
     *
     * @param databaseClient   The reactive database client used for interacting with the data source to persist OAuth2 authorization data.
     * @param clientRepository The repository containing the definitions of all registered clients for OAuth2 authentication.
     * @return A configured instance of {@link R2dbcReactiveOAuth2AuthorizedClientService} capable of handling OAuth2 authorized client services within a reactive context.
     */
    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService oAuth2ClientService(DatabaseClient databaseClient,
                                                                     ReactiveClientRegistrationRepository clientRepository) {
        return new R2dbcReactiveOAuth2AuthorizedClientService(databaseClient, clientRepository);
    }

    /**
     * Creates and configures a SpringSessionBackedReactiveSessionRegistry bean.
     * This registry is designed to manage sessions within a reactive environment, backed by the provided
     * ReactiveSessionRepository and ReactiveFindByIndexNameSessionRepository instances.
     *
     * @param <S> The type of session extending the Session interface.
     * @param sessionRepository A reactive session repository for storing and retrieving session data.
     * @param indexedSessionRepository A reactive session repository capable of finding sessions by index name,
     *                                enhancing session management capabilities.
     * @return An instance of SpringSessionBackedReactiveSessionRegistry configured with the given repositories,
     *         ready to manage and provide session-related services in a reactive context.
     */
    @Bean
    public <S extends Session> SpringSessionBackedReactiveSessionRegistry<S> sessionRegistry(
            ReactiveSessionRepository<S> sessionRepository,
            ReactiveFindByIndexNameSessionRepository<S> indexedSessionRepository) {
        return new SpringSessionBackedReactiveSessionRegistry<>(sessionRepository, indexedSessionRepository);
    }

    /**
     * Provides a PasswordEncoder bean that utilizes a delegating strategy for encoding passwords.
     * This delegating encoder can handle different password encoding schemes based on encoded passwords' ids.
     * It consults a set of encoders and picks the one matching the id prepended to the encoded password.
     *
     * @return A {@link PasswordEncoder} instance which is capable of delegating to the appropriate password encoding strategy.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Configures and provides an RSocket interceptor for security purposes.
     * This method sets up authorization policies for RSocket interactions,
     * enforcing authentication requirements and permission rules.
     *
     * @param rsocket The RSocketSecurity configuration object used to define security rules.
     * @return A configured PayloadSocketAcceptorInterceptor that enforces the defined security policies for RSocket communications.
     */
    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket) {
        rsocket.authorizePayload(authorize ->
                        authorize
                                .route("request.stream").authenticated()
                                .anyRequest().authenticated()
                                .anyExchange().permitAll()
                )
                .simpleAuthentication(Customizer.withDefaults());
        return rsocket.build();
    }

    /**
     * Configures and returns a SecurityWebFilterChain bean for Spring Security's WebFlux setup.
     * This method customizes various aspects of security including authorization rules,
     * session management, basic authentication entry point, form login, CSRF protection,
     * logout handling, and OAuth2 login support.
     *
     * @param http The ServerHttpSecurity builder used to configure web security settings.
     * @return A configured SecurityWebFilterChain ready to be used in the application's security filter chain.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchange -> {
            exchange.pathMatchers("/captcha/code", "/oauth2/qr/code").permitAll();
            exchange.matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            exchange.anyExchange().authenticated();
        });
        http.sessionManagement((sessions) -> sessions
                .concurrentSessions((concurrency) -> concurrency.maximumSessions((authentication) -> {
                    if (authentication.getAuthorities().stream()
                            .anyMatch(a -> RULE_ADMINISTRATORS.equals(a.getAuthority()))) {
                        return Mono.empty();
                    }
                    return Mono.just(3);
                })));
        http.httpBasic(httpBasicSpec -> httpBasicSpec
                .authenticationEntryPoint(new CustomServerAuthenticationEntryPoint()));
        http.formLogin(Customizer.withDefaults());
        http.csrf(this::setCsrfSpec);
        http.logout(this::setLogout);
        http.oauth2Login(oAuth2LoginSpec -> oAuth2LoginSpec.authenticationSuccessHandler(authenticationSuccessHandler));
        return http.build();
    }

    /**
     * Configures the Cross-Site Request Forgery (CSRF) protection specifications within the provided {@link ServerHttpSecurity.CsrfSpec}.
     * This method customizes the CSRF behavior by:
     * <ul>
     *   <li>Defining a matcher to ignore CSRF protection for certain endpoints.</li>
     *   <li>Setting a cookie-based repository for storing CSRF tokens with HTTP-only flag disabled.</li>
     *   <li>Registering a handler to place the CSRF token as a request attribute.</li>
     * </ul>
     *
     * @param csrfSpec The CSRF specification object to be configured, part of the {@link ServerHttpSecurity} configuration.
     */
    private void setCsrfSpec(ServerHttpSecurity.CsrfSpec csrfSpec) {
        csrfSpec.requireCsrfProtectionMatcher(new IgnoreRequireCsrfProtectionMatcher());
        csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
        csrfSpec.csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler());
    }

    /**
     * Configures the logout behavior for the server HTTP security.
     *
     * This method sets up the logout process by adding a security context logout handler,
     * a handler to clear site data through headers, and delegates these handlers
     * to manage the logout process. It also specifies the URL that triggers the logout
     * action and defines the success handler to return a HTTP status upon successful logout.
     *
     * @param logout The LogoutSpec instance to configure logout specifics of ServerHttpSecurity.
     */
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
     * Matcher implementation to exclude specific HTTP requests from requiring CSRF protection.
     * This is particularly useful for endpoints that are deemed safe from CSRF attacks due to their nature
     * or implementation specifics, such as certain GET requests or known safe POST requests like OAuth2 token exchanges.
     * The matcher combines a set of allowed HTTP methods and a list of more specific matchers to determine
     * if CSRF protection should be ignored for a given request.
     */
    static class IgnoreRequireCsrfProtectionMatcher implements ServerWebExchangeMatcher {
        private final Set<HttpMethod> allowedMethods = new HashSet<>(
                Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS));
        private final List<ServerWebExchangeMatcher> allowedMatchers = List.of(
                new PathPatternParserServerWebExchangeMatcher("/oauth2/none", HttpMethod.POST)
        );

        /**
         * Evaluates whether a given server web exchange should be excluded from CSRF protection.
         * It checks if the request's HTTP method is within the allowed set and if it matches any
         * specified additional matchers.
         *
         * @param exchange The server web exchange to evaluate for CSRF protection exclusion.
         * @return A Mono that emits a MatchResult indicating whether the exchange should be ignored for CSRF protection.
         *         - If the request method is allowed or matches custom criteria, emits MatchResult.notMatch(),
         *           suggesting the request should be ignored by CSRF protection.
         *         - Otherwise, emits MatchResult.match() indicating standard CSRF protection should apply.
         */
        @Override
        public Mono<MatchResult> matches(ServerWebExchange exchange) {
            Mono<MatchResult> ignoreMono = new OrServerWebExchangeMatcher(allowedMatchers)
                    .matches(exchange).filter(MatchResult::isMatch)
                    .flatMap(res -> MatchResult.notMatch())
                    .switchIfEmpty(Mono.defer(MatchResult::match));
            var request = exchange.getRequest();
            return Mono.justOrEmpty(request.getMethod()).filter(allowedMethods::contains)
                    .flatMap((m) -> MatchResult.notMatch()).switchIfEmpty(Mono.defer(() -> ignoreMono));
        }
    }

    /**
     * CustomServerAuthenticationEntryPoint is a specialized entry point for handling authentication failures in a reactive server environment.
     * It extends the HttpBasicServerAuthenticationEntryPoint to customize the behavior when an authentication exception occurs.
     * Specifically, it differentiates between standard requests and XMLHttpRequests (AJAX calls) to provide appropriate responses.
     *
     * <p>This class overrides the commence method to log authentication failure details and customize the response based on the nature
     * of the incoming request. If the request is identified as an AJAX call, it will return a JSON formatted error response with
     * an UNAUTHORIZED status code. Otherwise, it falls back to the default behavior provided by its superclass.</p>
     *
     * <p>Notable Methods:</p>
     * <ul>
     *   <li>{@link #commence(ServerWebExchange, AuthenticationException)}: Handles the commencement of the authentication failure handling.</li>
     *   <li>{@link #isXmlHttpRequest(String)}: Determines if the request is an XMLHttpRequest.</li>
     *   <li>{@link #handleXmlHttpRequestFailure(ServerWebExchange, AuthenticationException)}: Manages the response for failed AJAX requests.</li>
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
         * @param e The authentication exception that triggered this failure handling.
         * @return A Mono that, when subscribed to, signals the completion of the failure handling, typically without a value (Void).
         */
        @Override
        public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
            ServerHttpRequest request = exchange.getRequest();
            String requestedWith = request.getHeaders().getFirst(X_REQUESTED_WITH);

            log.error("认证失败! 信息: {}", e.getMessage());
            if (isXmlHttpRequest(requestedWith)) {
                return handleXmlHttpRequestFailure(exchange, e);
            }
            return super.commence(exchange, e);
        }

        /**
         * Determines whether the provided request header 'requestedWith' indicates an XMLHttpRequest.
         * This is typically used to check if the request was made via AJAX.
         *
         * @param requestedWith The value of the 'X-Requested-With' header from the HTTP request.
         * @return {@code true} if the 'requestedWith' header indicates an XMLHttpRequest, {@code false} otherwise.
         */
        private boolean isXmlHttpRequest(String requestedWith) {
            return requestedWith != null && requestedWith.contains(XML_HTTP_REQUEST);
        }

        /**
         * Handles the failure case of an XML HTTP request by setting the response status to UNAUTHORIZED,
         * preparing an error response in JSON format, and sending it back to the client.
         *
         * @param exchange The current server web exchange containing the request and response objects.
         * @param e The authentication exception that caused the request handling failure.
         * @return A Mono that, when subscribed to, completes after writing the error response to the client
         *         and handling any potential errors during the write operation by releasing allocated resources.
         */
        private Mono<Void> handleXmlHttpRequestFailure(ServerWebExchange exchange, AuthenticationException e) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            ErrorResponse errorResponse = createErrorResponse(exchange, e);
            var body = BeanUtils.objectToBytes(errorResponse);
            var dataBufferFactory = response.bufferFactory();
            var bodyBuffer = dataBufferFactory.wrap(body);

            return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged())
                    .doOnError((error) -> DataBufferUtils.release(bodyBuffer));
        }

        /**
         * Constructs an error response object for authentication failures.
         *
         * @param exchange The ServerWebExchange associated with the request that triggered the error.
         * @param e        The AuthenticationException that caused the error response to be generated.
         * @return An ErrorResponse instance representing the authentication failure, including details
         *         from the original request and the exception message.
         */
        private ErrorResponse createErrorResponse(ServerWebExchange exchange, AuthenticationException e) {
            return ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                    401, "认证失败,检查你的用户名,密码是否正确或安全密钥是否过期!", List.of(e.getMessage()));
        }

    }
}