package com.plate.boot.commons.utils;

import com.plate.boot.commons.base.AbstractEvent;
import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the pure parts of {@link ContextUtils} (no Spring / container required).
 * <p>
 * {@code securityDetails()} and {@code eventPublisher(...)} are reactive/static and are exercised
 * with mocked collaborators. The reactive security-context test uses
 * {@link ReactiveSecurityContextHolder#withSecurityContext} without any live server.
 */
class ContextUtilsTest {

    private static JsonMapper savedObjectMapper;
    private static CacheManager savedCacheManager;
    private static ApplicationEventPublisher savedEventPublisher;

    @BeforeAll
    static void captureStatics() {
        savedObjectMapper = ContextUtils.OBJECT_MAPPER;
        savedCacheManager = ContextUtils.CACHE_MANAGER;
        savedEventPublisher = ContextUtils.APPLICATION_EVENT_PUBLISHER;
    }

    @AfterAll
    static void restoreStatics() {
        ContextUtils.OBJECT_MAPPER = savedObjectMapper;
        ContextUtils.CACHE_MANAGER = savedCacheManager;
        ContextUtils.APPLICATION_EVENT_PUBLISHER = savedEventPublisher;
    }

    @Test
    void nextIdReturnsUuidV7() {
        UUID id = ContextUtils.nextId();

        assertThat(id).isNotNull();
        assertThat(id.version()).isEqualTo(7);
        assertThat(id.variant()).isEqualTo(2);
    }

    @Test
    void defaultUuidCodeIsAllZeros() {
        assertThat(ContextUtils.DEFAULT_UUID_CODE).isEqualTo(new UUID(0L, 0L));
    }

    @Test
    void ruleAdministratorsConstantHasExpectedValue() {
        assertThat(ContextUtils.RULE_ADMINISTRATORS).isEqualTo("ROLE_SYSTEM_ADMINISTRATORS");
    }

    @Test
    void encodeToSha256IsDeterministicAndBase64() throws Exception {
        String input = "test";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        String expected = Base64.getEncoder().encodeToString(hash);

        assertThat(ContextUtils.encodeToSHA256(input)).isEqualTo(expected);
        assertThat(ContextUtils.encodeToSHA256(null)).isNull();
    }

    @Test
    void createDelegatingPasswordEncoderEncodesAndMatches() {
        PasswordEncoder encoder = ContextUtils.createDelegatingPasswordEncoder("bcrypt");
        String encoded = encoder.encode("secret");

        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(encoder.matches("secret", encoded)).isTrue();
        assertThat(encoder.matches("wrong", encoded)).isFalse();
    }

    @Test
    void createDelegatingPasswordEncoderNoopPrefix() {
        PasswordEncoder encoder = ContextUtils.createDelegatingPasswordEncoder("noop");

        assertThat(encoder.encode("secret")).isEqualTo("{noop}secret");
        assertThat(encoder.matches("secret", "{noop}secret")).isTrue();
    }

    @Test
    void createDelegatingPasswordEncoderDefaultsToBcryptForNullId() {
        PasswordEncoder encoder = ContextUtils.createDelegatingPasswordEncoder(null);

        assertThat(encoder.matches("secret", encoder.encode("secret"))).isTrue();
    }

    @Test
    void getClientIpAddressReturnsNullForNullRequest() {
        assertThat(ContextUtils.getClientIpAddress(null)).isNull();
    }

    @Test
    void getClientIpAddressReadsXForwardedFor() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "8.8.8.8");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isEqualTo("8.8.8.8");
    }

    @Test
    void getClientIpAddressFallsBackToRemoteAddressWhenHeaderPrivate() throws Exception {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "10.0.0.1");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(
                new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 1234));

        assertThat(ContextUtils.getClientIpAddress(req)).isEqualTo("8.8.8.8");
    }

    @Test
    void getClientIpAddressUsesFirstValueOfCommaSeparatedXForwardedFor() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "8.8.8.8, 10.0.0.1, 172.16.0.1");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isEqualTo("8.8.8.8");
    }

    @Test
    void getClientIpAddressSkipsPrivateXForwardedForAndUsesNextHeaderCandidate() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "192.168.0.1, 8.8.4.4");
        headers.add("X-Real-IP", "8.8.4.4");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isEqualTo("8.8.4.4");
    }

    @Test
    void getClientIpAddressReadsXRealIp() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Real-IP", "8.8.4.4");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isEqualTo("8.8.4.4");
    }

    @Test
    void getClientIpAddressFiltersLoopbackAndUnresolvable() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "127.0.0.1");
        headers.add("X-Real-IP", "not-a-real-host");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isNull();
    }

    @Test
    void getClientIpAddressReturnsNullWhenRemoteAddressHasNoAddress() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        InetSocketAddress remoteAddress = mock(InetSocketAddress.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        when(req.getRemoteAddress()).thenReturn(remoteAddress);
        when(remoteAddress.getAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isNull();
    }

    @Test
    void getClientIpAddressFallsBackToRemoteAddressWhenNoHeaders() throws Exception {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        when(req.getHeaders()).thenReturn(new HttpHeaders());
        when(req.getRemoteAddress()).thenReturn(
                new InetSocketAddress(InetAddress.getByName("9.9.9.9"), 80));

        assertThat(ContextUtils.getClientIpAddress(req)).isEqualTo("9.9.9.9");
    }

    @Test
    void getClientIpAddressFiltersEmptyIpHeaderValue() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isNull();
    }

    @Test
    void getClientIpAddressFiltersUnspecifiedIpv4() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "0.0.0.0");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isNull();
    }

    @Test
    void getClientIpAddressFiltersUnspecifiedIpv6() {
        ServerHttpRequest req = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "::");
        when(req.getHeaders()).thenReturn(headers);
        when(req.getRemoteAddress()).thenReturn(null);

        assertThat(ContextUtils.getClientIpAddress(req)).isNull();
    }

    @Test
    void createDelegatingPasswordEncoderDefaultsToBcryptForEmptyId() {
        PasswordEncoder encoder = ContextUtils.createDelegatingPasswordEncoder("");

        assertThat(encoder.encode("secret")).startsWith("{bcrypt}");
        assertThat(encoder.matches("secret", encoder.encode("secret"))).isTrue();
    }

    @Test
    void createDelegatingPasswordEncoderSupportsLegacyIds() {
        assertThat(ContextUtils.createDelegatingPasswordEncoder("noop").encode("x")).isEqualTo("{noop}x");
        assertThat(ContextUtils.createDelegatingPasswordEncoder("MD5").encode("x")).startsWith("{MD5}");
        assertThat(ContextUtils.createDelegatingPasswordEncoder("sha256").encode("x")).startsWith("{sha256}");
    }

    @Test
    void afterPropertiesSetInitializesStaticFields() {
        JsonMapper mapper = JsonMapper.builder().build();
        CacheManager cacheManager = mock(CacheManager.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);

        new ContextUtils(mapper, cacheManager, publisher).afterPropertiesSet();

        assertThat(ContextUtils.OBJECT_MAPPER).isSameAs(mapper);
        assertThat(ContextUtils.CACHE_MANAGER).isSameAs(cacheManager);
        assertThat(ContextUtils.APPLICATION_EVENT_PUBLISHER).isSameAs(publisher);
    }

    @Test
    void securityDetailsIsEmptyWhenAuthenticationIsNull() {
        Authentication auth = null;

        SecurityDetails result = ContextUtils.securityDetails()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        Mono.just(new SecurityContextImpl(auth))))
                .block();

        assertThat(result).isNull();
    }

    @Test
    void securityDetailsIsEmptyWithoutContext() {
        assertThat(ContextUtils.securityDetails().block()).isNull();
    }

    @Test
    void securityDetailsIsEmptyWhenPrincipalIsNotSecurityDetails() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn("not-an-auditor");

        SecurityDetails result = ContextUtils.securityDetails()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
                .block();

        assertThat(result).isNull();
    }

    @Test
    void securityDetailsReturnsPrincipalWhenItIsSecurityDetails() {
        SecurityDetails details = new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
        details.setCode(UUID.randomUUID());

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(details);

        SecurityDetails result = ContextUtils.securityDetails()
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                        reactor.core.publisher.Mono.just(new SecurityContextImpl(auth))))
                .block();

        assertThat(result).isSameAs(details);
    }

    @Test
    void eventPublisherPublishesThroughStaticPublisher() {
        var publisher = mock(org.springframework.context.ApplicationEventPublisher.class);
        var saved = ContextUtils.APPLICATION_EVENT_PUBLISHER;
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
        try {
            TestEvent event = new TestEvent("entity");
            ContextUtils.eventPublisher(event);

            verify(publisher).publishEvent(event);
        } finally {
            ContextUtils.APPLICATION_EVENT_PUBLISHER = saved;
        }
    }

    static class TestEvent extends AbstractEvent<String> {
        TestEvent(String entity) {
            super(entity, Kind.INSERT);
        }
    }
}
