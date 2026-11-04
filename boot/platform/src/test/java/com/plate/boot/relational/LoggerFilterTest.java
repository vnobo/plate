package com.plate.boot.relational;

import com.plate.boot.commons.exception.JsonException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.config.HttpCodecsProperties;
import com.plate.boot.relational.logger.LoggerEvent;
import com.plate.boot.relational.logger.LoggerReq;
import com.plate.boot.security.SecurityDetails;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.unit.DataSize;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static com.plate.boot.relational.LoggerFilter.CACHED_REQUEST_BODY_ATTR;
import static com.plate.boot.relational.LoggerFilter.CACHED_RESPONSE_BODY_ATTR;
import static com.plate.boot.relational.LoggerFilter.CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR;
import static com.plate.boot.relational.LoggerFilter.CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggerFilter}, its anonymous request/response decorators, and the
 * request/response body caching, parsing, and DataBuffer release logic.
 */
class LoggerFilterTest {

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

    @Test
    void constructorDefaultsMemorySizeWhenUnset() {
        HttpCodecsProperties properties = new HttpCodecsProperties();

        LoggerFilter filter = new LoggerFilter(properties);

        assertThat(filter).isNotNull();
    }

    @Test
    void constructorUsesConfiguredMemorySize() {
        HttpCodecsProperties properties = new HttpCodecsProperties();
        properties.setMaxInMemorySize(DataSize.ofKilobytes(5));

        LoggerFilter filter = new LoggerFilter(properties);

        assertThat(filter).isNotNull();
    }

