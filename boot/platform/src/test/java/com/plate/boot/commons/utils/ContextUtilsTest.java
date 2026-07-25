package com.plate.boot.commons.utils;

import com.plate.boot.commons.base.AbstractEvent;
import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

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
