package com.plate.boot.security.jwt;

/**
 * Identity returned by the WeChat code-to-session exchange.
 *
 * @param openid    the user's stable identity within the mini-program.
 * @param sessionKey the WeChat session key (never persisted or logged by the platform).
 * @param unionid   the cross-application identity, present when the mini-program is bound
 *                  to a WeChat open-platform account; may be {@code null}.
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public record WechatSession(String openid, String sessionKey, String unionid) {
}
