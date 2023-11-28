package com.platform.boot.relational;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.platform.boot.commons.exception.JsonException;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.relational.logger.LoggerRequest;
import com.platform.boot.relational.logger.LoggersService;
import com.platform.boot.security.SecurityDetails;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.EmptyArrays;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.*;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.platform.boot.commons.utils.ContextUtils.getClientIpAddress;
import static org.springframework.security.web.server.csrf.CsrfWebFilter.DEFAULT_CSRF_MATCHER;

/**
 * This code is a web filter that logs requests and responses. It checks if the content type is JSON, and if it is, it caches the request body, logs the request, and then continues the filter chain. It has a getOrder() method which returns the lowest precedence of the filter. It also has a requireCsrfProtectionMatcher which is used to check if the request needs protection from CSRF attacks.
 * Step by step explanation:
 * 1. The code starts off by declaring the package and importing necessary classes.
 * 2. It then declares the class LoggerFilter which implements WebFilter and Ordered.
 * 3. It declares a ServerWebExchangeMatcher called requireCsrfProtectionMatcher which is used to check if the request needs protection from CSRF attacks.
 * 4. It also declares a LoggersService object which is used to log the request.
 * 5. The getOrder() method is then declared which returns the lowest precedence of the filter.
 * 6. The filter() method is then declared which checks if the content type is JSON, and if it is, it caches the request body, logs the request, and then continues the filter chain.
 * 7. The continueFilterChain() method is then declared which continues the filter chain.
 * 8. The cacheFilterChain() method is then declared which caches the request body.
 * 9. The logRequest() method is then declared which logs the request.
 * 10. The readRequestBody() method is then declared which reads the request body.
 * 11. The validContentTypeIsJson() method is then declared which checks if the content type is JSON.
 *
 * @author Alex bob(<a href="https://github.com/vnobo">https://github.com/vnobo</a>)
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1024)
public class LoggerFilter implements WebFilter {
    private static final Log log = LogFactory.getLog(LoggerFilter.class);

    public static final String CACHED_REQUEST_BODY_ATTR = "cachedRequestBody";
    public static final String CACHED_RESPONSE_BODY_ATTR = "cachedResponseBody";
    public static final String CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR = "cachedServerHttpRequestDecorator";
    public static final String CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR = "cachedServerHttpResponseDecorator";

    /**
     * Matcher used to check if CSRF protection is required
     */
    private final ServerWebExchangeMatcher requireCsrfProtectionMatcher = DEFAULT_CSRF_MATCHER;
    /**
     * Service used to log requests
     */
    private final LoggersService loggerService;

    public static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange,
                                               Function<ServerHttpRequest, Mono<T>> function) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory factory = response.bufferFactory();
        // Join all the DataBuffers so we have a single DataBuffer for the body
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(factory.wrap(EmptyArrays.EMPTY_BYTES))
                .flatMap(dataBuffer -> Mono.just(requestDecorate(exchange, dataBuffer)))
                .flatMap(function);
    }

    public static ServerHttpResponse responseDecorate(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        var factory = response.bufferFactory();
        var dataBuffer = factory.wrap("{\"message\":\"No response body available!\"}".getBytes());
        // Don't cache if body is already cached
        exchange.getAttributes().put(CACHED_RESPONSE_BODY_ATTR, dataBuffer);
        exchange.getAttributes().put("cachedOriginalResponseBodyBackup", dataBuffer);

        ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public @NotNull Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                var cachedDataBuffer = DataBufferUtils.join(body).doOnNext(dataBuffer -> {
                            Object previousCachedBody = exchange.getAttributes().put(CACHED_RESPONSE_BODY_ATTR, dataBuffer);
                            if (previousCachedBody != null) {
                                exchange.getAttributes().put("cachedOriginalResponseBodyBackup", previousCachedBody);
                            }
                        })
                        .flatMap(dataBuffer -> Mono.fromSupplier(() -> buildDataBuffer(dataBuffer)));
                return super.writeWith(cachedDataBuffer);
            }
        };
        exchange.getAttributes().put(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR, decorator);
        return decorator;
    }

    /**
     * Decorates the given {@code ServerHttpRequest} with cached request body data buffer.
     *
     * @param exchange   the server web exchange
     * @param dataBuffer the data buffer to cache
     * @return the decorated server http request
     */
    private static ServerHttpRequest requestDecorate(ServerWebExchange exchange, DataBuffer dataBuffer) {
        if (dataBuffer.readableByteCount() > 0) {
            if (log.isTraceEnabled()) {
                log.trace("Retaining body in exchange attribute");
            }
            var cachedDataBuffer = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
            // Don't cache if body is already cached
            if (!(cachedDataBuffer instanceof DataBuffer)) {
                exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, dataBuffer);
            }
            exchange.getAttributes().put("cachedOriginalRequestBodyBackup", dataBuffer);
        }
        var decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public @NotNull Flux<DataBuffer> getBody() {
                return Mono.fromSupplier(() -> {
                    if (exchange.getAttribute(CACHED_REQUEST_BODY_ATTR) == null) {
                        // Probably downstream closed or no body
                        return null;
                    }
                    return buildDataBuffer(dataBuffer);
                }).flux();
            }
        };
        exchange.getAttributes().put(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR, decorator);
        return decorator;
    }

    /**
     * Continue the filter chain with the given exchange and chain.
     *
     * @param exchange The exchange
     * @param chain    The chain
     * @return A {@link Mono} of void
     */
    private Mono<Void> continueFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("%sLogger filter chain next.".formatted(exchange.getLogPrefix()));
        return Mono.defer(() -> chain.filter(exchange));
    }

    private JsonNode readRequestBody(ServerWebExchange exchange) {
        String bodyStr = exchange.getRequiredAttribute(CACHED_REQUEST_BODY_ATTR);
        try {
            return ContextUtils.OBJECT_MAPPER.readTree(bodyStr);
        } catch (IOException exception) {
            throw JsonException.withError(exception);
        }
    }

    /**
     * This method builds a DataBuffer from the given input.
     * It checks the type of the input DataBuffer and constructs
     * a new DataBuffer based on the type of the input.
     * If the input DataBuffer is a NettyDataBuffer,
     * a new NettyDataBuffer is wrapped around the retained slice
     * of the given input NettyDataBuffer.
     * If the given DataBuffer is a DefaultDataBuffer,
     * a new DataBuffer generated from Unpoo
     * led.wrappedBuffer
     * is returned.
     */
    private static DataBuffer buildDataBuffer(DataBuffer dataBuffer) {
        if (dataBuffer instanceof NettyDataBuffer pdb) {
            return pdb.factory().wrap(pdb.getNativeBuffer().retainedSlice());
        } else if (dataBuffer instanceof DefaultDataBuffer ddf) {
            return ddf.factory().wrap(Unpooled.wrappedBuffer(ddf.getNativeBuffer()).nioBuffer());
        } else {
            throw new IllegalArgumentException(
                    "Unable to handle DataBuffer of type " + dataBuffer.getClass());
        }
    }

    private boolean validContentTypeIsJson(ServerWebExchange exchange) {
        return MediaType.APPLICATION_JSON.equalsTypeAndSubtype(exchange.getRequest().getHeaders().getContentType());
    }

    @Override
    public @NotNull Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var nextMono = cacheFilterChain(exchange, chain).then(Mono.defer(ContextUtils::securityDetails)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(userDetails -> logRequest(exchange, userDetails)).then());
        if (validContentTypeIsJson(exchange)) {
            return this.requireCsrfProtectionMatcher.matches(exchange)
                    .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                    .switchIfEmpty(Mono.defer(() -> continueFilterChain(exchange, chain).then(Mono.empty())))
                    .flatMap((m) -> nextMono);
        }
        return continueFilterChain(exchange, chain).then(Mono.empty());
    }

    /**
     * Cache the filter chain with the given exchange and chain.
     *
     * @param exchange The exchange
     * @param chain    The chain
     * @return A {@link Mono} of void
     */
    private Mono<Void> cacheFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        return cacheRequestBody(exchange, serverHttpRequest -> {
            ServerHttpResponse cachedResponse = responseDecorate(exchange);
            ServerRequest serverRequest = ServerRequest.create(exchange.mutate()
                            .request(serverHttpRequest).response(cachedResponse).build(),
                    HandlerStrategies.withDefaults().messageReaders());
            return serverRequest.bodyToMono(String.class).doOnNext((objectValue) -> {
                Object previousCachedBody = exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, objectValue);
                log.debug("%sCache request body: %s".formatted(exchange.getLogPrefix(), previousCachedBody));
            });
        }).then(Mono.defer(() -> {
            ServerHttpRequest cachedRequest = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
            ServerHttpResponse cachedResponse = exchange.getAttribute(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);
            Assert.notNull(cachedRequest, "cache request shouldn't be null");
            Assert.notNull(cachedResponse, "cache Response shouldn't be null");
            exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
            exchange.getAttributes().remove(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);
            return chain.filter(exchange.mutate().request(cachedRequest).response(cachedResponse).build())
                    .doFinally((s) -> {
                        Object backupCachedBody = exchange.getAttributes().get("cachedOriginalRequestBodyBackup");
                        Object backupCachedBody1 = exchange.getAttributes().get("cachedOriginalResponseBodyBackup");
                        if (backupCachedBody instanceof DataBuffer dataBuffer) {
                            DataBufferUtils.release(dataBuffer);
                        }
                        if (backupCachedBody1 instanceof DataBuffer dataBuffer) {
                            DataBufferUtils.release(dataBuffer);
                        }
                    });
        }));
    }

    private void logRequest(ServerWebExchange exchange, SecurityDetails userDetails) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        ObjectNode contentNode = ContextUtils.OBJECT_MAPPER.createObjectNode();
        contentNode.putPOJO("requestHeaders", request.getHeaders());
        contentNode.putPOJO("requestAddress", getClientIpAddress(request));
        contentNode.putPOJO("requestCookies", request.getCookies());
        contentNode.putPOJO("requestQueryParams", request.getQueryParams());
        contentNode.set("requestBody", readRequestBody(exchange));

        contentNode.putPOJO("responseHeaders", response.getHeaders());
        contentNode.putPOJO("responseStatusCode", response.getStatusCode());
        contentNode.set("responseBody", readResponseBody(exchange));

        String prefix = exchange.getLogPrefix();
        String method = request.getMethod().name();
        String status = String.valueOf(Objects.requireNonNull(response.getStatusCode()).value());
        String tenantCode = Optional.ofNullable(userDetails.getTenantCode()).orElse("0");
        String path = request.getPath().value();

        LoggerRequest logger = LoggerRequest.of(tenantCode, userDetails.getUsername(), prefix,
                method, status, path, contentNode);
        this.loggerService.operate(logger).share()
                .subscribe(res -> log.debug("%s**操作日志**: %s : MessageBody: %s"
                        .formatted(exchange.getLogPrefix(), exchange.getRequest().getMethod().name(), res)));
    }

    private JsonNode readResponseBody(ServerWebExchange exchange) {
        DataBuffer dataBuffer = exchange.getRequiredAttribute(CACHED_RESPONSE_BODY_ATTR);
        if (dataBuffer.readableByteCount() == 0) {
            return ContextUtils.OBJECT_MAPPER.createObjectNode();
        }
        try (var byteBufferIterator = dataBuffer.readableByteBuffers()) {
            StringBuilder bodyStr = new StringBuilder();
            while (byteBufferIterator.hasNext()) {
                ByteBuffer byteBuffer = byteBufferIterator.next();
                bodyStr.append(CharsetUtil.UTF_8.decode(byteBuffer));
            }
            return ContextUtils.OBJECT_MAPPER.readTree(bodyStr.toString());
        } catch (IOException exception) {
            throw JsonException.withError(exception);
        }
    }

}