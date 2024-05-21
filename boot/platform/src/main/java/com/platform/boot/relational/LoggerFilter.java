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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
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
 * @author Alex bob(<a href="https://github.com/vnobo">https://github.com/vnobo</a>)
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class LoggerFilter implements WebFilter {

    public static final String CACHED_REQUEST_BODY_ATTR = "cachedRequestBody";
    public static final String CACHED_RESPONSE_BODY_ATTR = "cachedResponseBody";
    public static final String CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR = "cachedServerHttpRequestDecorator";
    public static final String CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR = "cachedServerHttpResponseDecorator";

    private static final ServerWebExchangeMatcher REQUIRE_CSRF_PROTECTION_MATCHER = DEFAULT_CSRF_MATCHER;

    private final LoggersService loggerService;

    public static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange,
                                               Function<ServerHttpRequest, Mono<T>> function) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory factory = response.bufferFactory();
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .defaultIfEmpty(factory.wrap(EmptyArrays.EMPTY_BYTES))
                .flatMap(dataBuffer -> Mono.just(requestDecorate(exchange, dataBuffer)))
                .flatMap(function);
    }

    public static ServerHttpResponse responseDecorate(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        var factory = response.bufferFactory();
        var dataBuffer = factory.wrap("{\"message\":\"No response body available!\"}".getBytes());
        exchange.getAttributes().put(CACHED_RESPONSE_BODY_ATTR, dataBuffer);
        exchange.getAttributes().put("cachedOriginalResponseBodyBackup", dataBuffer);

        ServerHttpResponseDecorator decorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public @NonNull Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
                var cachedDataBuffer = DataBufferUtils.join(body).doOnNext(dataBuffer -> {
                    Object previousCachedBody = exchange.getAttributes().put(CACHED_RESPONSE_BODY_ATTR, dataBuffer);
                    if (previousCachedBody != null) {
                        exchange.getAttributes().put("cachedOriginalResponseBodyBackup", previousCachedBody);
                    }
                }).flatMap(dataBuffer -> Mono.fromSupplier(() -> buildDataBuffer(dataBuffer)));
                return super.writeWith(cachedDataBuffer);
            }
        };
        exchange.getAttributes().put(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR, decorator);
        return decorator;
    }

    private static ServerHttpRequest requestDecorate(ServerWebExchange exchange, DataBuffer dataBuffer) {
        if (dataBuffer.capacity() > 0) {
            if (log.isTraceEnabled()) {
                log.trace("Retaining body in exchange attribute");
            }
            var cachedDataBuffer = exchange.getAttribute(CACHED_REQUEST_BODY_ATTR);
            if (!(cachedDataBuffer instanceof DataBuffer)) {
                exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, dataBuffer);
            }
            exchange.getAttributes().put("cachedOriginalRequestBodyBackup", dataBuffer);
        }
        var decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public @NonNull Flux<DataBuffer> getBody() {
                return Mono.fromSupplier(() -> {
                    if (exchange.getAttribute(CACHED_REQUEST_BODY_ATTR) == null) {
                        return null;
                    }
                    return buildDataBuffer(dataBuffer);
                }).flux();
            }
        };
        exchange.getAttributes().put(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR, decorator);
        return decorator;
    }

    private JsonNode readRequestBody(ServerWebExchange exchange) {
        String bodyStr = exchange.getRequiredAttribute(CACHED_REQUEST_BODY_ATTR);
        try {
            return ContextUtils.OBJECT_MAPPER.readTree(bodyStr);
        } catch (IOException exception) {
            throw JsonException.withError(exception);
        }
    }

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

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var nextMono = cacheFilterChain(exchange, chain).then(Mono.defer(ContextUtils::securityDetails)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(userDetails -> logRequest(exchange, userDetails)).then());
        return REQUIRE_CSRF_PROTECTION_MATCHER.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(Mono.defer(() -> continueFilterChain(exchange, chain).then(Mono.empty())))
                .flatMap((m) -> nextMono);
    }

    private Mono<Void> continueFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("{}Logger filter chain continue next.", exchange.getLogPrefix());
        return Mono.defer(() -> chain.filter(exchange));
    }

    private Mono<Void> cacheFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        return cacheRequestBody(exchange, serverHttpRequest -> processRequestBody(exchange, serverHttpRequest))
                .then(Mono.defer(() -> processFilter(exchange, chain)));
    }

    private Mono<String> processRequestBody(ServerWebExchange exchange, ServerHttpRequest serverHttpRequest) {
        ServerHttpResponse cachedResponse = responseDecorate(exchange);
        ServerRequest serverRequest = ServerRequest.create(exchange.mutate()
                        .request(serverHttpRequest).response(cachedResponse).build(),
                HandlerStrategies.withDefaults().messageReaders());
        return serverRequest.bodyToMono(String.class).doOnNext((objectValue) -> {
            Object previousCachedBody = exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, objectValue);
            log.debug("{}Logger filter cache request body: {}", exchange.getLogPrefix(), previousCachedBody);
        });
    }

    private Mono<Void> processFilter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest cachedRequest = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        ServerHttpResponse cachedResponse = exchange.getAttribute(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);

        if (cachedRequest == null || cachedResponse == null) {
            log.error("{}Logger filter cached request or response is null. Abort processing.",
                    exchange.getLogPrefix());
            return Mono.empty();
        }

        exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        exchange.getAttributes().remove(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);

        return chain.filter(exchange.mutate().request(cachedRequest).response(cachedResponse).build())
                .doFinally(s -> releaseResources(exchange));
    }

    private void releaseResources(ServerWebExchange exchange) {
        Object backupCachedBody = exchange.getAttributes().get("cachedOriginalRequestBodyBackup");
        Object backupCachedBody1 = exchange.getAttributes().get("cachedOriginalResponseBodyBackup");

        releaseDataBuffer(backupCachedBody);
        releaseDataBuffer(backupCachedBody1);
    }

    private void releaseDataBuffer(Object dataBufferObject) {
        if (dataBufferObject instanceof DataBuffer dataBuffer) {
            try {
                DataBufferUtils.release(dataBuffer);
            } catch (Exception e) {
                log.error("Logger filter failed to release DataBuffer resource.", e);
            }
        }
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
        this.loggerService.operate(logger).share().subscribe(res ->
                log.debug("{}**操作日志**: {},MessageBody: {}",
                        exchange.getLogPrefix(), exchange.getRequest().getMethod().name(), res));
    }

    private JsonNode readResponseBody(ServerWebExchange exchange) {
        DataBuffer dataBuffer = exchange.getRequiredAttribute(CACHED_RESPONSE_BODY_ATTR);
        if (dataBuffer.capacity() < 2) {
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