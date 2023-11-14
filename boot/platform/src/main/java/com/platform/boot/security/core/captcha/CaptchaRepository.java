package com.platform.boot.security.core.captcha;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
public class CaptchaRepository {
    public static final String DEFAULT_CAPTCHA_TOKEN_ATTR_NAME = CaptchaRepository.class.getName()
            .concat(".CAPTCHA_TOKEN");
    static final String DEFAULT_CAPTCHA_PARAMETER_NAME = "_captcha";
    static final String DEFAULT_CAPTCHA_HEADER_NAME = "X-CAPTCHA-TOKEN";

    protected String parameterName = DEFAULT_CAPTCHA_PARAMETER_NAME;
    protected String headerName = DEFAULT_CAPTCHA_HEADER_NAME;
    protected String sessionAttributeName = DEFAULT_CAPTCHA_TOKEN_ATTR_NAME;

    /**
     * This method is used to generate a CaptchaToken and store it in the exchange
     * for later use.
     *
     * @param exchange the exchange
     * @return a Mono that encapsulates the CaptchaToken
     */
    public Mono<CaptchaToken> generateToken(ServerWebExchange exchange) {
        return Mono.fromCallable(this::createCaptchaToken)
                .doOnNext(captchaToken -> exchange.getAttributes().put(CaptchaToken.class.getName(), captchaToken))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> saveToken(ServerWebExchange exchange, CaptchaToken token) {
        return exchange.getSession().doOnNext((session) -> putToken(session.getAttributes(), token))
                .flatMap(WebSession::changeSessionId);
    }

    public Mono<Void> clearToken(ServerWebExchange exchange) {
        return exchange.getSession().doOnNext((session) -> putToken(session.getAttributes(), null))
                .flatMap(WebSession::changeSessionId);
    }

    private void putToken(Map<String, Object> attributes, CaptchaToken token) {
        if (token == null) {
            attributes.remove(this.sessionAttributeName);
        } else {
            attributes.put(this.sessionAttributeName, token);
        }
    }

    public Mono<CaptchaToken> loadToken(ServerWebExchange exchange) {
        return exchange.getSession().filter((session) -> session.getAttributes().containsKey(this.sessionAttributeName))
                .mapNotNull((session) -> session.getAttribute(this.sessionAttributeName));
    }

    protected CaptchaToken createCaptchaToken() {
        // 定义图形验证码的长、宽、验证码字符数、干扰元素个数
        // LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 10);
        // captcha.setGenerator(new RandomGenerator("0123456789", 4));
        return CaptchaToken.of(this.headerName, this.parameterName, null);

    }

}