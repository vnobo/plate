package com.platform.boot.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.*;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.header.ClearSiteDataServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class contains the security configuration for the application.
 * It sets up the security filters and rules for the application.
 * It also defines the beans required for the security configuration.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
public class SecurityConfiguration {

    /**
     * This method defines the PasswordEncoder bean required for the security configuration.
     *
     * @return PasswordEncoder object
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * This method defines the SecurityWebFilterChain bean required for the security configuration.
     * It sets up the security filters and rules for the application.
     *
     * @param http ServerHttpSecurity object
     * @return SecurityWebFilterChain object
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchange -> {
            exchange.pathMatchers("/captcha/code").permitAll();
            exchange.pathMatchers("/oauth2/qr/code").permitAll();
            exchange.matchers(PathRequest.toStaticResources().atCommonLocations()).permitAll();
            exchange.anyExchange().authenticated();
        });
        http.securityContextRepository(new WebSessionServerSecurityContextRepository());
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(httpBasicSpec -> httpBasicSpec
                .authenticationEntryPoint(new CustomServerAuthenticationEntryPoint()));
        http.formLogin(Customizer.withDefaults());
        http.csrf(this::setCsrfSpec);
        http.logout(this::setLogout);
        return http.build();
    }

    /**
     * This method sets up the CSRF protection for the application.
     *
     * @param csrfSpec ServerHttpSecurity.CsrfSpec object
     */
    private void setCsrfSpec(ServerHttpSecurity.CsrfSpec csrfSpec) {
        csrfSpec.requireCsrfProtectionMatcher(new IgnoreRequireCsrfProtectionMatcher());
        csrfSpec.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
        csrfSpec.csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler());
    }

    /**
     * This method sets up the logout functionality for the application.
     *
     * @param logout ServerHttpSecurity.LogoutSpec object
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
     * This class defines a custom ServerWebExchangeMatcher that ignores CSRF protection for certain requests.
     * It allows GET, HEAD, TRACE, and OPTIONS requests, as well as requests to the "/oauth2/none" endpoint with a POST method.
     */
    static class IgnoreRequireCsrfProtectionMatcher implements ServerWebExchangeMatcher {
        private final Set<HttpMethod> allowedMethods = new HashSet<>(
                Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.TRACE, HttpMethod.OPTIONS));
        private final List<ServerWebExchangeMatcher> allowedMatchers = List.of(
                new PathPatternParserServerWebExchangeMatcher("/oauth2/none", HttpMethod.POST)
        );

        /**
         * This method checks if the given ServerWebExchange matches the allowed methods and matchers.
         * If it does, it returns a MatchResult indicating that CSRF protection should be ignored.
         * If it does not, it returns a MatchResult indicating that CSRF protection should be enforced.
         *
         * @param exchange ServerWebExchange object
         * @return Mono<MatchResult> indicating whether CSRF protection should be ignored or enforced
         */
        @Override
        public Mono<MatchResult> matches(ServerWebExchange exchange) {
            Mono<MatchResult> ignoreMono = new OrServerWebExchangeMatcher(allowedMatchers)
                    .matches(exchange).filter(MatchResult::isMatch)
                    .flatMap(res -> MatchResult.notMatch())
                    .switchIfEmpty(MatchResult.match());
            var request = exchange.getRequest();
            return Mono.justOrEmpty(request.getMethod()).filter(allowedMethods::contains)
                    .flatMap((m) -> MatchResult.notMatch()).switchIfEmpty(ignoreMono);
        }
    }

    static class CustomServerAuthenticationEntryPoint extends HttpBasicServerAuthenticationEntryPoint {
        private static final Log log = LogFactory.getLog(CustomServerAuthenticationEntryPoint.class);

        /**
         * This method is called when authentication fails. It first checks if the request matches the captcha protection
         * matcher. If it does, it generates a captcha token and returns a response with a JSON body containing the captcha
         * URL and an error message. If it does not, it returns a response with a JSON body containing an error message.
         *
         * @param exchange ServerWebExchange object
         * @param e        AuthenticationException object
         * @return Mono<Void> indicating completion of the response
         */
        @Override
        public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
            String xRequestedWith = "X-Requested-With";
            String xmlHttpRequest = "XMLHttpRequest";
            String requestedWith = exchange.getRequest().getHeaders().getFirst(xRequestedWith);

            log.error("认证失败! 信息: %s".formatted(e.getMessage()));

            if (requestedWith != null && requestedWith.contains(xmlHttpRequest)) {
                var response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                var body = """
                        {"code":401,"msg":"认证失败,检查你的用户名,密码是否正确或安全密钥是否过期!","errors":"%s"}
                        """;
                body = body.formatted(e.getMessage());
                var dataBufferFactory = response.bufferFactory();
                var bodyBuffer = dataBufferFactory.wrap(body.getBytes());
                return response.writeAndFlushWith(Flux.just(bodyBuffer).windowUntilChanged())
                        .doOnError((error) -> DataBufferUtils.release(bodyBuffer));
            }
            return super.commence(exchange, e);
        }

    }
}