package com.plate.boot.security.captcha;

import lombok.RequiredArgsConstructor;
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
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.plate.boot.security.captcha.CaptchaRepository.DEFAULT_CAPTCHA_TOKEN_ATTR_NAME;

/**
 * A filter that provides captcha protection for specific endpoints, primarily used to secure OAuth2 token requests.
 * It integrates with session management and captcha token validation to prevent unauthorized access.
 */
@Log4j2
//@Component
@RequiredArgsConstructor
public class CaptchaFilter implements WebFilter, Ordered {

    private static final ServerAccessDeniedHandler ACCESS_DENIED_HANDLER =
            new CaptchaServerAccessDeniedHandler(HttpStatus.FORBIDDEN);
    private static final ServerWebExchangeMatcher REQUIRE_CAPTCHA_PROTECTION_MATCHER =
            new PathPatternParserServerWebExchangeMatcher("/oauth2/token");

    private final CaptchaRepository captchaTokenRepository;

    /**
     * Filters the incoming request to check if it requires captcha protection.
     * If the request matches the captcha protection criteria, it validates the captcha token.
     * If the captcha token is valid, it continues the filter chain.
     * If the captcha token is invalid or an error occurs, it handles the access denied exception.
     *
     * @param exchange the current server web exchange
     * @param chain    the web filter chain
     * @return a Mono that indicates when request processing is complete
     */
    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return REQUIRE_CAPTCHA_PROTECTION_MATCHER.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .flatMap(matchResult -> exchange.getSession()
                        .filter(webSession -> webSession.getAttributes().containsKey(DEFAULT_CAPTCHA_TOKEN_ATTR_NAME))
                        .flatMap(webSession -> validateToken(exchange)))
                .then(Mono.defer(() -> continueFilterChain(exchange, chain)))
                .onErrorResume(CaptchaException.class, ex -> ACCESS_DENIED_HANDLER.handle(exchange, ex));
    }

    private Mono<Void> continueFilterChain(ServerWebExchange exchange, WebFilterChain chain) {
        log.debug("{}Captcha filter chain continue next.", exchange.getLogPrefix());
        return Mono.defer(() -> chain.filter(exchange));
    }

    private Mono<Void> validateToken(ServerWebExchange exchange) {
        return this.captchaTokenRepository.loadToken(exchange)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new CaptchaException("An expected Captcha token cannot be found"))))
                .filterWhen((captchaToken) -> containsValidCaptchaToken(exchange, captchaToken))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new CaptchaException("Invalid Captcha Token"))))
                .then(this.captchaTokenRepository.clearToken(exchange));
    }

    private Mono<Boolean> containsValidCaptchaToken(ServerWebExchange exchange, CaptchaToken captchaToken) {
        return this.resolveCaptchaTokenValue(exchange, captchaToken).map(captchaToken::validate);
    }

    private Mono<String> resolveCaptchaTokenValue(ServerWebExchange exchange, CaptchaToken captchaToken) {
        String captchaCode = exchange.getRequest().getHeaders().getFirst(captchaToken.headerName());
        return Mono.justOrEmpty(captchaCode);
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