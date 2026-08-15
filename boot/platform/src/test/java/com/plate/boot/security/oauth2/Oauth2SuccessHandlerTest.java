package com.plate.boot.security.oauth2;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.plate.boot.config.SessionConfiguration.X_REQUESTED_WITH;
import static com.plate.boot.config.SessionConfiguration.XML_HTTP_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Oauth2SuccessHandler} (no Spring / servlet container required).
 * <p>
 * The three {@code onAuthenticationSuccess} branches are exercised: reject a non-OAuth2
 * authentication, write a JSON token for XHR requests, and delegate to the superclass
 * redirect for non-XHR requests. JSON output uses the shared {@link ContextUtils#OBJECT_MAPPER}.
 */
@ExtendWith(MockitoExtension.class)
class Oauth2SuccessHandlerTest {

    private final Oauth2SuccessHandler handler = new Oauth2SuccessHandler();

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

    private OAuth2AuthenticationToken oauth2Token() {
        OAuth2User principal = new DefaultOAuth2User(List.of(), Map.of("sub", "x"), "sub");
        return new OAuth2AuthenticationToken(principal, List.of(), "github");
    }

    @Test
    void rejectsNonOauth2Authentication() {
        WebFilterExchange webFilterExchange = mock(WebFilterExchange.class);
        Authentication authentication = mock(Authentication.class);

        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, authentication))
                .expectErrorSatisfies(error -> assertThat(error).isInstanceOf(RestServerException.class))
                .verify();
    }

    @Test
    void handlesXmlHttpRequestByWritingToken() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set(X_REQUESTED_WITH, XML_HTTP_REQUEST);
        when(request.getHeaders()).thenReturn(requestHeaders);

        ServerHttpResponse response = mock(ServerHttpResponse.class);
        HttpHeaders responseHeaders = new HttpHeaders();
        when(response.getHeaders()).thenReturn(responseHeaders);

        DataBufferFactory bufferFactory = mock(DataBufferFactory.class);
        DataBuffer buffer = mock(DataBuffer.class);
        when(bufferFactory.wrap(any(byte[].class))).thenReturn(buffer);
        when(response.bufferFactory()).thenReturn(bufferFactory);
        when(response.writeAndFlushWith(any())).thenReturn(Mono.empty());

        WebSession session = mock(WebSession.class);
        when(session.getId()).thenReturn("session-123");
        when(session.getLastAccessTime()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));
        when(session.getMaxIdleTime()).thenReturn(Duration.ofSeconds(1800));

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(exchange.getSession()).thenReturn(Mono.just(session));

        WebFilterExchange webFilterExchange = mock(WebFilterExchange.class);
        when(webFilterExchange.getExchange()).thenReturn(exchange);

        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, oauth2Token()))
                .verifyComplete();

        assertThat(responseHeaders.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        verify(response).setStatusCode(HttpStatus.OK);
        verify(response).writeAndFlushWith(any());
    }

    @Test
    void delegatesToSuperWhenRequestedWithHeaderAbsent() {
        handler.setRedirectStrategy((exchange, location) -> Mono.empty());

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getSession()).thenReturn(Mono.empty());

        WebFilterExchange webFilterExchange = mock(WebFilterExchange.class);
        when(webFilterExchange.getExchange()).thenReturn(exchange);

        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, oauth2Token()))
                .verifyComplete();
    }

    @Test
    void delegatesToSuperWhenRequestedWithIsNotXmlHttpRequest() {
        handler.setRedirectStrategy((exchange, location) -> Mono.empty());

        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set(X_REQUESTED_WITH, "fetch");
        when(request.getHeaders()).thenReturn(requestHeaders);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getSession()).thenReturn(Mono.empty());

        WebFilterExchange webFilterExchange = mock(WebFilterExchange.class);
        when(webFilterExchange.getExchange()).thenReturn(exchange);

        StepVerifier.create(handler.onAuthenticationSuccess(webFilterExchange, oauth2Token()))
                .verifyComplete();
    }
}
