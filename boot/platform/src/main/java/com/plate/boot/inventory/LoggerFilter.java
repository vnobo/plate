package com.plate.boot.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.plate.boot.commons.exception.JsonException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.inventory.relational.logger.LoggerReq;
import com.plate.boot.inventory.relational.logger.LoggersService;
import com.plate.boot.security.SecurityDetails;
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.plate.boot.commons.utils.ContextUtils.getClientIpAddress;
import static org.springframework.security.web.server.csrf.CsrfWebFilter.DEFAULT_CSRF_MATCHER;

/**
 * LoggerFilter is a WebFilter designed to intercept and log HTTP request and response details.
 * It caches the request and response bodies, decorates the requests and responses for logging purposes,
 * and utilizes a LoggersService to persist operation logs with enriched content.
 * The filter matches requests based on a predefined matcher (defaultLoggerMatcher) and can be bypassed
 * if the match is not successful.
 * <p>
 * Key Features:
 * - Caches request and response bodies to ensure they can be logged even after consumption.
 * - Matches requests to determine if logging should occur based on a ServerWebExchangeMatcher.
 * - Processes and logs the HTTP method, status, path, headers, cookies, from parameters, and bodies.
 * - Handles DataBuffer retention and release to prevent memory leaks.
 * - Utilizes a separate service (LoggersService) to handle the logging operation asynchronously.
 * - Supports tracing-level logging for debugging filter operations.
 * <p>
 * Dependencies:
 * - ServerWebExchangeMatcher for matching requests.
 * - LoggersService for persisting log entries.
 * - SLF4J Logger (log) for internal logging.
 * <p>
 * Usage:
 * Implemented as a Spring component (@Component), it's automatically registered in the WebFilter chain.
 * Configuration may be required to customize the matching behavior or logging service interaction.
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class LoggerFilter implements WebFilter {

    /**
     * Constants for the attribute key used to cache request body information.
     * This string represents the attribute name under which the cached request body can be stored or retrieved in a context where attributes are managed.
     */
    public static final String CACHED_REQUEST_BODY_ATTR = "cachedRequestBody";
    /**
     * Constant defining an attribute key for storing the cached response body.
     * This can be used in scenarios where the response body needs to be temporarily stored,
     * such as during request handling or caching purposes within an application.
     */
    public static final String CACHED_RESPONSE_BODY_ATTR = "cachedResponseBody";
    /**
     * Constants for attribute key used to store cached ServerHttpRequest decorator.
     * This string represents the attribute name under which a decorated ServerHttpRequest
     * can be found within the attribute map of a request processing context.
     */
    public static final String CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR = "cachedServerHttpRequestDecorator";
    /**
     * Constant defining an attribute key for the cached server HTTP response decorator.
     * This attribute can be used to store or retrieve a decorated server HTTP response in a context where
     * caching mechanisms or decorators are applied to server responses.
     */
    public static final String CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR = "cachedServerHttpResponseDecorator";

    /**
     * The default logger matcher instance used to match server web exchange requests.
     * This matcher is initialized with the DEFAULT_CSRF_MATCHER constant.
     */
    private final ServerWebExchangeMatcher defaultLoggerMatcher = DEFAULT_CSRF_MATCHER;
    /**
     * Represents a private final instance of the LoggersService class.
     * This service is responsible for handling logging operations within the application,
     * providing functionality to log messages at various levels of severity.
     */
    private final LoggersService loggerService;

    /**
     * Caches the request body of a ServerWebExchange and decorates the ServerHttpRequest
     * to be used within the provided function, ensuring the request body can be consumed multiple times.
     *
     * @param <T>      The type of the result emitted by the decorated function.
     * @param exchange The current ServerWebExchange containing the request and response.
     * @param function A function that takes a ServerHttpRequest with a cached body and returns a Mono of T.
     * @return A Mono emitting the result of applying the function to the request with the cached body.
     */
    public static <T> Mono<T> cacheRequestBody(ServerWebExchange exchange,
                                               Function<ServerHttpRequest, Mono<T>> function) {
        ServerHttpResponse response = exchange.getResponse();
        DataBufferFactory factory = response.bufferFactory();
        var requestBody = DataBufferUtils.join(exchange.getRequest().getBody());
        requestBody = requestBody.defaultIfEmpty(factory.wrap(EmptyArrays.EMPTY_BYTES));
        var requestDecorate = requestBody.flatMap(dataBuffer -> Mono.just(requestDecorate(exchange, dataBuffer)));
        return requestDecorate.flatMap(function);
    }

    /**
     * Decorates the ServerHttpResponse to cache the response body and modify it when written.
     * It stores an initial default message in case no response body is provided later.
     * The decoration process involves intercepting the write operation to cache the actual response
     * and restore a previous cached response if needed.
     *
     * @param exchange The current ServerWebExchange containing the response to be decorated.
     * @return A ServerHttpResponseDecorator that overrides the writeWith method to manage caching of response bodies.
     */
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

    /**
     * Decorates the provided ServerHttpRequest to cache its body content.
     * This method is useful for scenarios where the request body needs to be read or processed
     * multiple times without losing its original state.
     *
     * @param exchange   The ServerWebExchange containing the current request and response.
     * @param dataBuffer The DataBuffer representing the body of the request.
     * @return A decorated ServerHttpRequest with the body cached for subsequent accesses.
     * Returns null if the data buffer capacity is zero.
     */
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

    /**
     * Constructs a new DataBuffer based on the type of the input DataBuffer.
     * This method supports conversion for NettyDataBuffer and DefaultDataBuffer instances,
     * ensuring compatibility or preparing the buffer for operations that require a specific format.
     *
     * @param dataBuffer The source DataBuffer to be converted or wrapped.
     * @return A new DataBuffer instance, either wrapped or converted from the input,
     * maintaining the content of the original buffer.
     * @throws IllegalArgumentException If the input DataBuffer is neither a NettyDataBuffer
     *                                  nor a DefaultDataBuffer, indicating an unsupported type.
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

    /**
     * Reads and parses the request body from the given server web exchange.
     *
     * @param exchange The ServerWebExchange containing the request information.
     * @return A JsonNode representing the parsed request body.
     * @throws JsonException if an IOException occurs during JSON parsing.
     */
    private JsonNode readRequestBody(ServerWebExchange exchange) {
        String bodyStr = exchange.getRequiredAttribute(CACHED_REQUEST_BODY_ATTR);
        try {
            return ContextUtils.OBJECT_MAPPER.readTree(bodyStr);
        } catch (IOException exception) {
            throw JsonException.withError(exception);
        }
    }

    /**
     * Filters the given server web exchange based on a matching condition and applies caching before continuing the filter chain.
     * If the default logger matcher determines a match, it proceeds to cache the response and logs the request details using user details.
     * If no match is found, it simply continues the filter chain without caching or logging.
     *
     * @param exchange The current server web exchange to be filtered.
     * @param chain    The filter chain to be invoked for further processing.
     * @return A Mono that completes void when the filtering and optional caching/logging are finished.
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        var filterMono = defaultLoggerMatcher.matches(exchange);
        filterMono = filterMono.filter(ServerWebExchangeMatcher.MatchResult::isMatch);
        filterMono = filterMono.switchIfEmpty(Mono.defer(() -> continueFilterChain(exchange, chain).then(Mono.empty())));
        return filterMono.flatMap((m) -> cacheFilterChain(exchange, chain).then(Mono.defer(ContextUtils::securityDetails))
                .doOnNext(userDetails -> logRequest(exchange, userDetails)).then());
    }

    /**
     * Continues the filter chain by deferring to the next filter in the sequence.
     * This method is typically used within a custom WebFilter implementation to ensure
     * that the request processing flows correctly through the chain after some
     * preliminary actions or checks have been performed.
     *
     * @param exchange The current server web exchange which holds information about
     *                 the client request and provides a way to manipulate the response.
     * @param chain    The WebFilterChain representing the remaining filters to be applied.
     * @return A Mono<Void> indicating the completion or error of the deferred filter operation.
     * The completion signifies that the request has been processed by the next filter
     * in line, or an error indicates that the processing failed at some point in the chain.
     */
    private Mono<Void> continueFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("{}Logger filter chain [continueFilterChain] next.", exchange.getLogPrefix());
        return Mono.defer(() -> chain.filter(exchange));
    }

    /**
     * Continues the filter chain after caching the request body.
     * This method first caches the request body and then proceeds to execute the next filters in the chain.
     *
     * @param exchange The current server web exchange containing the request and response objects.
     * @param chain    The next filter chain to be executed after caching the request body.
     * @return A Mono that completes when the filter chain execution is finished, or signals an error if any occurs.
     */
    private Mono<Void> cacheFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("{}Logger filter chain [cacheRequestBody] next.", exchange.getLogPrefix());
        return cacheRequestBody(exchange, serverHttpRequest -> processRequestBody(exchange, serverHttpRequest))
                .then(Mono.defer(() -> processFilter(exchange, chain)));
    }

    /**
     * Processes the request body from the given ServerWebExchange and ServerHttpRequest.
     *
     * @param exchange          The current server web exchange which holds information about the HTTP request and response.
     * @param serverHttpRequest The actual HTTP request containing the body to be processed.
     * @return A Mono that emits the processed request body as a String.
     */
    private Mono<String> processRequestBody(ServerWebExchange exchange, ServerHttpRequest serverHttpRequest) {
        ServerHttpResponse cachedResponse = responseDecorate(exchange);
        ServerRequest serverRequest = ServerRequest.create(exchange.mutate()
                        .request(serverHttpRequest).response(cachedResponse).build(),
                HandlerStrategies.withDefaults().messageReaders());
        return serverRequest.bodyToMono(String.class).doOnNext((objectValue) -> {
            Object previousCachedBody = exchange.getAttributes().put(CACHED_REQUEST_BODY_ATTR, objectValue);
            log.debug("{}Logger filter chain [processRequestBody] body: {}",
                    exchange.getLogPrefix(), previousCachedBody);
        });
    }

    /**
     * Processes the filter chain after preparing the cached request and response decorators.
     *
     * @param exchange The current server web exchange that holds the request and response information.
     * @param chain    The filter chain to continue processing.
     * @return A Mono that completes when the processing is done or empty if caching attributes are null.
     */
    private Mono<Void> processFilter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest cachedRequest = exchange.getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        ServerHttpResponse cachedResponse = exchange.getAttribute(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);

        if (cachedRequest == null || cachedResponse == null) {
            log.error("{}Logger filter cached request or response is null, Abort processing.",
                    exchange.getLogPrefix());
            return Mono.empty();
        }

        exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
        exchange.getAttributes().remove(CACHED_SERVER_HTTP_RESPONSE_DECORATOR_ATTR);

        return chain.filter(exchange.mutate().request(cachedRequest).response(cachedResponse).build())
                .doFinally(s -> releaseResources(exchange));
    }

    /**
     * Releases the resources held by the cached request and response bodies in the given ServerWebExchange.
     * This method retrieves the backup cached request body and response body from the exchange attributes
     * and then calls the `releaseDataBuffer` method to free up the resources associated with these data buffers.
     *
     * @param exchange The ServerWebExchange context from which the cached bodies' resources will be released.
     */
    private void releaseResources(ServerWebExchange exchange) {
        Object backupCachedBody = exchange.getAttributes().get("cachedOriginalRequestBodyBackup");
        Object backupCachedBody1 = exchange.getAttributes().get("cachedOriginalResponseBodyBackup");

        releaseDataBuffer(backupCachedBody);
        releaseDataBuffer(backupCachedBody1);
    }

    /**
     * Releases the given data buffer object if it is an instance of {@link DataBuffer}.
     * This method ensures that the resources held by the data buffer are properly released.
     *
     * @param dataBufferObject The object that may be a DataBuffer instance to be released.
     */
    private void releaseDataBuffer(Object dataBufferObject) {
        if (dataBufferObject instanceof DataBuffer dataBuffer) {
            try {
                DataBufferUtils.release(dataBuffer);
            } catch (Exception e) {
                log.error("Logger filter failed to release DataBuffer resource.", e);
            }
        }
    }

    /**
     * Logs the details of an HTTP request along with optional user details for security audit purposes.
     * It extracts information from the provided ServerWebExchange including request and response headers,
     * cookies, from parameters, body content, and response status code. The logged data is structured
     * into an ObjectNode which is then used to create a LoggerReq object. This method also associates
     * the log entry with a tenant code extracted from the user details, if available. The logging process
     * is asynchronous and the result is logged at the debug level with a prefixed log message indicating
     * the operation performed.
     *
     * @param exchange    The current server web exchange which holds both the incoming request and the
     *                    outgoing response.
     * @param userDetails Optional security details of the authenticated user, containing a username
     *                    and a tenant code. If not provided, defaults are used.
     */
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

        LoggerReq logger = LoggerReq.of(tenantCode, userDetails.getUsername(), prefix,
                method, status, path, contentNode);
        this.loggerService.operate(logger).share().subscribe(res ->
                log.debug("{}**操作日志** Method: {},MessageBody: {}",
                        exchange.getLogPrefix(), exchange.getRequest().getMethod().name(), res));
    }

    /**
     * Reads and parses the response body from the given server web exchange into a JSON node.
     *
     * @param exchange The ServerWebExchange containing the response body to be read.
     * @return A JsonNode representing the parsed response body. If the body is empty or parsing fails, an empty ObjectNode is returned.
     * @throws JsonException If an IOException occurs during JSON parsing.
     */
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