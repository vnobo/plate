package com.plate.boot.config;

import com.plate.boot.commons.ErrorResponse;
import com.plate.boot.commons.utils.ContextUtils;
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
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
@EnableRSocketSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final Oauth2SuccessHandler authenticationSuccessHandler;

    @Bean
    @Primary
    public ReactiveOAuth2AuthorizedClientService oAuth2ClientService(DatabaseClient databaseClient,
                                                                     ReactiveClientRegistrationRepository clientRepository) {
        return new R2dbcReactiveOAuth2AuthorizedClientService(databaseClient, clientRepository);
    }

    @Bean
    public <S extends Session> SpringSessionBackedReactiveSessionRegistry<S> sessionRegistry(
            ReactiveSessionRepository<S> sessionRepository,
            ReactiveFindByIndexNameSessionRepository<S> indexedSessionRepository) {
        return new SpringSessionBackedReactiveSessionRegistry<>(sessionRepository, indexedSessionRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

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

    private void setCsrfSpec(ServerHttpSecurity.CsrfSpec csrfSpec) {
        csrfSpec.requireCsrfProtectionMatcher(new IgnoreRequireCsrfProtectionMatcher());
        csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
        csrfSpec.csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler());
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

    static class IgnoreRequireCsrfProtectionMatcher implements ServerWebExchangeMatcher {
        private final Set<HttpMethod> allowedMethods = new HashSet<>(
                Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS));
        private final List<ServerWebExchangeMatcher> allowedMatchers = List.of(
                new PathPatternParserServerWebExchangeMatcher("/oauth2/none", HttpMethod.POST)
        );

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

    @Log4j2
    static class CustomServerAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {

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

        private boolean isXmlHttpRequest(String requestedWith) {
            return requestedWith != null && requestedWith.contains(XML_HTTP_REQUEST);
        }

        private Mono<Void> handleXmlHttpRequestFailure(ServerWebExchange exchange, AuthenticationException e) {
            var response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            ErrorResponse errorResponse = createErrorResponse(exchange, e);
            var body = ContextUtils.objectToBytes(errorResponse);
            var dataBufferFactory = response.bufferFactory();
            var bodyBuffer = dataBufferFactory.wrap(body);

            return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged())
                    .doOnError((error) -> DataBufferUtils.release(bodyBuffer));
        }

        private ErrorResponse createErrorResponse(ServerWebExchange exchange, AuthenticationException e) {
            return ErrorResponse.of(exchange.getRequest().getId(), exchange.getRequest().getPath().value(),
                    401, "认证失败,检查你的用户名,密码是否正确或安全密钥是否过期!", List.of(e.getMessage()));
        }

    }
}