    @Test
    void filterContinuesChainForSafeMethodWithoutLogging() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/x").build());
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        verify(chain).filter(any());
        Object requestDecorator = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        assertThat(requestDecorator).isNull();
    }

    @Test
    void filterLogsRequestAndResponseForNonSafeMethod() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/x")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Custom", "value")
                .queryParam("a", "b")
                .cookie(new HttpCookie("SESSION", "session-1"))
                .body("{\"foo\":\"bar\"}");
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        SecurityDetails details = securityDetails();
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ApplicationEventPublisher savedPublisher = ContextUtils.APPLICATION_EVENT_PUBLISHER;
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
        try {
            Mono<Void> result = filter.filter(exchange, chain)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(new SecurityContextImpl(authentication(details)))));

            StepVerifier.create(result).verifyComplete();

            ArgumentCaptor<LoggerEvent> captor = ArgumentCaptor.forClass(LoggerEvent.class);
            verify(publisher).publishEvent(captor.capture());
            LoggerReq logged = captor.getValue().getEntity();
            assertThat(logged.getMethod()).isEqualTo(MethodType.POST);
            assertThat(logged.getStatus()).isEqualTo("200");
            assertThat(logged.getUrl()).isEqualTo("/api/x");
        } finally {
            ContextUtils.APPLICATION_EVENT_PUBLISHER = savedPublisher;
        }
    }

    @Test
    void filterReportsUnknownStatusWhenResponseStatusIsNull() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/y").contentType(MediaType.APPLICATION_JSON).body("{\"a\":1}"));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        SecurityDetails details = securityDetails();
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ApplicationEventPublisher savedPublisher = ContextUtils.APPLICATION_EVENT_PUBLISHER;
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
        try {
            Mono<Void> result = filter.filter(exchange, chain)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(new SecurityContextImpl(authentication(details)))));

            StepVerifier.create(result).verifyComplete();

            ArgumentCaptor<LoggerEvent> captor = ArgumentCaptor.forClass(LoggerEvent.class);
            verify(publisher).publishEvent(captor.capture());
            assertThat(captor.getValue().getEntity().getStatus()).isEqualTo("Unknown");
        } finally {
            ContextUtils.APPLICATION_EVENT_PUBLISHER = savedPublisher;
        }
    }

    @Test
    void filterCachesResponseBodyWhenChainWrites() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/z").contentType(MediaType.APPLICATION_JSON).body("{\"a\":1}"));
        exchange.getResponse().setStatusCode(HttpStatus.CREATED);
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenAnswer(invocation -> {
            ServerWebExchange mutated = invocation.getArgument(0);
            ServerHttpResponse response = mutated.getResponse();
            DataBuffer buffer = response.bufferFactory().wrap("{\"result\":\"ok\"}".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        });

        SecurityDetails details = securityDetails();
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ApplicationEventPublisher savedPublisher = ContextUtils.APPLICATION_EVENT_PUBLISHER;
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
        try {
            Mono<Void> result = filter.filter(exchange, chain)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(
                            Mono.just(new SecurityContextImpl(authentication(details)))));

            StepVerifier.create(result).verifyComplete();

            ArgumentCaptor<LoggerEvent> captor = ArgumentCaptor.forClass(LoggerEvent.class);
            verify(publisher).publishEvent(captor.capture());
            LoggerReq logged = captor.getValue().getEntity();
            assertThat(logged.getStatus()).isEqualTo("201");
            assertThat(logged.getContext().get("responseBody").get("result").asString()).isEqualTo("ok");
        } finally {
            ContextUtils.APPLICATION_EVENT_PUBLISHER = savedPublisher;
        }
    }

    @Test
    void cacheRequestBodyCachesNonEmptyBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/x").contentType(MediaType.APPLICATION_JSON).body("{\"a\":1}"));
        AtomicReference<ServerHttpRequest> captured = new AtomicReference<>();

        Mono<String> result = LoggerFilter.cacheRequestBody(exchange, request -> {
            captured.set(request);
            return Mono.just("done");
        });

        StepVerifier.create(result).expectNext("done").verifyComplete();
        assertThat(captured.get()).isNotNull();
        Object requestBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        assertThat(requestBody).isInstanceOf(DataBuffer.class);
    }

    @Test
    void cacheRequestBodyHandlesEmptyBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/x"));
        AtomicReference<ServerHttpRequest> captured = new AtomicReference<>();

        Mono<String> result = LoggerFilter.cacheRequestBody(exchange, request -> {
            captured.set(request);
            return Mono.just("done");
        });

        StepVerifier.create(result).expectNext("done").verifyComplete();
        assertThat(captured.get()).isNotNull();
    }

    @Test
    void responseDecorateStoresBodyAndDecoratorAttributes() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));

        ServerHttpResponse decorator = LoggerFilter.responseDecorate(exchange);

        assertThat(decorator).isNotNull();
        Object responseBody = exchange.getAttribute(CACHED_RESPONSE_BODY_ATTR);
        assertThat(responseBody).isInstanceOf(DataBuffer.class);
        Object responseDecorator = exchange.getAttribute(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);
        assertThat(responseDecorator).isSameAs(decorator);
    }

    @Test
    void buildDataBufferHandlesDefaultDataBuffer() {
        DataBuffer input = new DefaultDataBufferFactory().wrap("hello".getBytes(StandardCharsets.UTF_8));

        DataBuffer result = (DataBuffer) invokePrivate(null, "buildDataBuffer",
                new Class<?>[]{DataBuffer.class}, input);

        assertThat(result).isInstanceOf(DefaultDataBuffer.class);
    }

    @Test
    void buildDataBufferHandlesNettyDataBuffer() {
        DataBuffer input = new NettyDataBufferFactory(UnpooledByteBufAllocator.DEFAULT)
                .wrap("hello".getBytes(StandardCharsets.UTF_8));

        DataBuffer result = (DataBuffer) invokePrivate(null, "buildDataBuffer",
                new Class<?>[]{DataBuffer.class}, input);

        assertThat(result).isInstanceOf(NettyDataBuffer.class);
    }

    @Test
    void buildDataBufferThrowsForUnknownBufferType() {
        DataBuffer input = mock(DataBuffer.class);

        assertThatThrownBy(() -> invokePrivate(null, "buildDataBuffer",
                new Class<?>[]{DataBuffer.class}, input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requestDecorateCachesNonEmptyBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("body".getBytes(StandardCharsets.UTF_8));

        invokePrivate(filter, "requestDecorate",
                new Class<?>[]{ServerWebExchange.class, DataBuffer.class}, exchange, buffer);

        Object requestBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        assertThat(requestBody).isSameAs(buffer);
        Object requestDecorator = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        assertThat(requestDecorator).isNotNull();
    }

    @Test
    void requestDecorateKeepsExistingCachedBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer existing = new DefaultDataBufferFactory().wrap("old".getBytes(StandardCharsets.UTF_8));
        exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, existing);
        DataBuffer buffer = new DefaultDataBufferFactory().wrap("new".getBytes(StandardCharsets.UTF_8));

        invokePrivate(filter, "requestDecorate",
                new Class<?>[]{ServerWebExchange.class, DataBuffer.class}, exchange, buffer);

        Object requestBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        assertThat(requestBody).isSameAs(existing);
    }

    @Test
    void requestDecorateSkipsEmptyBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer empty = new DefaultDataBufferFactory().wrap(new byte[0]);

        invokePrivate(filter, "requestDecorate",
                new Class<?>[]{ServerWebExchange.class, DataBuffer.class}, exchange, empty);

        Object requestBody = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
        assertThat(requestBody).isNull();
    }

    @Test
    void readRequestBodyThrowsJsonExceptionForInvalidBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer bad = new DefaultDataBufferFactory().wrap("not-json".getBytes(StandardCharsets.UTF_8));
        exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, bad);

        assertThatThrownBy(() -> invokePrivate(filter, "readRequestBody",
                new Class<?>[]{ServerWebExchange.class}, exchange))
                .isInstanceOf(JsonException.class);
    }

    @Test
    void readResponseBodyThrowsJsonExceptionForInvalidBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer bad = new DefaultDataBufferFactory().wrap("not-json".getBytes(StandardCharsets.UTF_8));
        exchange.getAttributes().put(CACHED_RESPONSE_BODY_ATTR, bad);

        assertThatThrownBy(() -> invokePrivate(filter, "readResponseBody",
                new Class<?>[]{ServerWebExchange.class}, exchange))
                .isInstanceOf(JsonException.class);
    }

    @Test
    void readRequestBodyParsesValidBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer body = new DefaultDataBufferFactory().wrap("{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8));
        exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, body);

        JsonNode result = (JsonNode) invokePrivate(filter, "readRequestBody",
                new Class<?>[]{ServerWebExchange.class}, exchange);

        assertThat(result.get("foo").asString()).isEqualTo("bar");
    }

    @Test
    void releaseDataBufferIgnoresNonDataBufferObjects() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());

        invokePrivate(filter, "releaseDataBuffer", new Class<?>[]{Object.class}, "not-a-buffer");
    }

    @Test
    void releaseDataBufferSwallowsReleaseFailure() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        PooledDataBuffer pooled = mock(PooledDataBuffer.class);
        when(pooled.release()).thenThrow(new RuntimeException("release failed"));

        invokePrivate(filter, "releaseDataBuffer", new Class<?>[]{Object.class}, pooled);
    }

    @Test
    void processFilterReturnsEmptyWhenCachedDecoratorsMissing() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/x"));
        WebFilterChain chain = mock(WebFilterChain.class);

        Mono<Void> result = (Mono<Void>) invokePrivate(filter, "processFilter",
                new Class<?>[]{ServerWebExchange.class, WebFilterChain.class}, exchange, chain);

        StepVerifier.create(result).verifyComplete();
        verify(chain, never()).filter(any());
    }

    @Test
    void readResponseBodyParsesValidBody() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x"));
        DataBuffer body = new DefaultDataBufferFactory().wrap("{\"foo\":\"bar\"}".getBytes(StandardCharsets.UTF_8));
        exchange.getAttributes().put(CACHED_RESPONSE_BODY_ATTR, body);

        JsonNode result = (JsonNode) invokePrivate(filter, "readResponseBody",
                new Class<?>[]{ServerWebExchange.class}, exchange);

        assertThat(result.get("foo").asString()).isEqualTo("bar");
    }

    @Test
    void filterCompletesWithoutPublishingWhenNoSecurityContext() {
        LoggerFilter filter = new LoggerFilter(new HttpCodecsProperties());
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/x").contentType(MediaType.APPLICATION_JSON).body("{\"a\":1}"));
        WebFilterChain chain = mock(WebFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        ApplicationEventPublisher savedPublisher = ContextUtils.APPLICATION_EVENT_PUBLISHER;
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
        try {
            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

            verify(publisher, never()).publishEvent(any());
        } finally {
            ContextUtils.APPLICATION_EVENT_PUBLISHER = savedPublisher;
        }
    }

    private SecurityDetails securityDetails() {
        SecurityDetails details = new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
        details.setCode(UUID.randomUUID());
        details.setUsername("admin");
        return details;
    }

    private Authentication authentication(SecurityDetails details) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);
        return authentication;
    }

    private Object invokePrivate(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method method = LoggerFilter.class.getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
