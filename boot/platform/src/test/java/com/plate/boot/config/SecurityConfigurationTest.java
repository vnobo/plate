package com.plate.boot.config;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.jwt.TokenService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the inner classes of {@link SecurityConfiguration}, plus the (private) JWT
 * authentication converter which maps a verified access token to a {@link SecurityDetails} principal.
 */
class SecurityConfigurationTest {

    private static JsonMapper savedMapper;

    @BeforeAll
    static void setUpMapper() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDownMapper() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    private static SecurityDetails detailsWithAuthorities() {
        SecurityDetails d = new SecurityDetails(
                List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("username", "admin"), "username");
        d.setCode(UUID.randomUUID());
        d.setUsername("admin");
        return d;
    }

    @Test
    void csrfMatcherIgnoresGetRequest() {
        SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher matcher =
                new SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/path").build());

        MatchResult result = matcher.matches(exchange).block();

        assertThat(result).isNotNull();
        assertThat(result.isMatch()).isFalse();
    }

    @Test
    void csrfMatcherIgnoresOptionsRequest() {
        SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher matcher =
                new SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(org.springframework.http.HttpMethod.OPTIONS, "/path").build());

        MatchResult result = matcher.matches(exchange).block();

        assertThat(result).isNotNull();
        assertThat(result.isMatch()).isFalse();
    }

    @Test
    void csrfMatcherIgnoresOauth2NonePostRequest() {
        SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher matcher =
                new SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/oauth2/none").build());

        MatchResult result = matcher.matches(exchange).block();

        assertThat(result).isNotNull();
        assertThat(result.isMatch()).isFalse();
    }

    @Test
    void csrfMatcherRequiresProtectionForOtherPostRequest() {
        SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher matcher =
                new SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/oauth2/token").build());

        MatchResult result = matcher.matches(exchange).block();

        assertThat(result).isNotNull();
        assertThat(result.isMatch()).isTrue();
    }

    @Test
    void csrfMatcherRequiresProtectionForPutRequest() {
        SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher matcher =
                new SecurityConfiguration.IgnoreRequireCsrfProtectionMatcher();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.put("/resource/1").build());

        MatchResult result = matcher.matches(exchange).block();

        assertThat(result).isNotNull();
        assertThat(result.isMatch()).isTrue();
    }

    @Test
    void authenticationEntryPointWritesJsonUnauthorizedResponse() {
        SecurityConfiguration.CustomServerAuthenticationEntryPoint entryPoint =
                new SecurityConfiguration.CustomServerAuthenticationEntryPoint();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/login").build());
        org.springframework.security.authentication.BadCredentialsException exception =
                new org.springframework.security.authentication.BadCredentialsException("Bad credentials");

        StepVerifier.create(entryPoint.commence(exchange, exception)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = exchange.getResponse().getBodyAsString().block();
        assertThat(body).contains("Authentication Failure");
    }

    @Test
    void jwtConverterReturnsPrincipalWhenCurrent() throws Exception {
        Jwt jwt = mock(Jwt.class);
        UUID code = UUID.randomUUID();
        when(jwt.getClaimAsString("code")).thenReturn(code.toString());
        when(jwt.getClaim("gen")).thenReturn(5L);
        when(jwt.getSubject()).thenReturn("admin");
        SecurityDetails details = detailsWithAuthorities();
        com.plate.boot.security.SecurityManager sm = mock(com.plate.boot.security.SecurityManager.class);
        TokenService ts = mock(TokenService.class);
        when(ts.isCurrent(code, 5L)).thenReturn(Mono.just(true));
        when(sm.details("admin")).thenReturn(Mono.just(details));
        // re-create converter bound to the stubbed mocks
        SecurityConfiguration config = new SecurityConfiguration(null, null);
        Method m = SecurityConfiguration.class.getDeclaredMethod(
                "jwtAuthenticationConverter", com.plate.boot.security.SecurityManager.class, TokenService.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Converter<Jwt, Mono<AbstractAuthenticationToken>> bound =
                (Converter<Jwt, Mono<AbstractAuthenticationToken>>) m.invoke(config, sm, ts);

        StepVerifier.create(bound.convert(jwt))
                .assertNext(auth -> assertThat(auth.getPrincipal()).isSameAs(details))
                .verifyComplete();
    }

    @Test
    void jwtConverterRejectsRevokedToken() throws Exception {
        Jwt jwt = mock(Jwt.class);
        UUID code = UUID.randomUUID();
        when(jwt.getClaimAsString("code")).thenReturn(code.toString());
        when(jwt.getClaim("gen")).thenReturn(5L);
        com.plate.boot.security.SecurityManager sm = mock(com.plate.boot.security.SecurityManager.class);
        TokenService ts = mock(TokenService.class);
        when(ts.isCurrent(code, 5L)).thenReturn(Mono.just(false));
        SecurityConfiguration config = new SecurityConfiguration(null, null);
        Method m = SecurityConfiguration.class.getDeclaredMethod(
                "jwtAuthenticationConverter", com.plate.boot.security.SecurityManager.class, TokenService.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Converter<Jwt, Mono<AbstractAuthenticationToken>> bound =
                (Converter<Jwt, Mono<AbstractAuthenticationToken>>) m.invoke(config, sm, ts);

        StepVerifier.create(bound.convert(jwt))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(InvalidBearerTokenException.class))
                .verify();
    }

    @Test
    void jwtConverterRejectsMalformedClaims() throws Exception {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("code")).thenReturn(null);
        when(jwt.getClaim("gen")).thenReturn(null);
        com.plate.boot.security.SecurityManager sm = mock(com.plate.boot.security.SecurityManager.class);
        TokenService ts = mock(TokenService.class);
        SecurityConfiguration config = new SecurityConfiguration(null, null);
        Method m = SecurityConfiguration.class.getDeclaredMethod(
                "jwtAuthenticationConverter", com.plate.boot.security.SecurityManager.class, TokenService.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Converter<Jwt, Mono<AbstractAuthenticationToken>> bound =
                (Converter<Jwt, Mono<AbstractAuthenticationToken>>) m.invoke(config, sm, ts);

        StepVerifier.create(bound.convert(jwt))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(InvalidBearerTokenException.class))
                .verify();
    }
}
