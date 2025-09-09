package com.plate.boot.security.captcha;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

/**
 * Captcha Repository service class, used to generate, save, load and clear captcha tokens
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
public class CaptchaRepository {
    /**
     * Default captcha token attribute name in session
     */
    public static final String DEFAULT_CAPTCHA_TOKEN_ATTR_NAME = CaptchaRepository.class.getName()
            .concat(".CAPTCHA_TOKEN");

    /**
     * Default captcha parameter name
     */
    static final String DEFAULT_CAPTCHA_PARAMETER_NAME = "_captcha";

    /**
     * Default captcha header name
     */
    static final String DEFAULT_CAPTCHA_HEADER_NAME = "X-CAPTCHA-TOKEN";

    protected String parameterName = DEFAULT_CAPTCHA_PARAMETER_NAME;
    protected String headerName = DEFAULT_CAPTCHA_HEADER_NAME;
    protected String sessionAttributeName = DEFAULT_CAPTCHA_TOKEN_ATTR_NAME;

    /**
     * Generate captcha token
     *
     * @param exchange Server web exchange, contains HTTP request and response information
     * @return Mono object containing captcha token
     */
    public Mono<CaptchaToken> generateToken(ServerWebExchange exchange) {
        return Mono.fromCallable(this::createCaptchaToken)
                .doOnNext(captchaToken -> exchange.getAttributes().put(CaptchaToken.class.getName(), captchaToken))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Save captcha token to session
     *
     * @param exchange Server web exchange, contains HTTP request and response information
     * @param token    Captcha token to be saved
     * @return Mono object representing completion of save operation
     */
    public Mono<Void> saveToken(ServerWebExchange exchange, CaptchaToken token) {
        return exchange.getSession().doOnNext((session) -> putToken(session.getAttributes(), token))
                .flatMap(WebSession::changeSessionId);
    }

    /**
     * Clear captcha token from session
     *
     * @param exchange Server web exchange, contains HTTP request and response information
     * @return Mono object representing completion of clear operation
     */
    public Mono<Void> clearToken(ServerWebExchange exchange) {
        return exchange.getSession().doOnNext((session) -> putToken(session.getAttributes(), null))
                .flatMap(WebSession::changeSessionId);
    }

    /**
     * Put captcha token into session attributes, remove the attribute if token is null
     *
     * @param attributes Session attributes map
     * @param token Captcha token, null means need to remove
     */
    private void putToken(Map<String, Object> attributes, CaptchaToken token) {
        if (token == null) {
            attributes.remove(this.sessionAttributeName);
        } else {
            attributes.put(this.sessionAttributeName, token);
        }
    }

    /**
     * Load captcha token from session
     *
     * @param exchange Server web exchange, contains HTTP request and response information
     * @return Mono object containing captcha token, empty if not exists
     */
    public Mono<CaptchaToken> loadToken(ServerWebExchange exchange) {
        return exchange.getSession().filter((session) -> session.getAttributes().containsKey(this.sessionAttributeName))
                .mapNotNull((session) -> session.getAttribute(this.sessionAttributeName));
    }

    /**
     * Create new captcha token
     *
     * @return Newly created captcha token
     */
    protected CaptchaToken createCaptchaToken() {
        //LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 10);
        //captcha.setGenerator(new RandomGenerator("0123456789", 4));
        return CaptchaToken.of(this.headerName, this.parameterName, "54321");
    }

}