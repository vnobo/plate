package com.platform.boot.security.filter;

import com.platform.boot.security.core.captcha.CaptchaRepository;
import com.platform.boot.security.core.captcha.CaptchaToken;
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

import static com.platform.boot.security.core.captcha.CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Component
public class CaptchaFilter implements WebFilter, Ordered {

    public static final ServerWebExchangeMatcher DEFAULT_CAPTCHA_MATCHER =
            new PathPatternParserServerWebExchangeMatcher("/oauth2/token");
    private static final ServerAccessDeniedHandler ACCESS_DENIED_HANDLER = new CaptchaServerAccessDeniedHandler(
            HttpStatus.FORBIDDEN);
    private static final ServerWebExchangeMatcher REQUIRE_CAPTCHA_PROTECTION_MATCHER = DEFAULT_CAPTCHA_MATCHER;

    private final CaptchaRepository captchaTokenRepository;

    public CaptchaFilter(CaptchaRepository captchaTokenRepository) {
        this.captchaTokenRepository = captchaTokenRepository;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return REQUIRE_CAPTCHA_PROTECTION_MATCHER.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(Mono.defer(() -> continueFilterChain(exchange, chain).then(Mono.empty())))
                .flatMap(matchResult -> exchange.getSession()
                        .filter(webSession -> webSession.getAttributes().containsKey(DEFAULT_CAPTCHA_TOKEN_ATTR_NAME))
                        .flatMap(webSession -> validateToken(exchange)))
                .onErrorResume(CaptchaException.class, ex -> ACCESS_DENIED_HANDLER.handle(exchange, ex));
    }

    private Mono<Void> continueFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("{}Captcha filter chain continue next.", exchange.getLogPrefix());
        return Mono.defer(() -> chain.filter(exchange));
    }

    private Mono<Void> validateToken(ServerWebExchange exchange) {
        return this.captchaTokenRepository.loadToken(exchange)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CaptchaException("An expected Captcha token cannot be found"))))
                .filterWhen((expected) -> containsValidCaptchaToken(exchange, expected))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new CaptchaException("Invalid Captcha Token"))))
                .then(this.captchaTokenRepository.clearToken(exchange));
    }

    private Mono<Boolean> containsValidCaptchaToken(ServerWebExchange exchange, CaptchaToken expected) {
        return this.resolveCaptchaTokenValue(exchange, expected).map((actual) -> true);
    }

    private Mono<String> resolveCaptchaTokenValue(ServerWebExchange exchange, CaptchaToken captchaToken) {
        Assert.notNull(exchange, "exchange cannot be null");
        Assert.notNull(captchaToken, "captchaToken cannot be null");
        return exchange.getFormData().flatMap((data) -> Mono.justOrEmpty(data.getFirst(captchaToken.getParameterName())))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(captchaToken.getHeaderName()))));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    static class CaptchaException extends AccessDeniedException {
        public CaptchaException(String message) {
            super(message);
        }
    }

    @Log4j2
    static class CaptchaServerAccessDeniedHandler implements ServerAccessDeniedHandler {
        private final HttpStatus httpStatus;

        public CaptchaServerAccessDeniedHandler(HttpStatus httpStatus) {
            Assert.notNull(httpStatus, "httpStatus cannot be null");
            this.httpStatus = httpStatus;
        }

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