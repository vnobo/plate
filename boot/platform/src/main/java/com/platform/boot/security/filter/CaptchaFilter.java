package com.platform.boot.security.filter;

import com.platform.boot.security.captcha.CaptchaRepository;
import com.platform.boot.security.captcha.CaptchaToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Component
public class CaptchaFilter implements WebFilter, Ordered {
    public static final ServerWebExchangeMatcher DEFAULT_CAPTCHA_MATCHER =
            new PathPatternParserServerWebExchangeMatcher("/oauth2/token");

    private final ServerAccessDeniedHandler accessDeniedHandler = new CaptchaServerAccessDeniedHandler(
            HttpStatus.FORBIDDEN);
    private final ServerWebExchangeMatcher requireCaptchaProtectionMatcher = DEFAULT_CAPTCHA_MATCHER;
    private final CaptchaRepository captchaTokenRepository;

    public CaptchaFilter(CaptchaRepository captchaTokenRepository) {
        this.captchaTokenRepository = captchaTokenRepository;
    }

    /**
     * Filter to handle server-side captcha protection. If a captcha token is present in theSession Attributes, and validation is successful, the filter chain is invoked. Otherwise, an access denied exception is thrown.
     *
     * @param exchange The {@code ServerWebExchange} object
     * @param chain    The {@code WebFilterChain} object
     * @return Void Mono
     */
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        // Check if captcha protection is required
        return this.requireCaptchaProtectionMatcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                // Check if captcha token is present in session
                .filterWhen((matchResult) -> exchange.getSession().map(webSession -> webSession.getAttributes()
                        .containsKey(CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME)))
                // Validate the token
                .flatMap((m) -> validateToken(exchange))
                // If no exception, proceed with the filter chain
                .switchIfEmpty(Mono.defer(() -> chain.filter(exchange)))
                // Handle captcha exception
                .onErrorResume(CaptchaException.class, (ex) -> this.accessDeniedHandler.handle(exchange, ex));
    }

    /**
     * Validates the captcha token in the request.
     *
     * @param exchange The server web exchange
     * @return A mono of void
     */
    private Mono<Void> validateToken(ServerWebExchange exchange) {
        return this.captchaTokenRepository.loadToken(exchange)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CaptchaException("An expected Captcha token cannot be found"))))
                .filterWhen((expected) -> containsValidCaptchaToken(exchange, expected))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CaptchaException("Invalid Captcha Token"))))
                .then(this.captchaTokenRepository.clearToken(exchange));
    }

    /**
     * Checks if the request contains a valid captcha token.
     *
     * @param exchange The server web exchange
     * @param expected The expected captcha token
     * @return A mono of boolean
     */
    private Mono<Boolean> containsValidCaptchaToken(ServerWebExchange exchange, CaptchaToken expected) {
        return this.resolveCaptchaTokenValue(exchange, expected)
                .map((actual) -> true);
    }

    /**
     * Resolves the captcha token value from the request.
     *
     * @param exchange     The server web exchange
     * @param captchaToken The captcha token
     * @return A mono of string
     */
    private Mono<String> resolveCaptchaTokenValue(ServerWebExchange exchange, CaptchaToken captchaToken) {
        Assert.notNull(exchange, "exchange cannot be null");
        Assert.notNull(captchaToken, "captchaToken cannot be null");
        return exchange.getFormData().flatMap((data) -> Mono.justOrEmpty(data.getFirst(captchaToken.getParameterName())))
                .switchIfEmpty(
                        Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(captchaToken.getHeaderName())));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Exception thrown when an invalid captcha token is encountered.
     */
    static class CaptchaException extends AccessDeniedException {
        public CaptchaException(String message) {
            super(message);
        }
    }

    @Log4j2
    static class CaptchaServerAccessDeniedHandler implements ServerAccessDeniedHandler {
        private final HttpStatus httpStatus;

        /**
         * Creates an instance with the provided status
         *
         * @param httpStatus the status to use
         */
        public CaptchaServerAccessDeniedHandler(HttpStatus httpStatus) {
            Assert.notNull(httpStatus, "httpStatus cannot be null");
            this.httpStatus = httpStatus;
        }

        /**
         * This method handles the access denied exception by setting the response status to the provided status and
         * writing the exception message to the response body.
         *
         * @param exchange ServerWebExchange object
         * @param ex       AccessDeniedException object
         * @return Mono<Void> indicating completion of the response
         */
        @Override
        public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
            if (log.isDebugEnabled()) {
                log.error("Access denied exception.", ex);
            }
            return Mono.defer(() -> Mono.just(exchange.getResponse())).flatMap((response) -> {
                response.setStatusCode(this.httpStatus);
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                var body = """
                        {"captcha":"/captcha/code","code":403,"msg":"验证码认证失败,请重试!","errors":"%s"}
                        """.formatted(ex.getMessage());
                DataBufferFactory dataBufferFactory = response.bufferFactory();
                DataBuffer buffer = dataBufferFactory.wrap(body.getBytes());
                return response.writeWith(Flux.just(buffer))
                        .doOnError((error) -> DataBufferUtils.release(buffer));
            });
        }

    }

}