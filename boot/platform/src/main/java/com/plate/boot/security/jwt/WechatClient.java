package com.plate.boot.security.jwt;

import reactor.core.publisher.Mono;

/**
 * Abstraction over the WeChat open-platform identity endpoints.
 * <p>
 * Extracted as an interface so the mini-program login flow can be unit tested with a
 * deterministic stub while production uses {@link WechatApiClient}.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public interface WechatClient {

    /**
     * Exchanges a {@code wx.login} code for the user's WeChat identity.
     *
     * @param code the temporary login code issued by the WeChat mini-program runtime
     * @return a {@link Mono} emitting the resolved {@link WechatSession}
     */
    Mono<WechatSession> code2Session(String code);
}
