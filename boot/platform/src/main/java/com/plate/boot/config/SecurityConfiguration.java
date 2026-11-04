package com.plate.boot.config;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.jwt.JwtProperties;
import com.plate.boot.security.jwt.JwtService;
import com.plate.boot.security.jwt.TokenType;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.ErrorResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Stateless JWT security configuration. The filter chain validates
 * {@code Authorization: Bearer <accessToken>} on every request; the anonymous authentication
 * endpoints (local login, refresh, OAuth2 login and account binding) are handled by
 * {@code SecurityController}.
 */
@Configuration(proxyBeanMethods = false)
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfiguration {

    /**
     * Static so UsersService can obtain the encoder without instantiating this config class.
     */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return ContextUtils.createDelegatingPasswordEncoder("bcrypt");
    }

    /**
     * Verifies HS256 signatures and expiration using the same derived key as {@link JwtService}.
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
     * Username/password authentication manager backing the login endpoint.
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
     * Maps a verified access JWT to the authenticated principal. Refresh tokens are rejected
     * so they can never be used as API credentials; the principal is reloaded on each request
     * so authority changes and disabled accounts take effect immediately.
     * <p>
     * Kept as a plain factory method: registering it as a {@code Converter} bean would make
     * {@code R2dbcConfiguration}'s {@code List<Converter<?, ?>>} pull it into repository startup
     * and create a circular dependency through SecurityManager → UsersService → usersRepository.
     */
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter(
            SecurityManager securityManager) {
        return jwt -> {
            String tokenType = jwt.getClaimAsString(JwtService.CLAIM_TOKEN_TYPE);
            if (!TokenType.ACCESS.name().equals(tokenType)) {
                return Mono.error(new BadCredentialsException("Not an access token"));
            }
            return securityManager.loadUserDetails(Objects.requireNonNull(jwt.getSubject()))
                    .ofType(SecurityDetails.class)
                    .switchIfEmpty(Mono.error(new BadCredentialsException("Unknown JWT subject")))
                    .map(details -> new UsernamePasswordAuthenticationToken(
                            details, null, details.getAuthorities()));
        };
    }

    /**
     * Builds the stateless filter chain. Sessions, Basic Auth, form login, OAuth2 login,
     * remember-me and logout are all intentionally absent: JWT is the only mechanism.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            SecurityManager securityManager,
            ServerSecurityContextRepository securityContextRepository) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.POST, "/**/oauth2/login", "/**/oauth2/login/*",
                        "/**/oauth2/refresh", "/**/oauth2/bind").permitAll()
                .pathMatchers("/captcha/code", "/oauth2/qr/code").permitAll()
                .matchers(PathRequest.toStaticResources().atCommonLocations()
                        .excluding(StaticResourceLocation.FONTS)).permitAll()
                .anyExchange().authenticated());
        http.securityContextRepository(securityContextRepository);
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter(securityManager)))
                .authenticationEntryPoint(new CustomServerAuthenticationEntryPoint()));
        return http.build();
    }

    /**
     * CORS policy for the separated frontend: the SPA sends {@code Authorization: Bearer ...}
     * from another origin, so that header must be allowed. Origins default to the local dev
     * server and can be overridden by configuration.
     */
    private CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        //config.setAllowedOriginPatterns();
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "x-api-version"));
        config.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        return config;
    }

    /**
     * Returns a JSON 401 body for API clients instead of the default Basic Auth challenge.
     */
    @Log4j2
    static class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

        @Override
        public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
            log.error("Authentication Failure: {}", e.getMessage(), e);
            ErrorResponse errorResponse = ErrorResponse
                    .builder(e, HttpStatus.UNAUTHORIZED, e.getMessage())
                    .title("Authentication Failure").type(exchange.getRequest().getURI())
                    .headers(headers -> headers.addAll(exchange.getResponse().getHeaders()))
                    .build();
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(errorResponse.getStatusCode());
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] bytes = BeanUtils.objectToBytes(errorResponse.getBody());
            var buffer = response.bufferFactory().wrap(bytes);
            return response.writeAndFlushWith(Mono.just(Mono.just(buffer)))
                    .doFinally(_ -> DataBufferUtils.release(buffer));
        }
    }
}
