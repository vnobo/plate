package com.plate.boot.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import com.plate.boot.commons.exception.RestServerException;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * {@link WechatClient} implementation calling the WeChat mini-program code-to-session endpoint.
 * <p>
 * The WeChat response is consumed as a generic map to stay independent of JSON binding
 * details. Responses carrying a non-zero {@code errcode} are translated into authentication
 * failures ({@link BadCredentialsException} for invalid login codes) or server errors
 * ({@link RestServerException} for configuration/upstream problems).
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class WechatApiClient implements WechatClient {

    /**
     * WeChat error code returned when the presented {@code js_code} is invalid or expired.
     */
    private static final int ERR_CODE_INVALID_LOGIN_CODE = 40029;

    private static final String FIELD_ERRCODE = "errcode";
    private static final String FIELD_ERRMSG = "errmsg";
    private static final String FIELD_OPENID = "openid";
    private static final String FIELD_SESSION_KEY = "session_key";
    private static final String FIELD_UNIONID = "unionid";

    private final WebClient.Builder webClientBuilder;
    private final WechatMiniAppProperties properties;

    /**
     * Exchanges the temporary mini-program login code for the user's WeChat identity.
     *
     * @param code the {@code wx.login} code to exchange
     * @return a {@link Mono} emitting the resolved {@link WechatSession}
     */
    @Override
    public Mono<WechatSession> code2Session(String code) {
        var uri = UriComponentsBuilder.fromUriString(this.properties.getCode2SessionUrl())
                .queryParam("appid", this.properties.getAppId())
                .queryParam("secret", this.properties.getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .encode().build().toUri();
        return this.webClientBuilder.build().get().uri(uri)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                })
                .flatMap(WechatApiClient::toSession)
                .onErrorMap(WebClientResponseException.class,
                        e -> RestServerException.withMsg("WeChat code2Session http error", e));
    }

    /**
     * Maps the raw WeChat response to a {@link WechatSession}, surfacing upstream errors.
     */
    private static Mono<WechatSession> toSession(Map<String, Object> body) {
        if (CollectionUtils.isEmpty(body) || !body.containsKey(FIELD_OPENID)) {
            if (body != null && body.containsKey(FIELD_ERRCODE)) {
                int errCode = Integer.parseInt(String.valueOf(body.get(FIELD_ERRCODE)));
                String errMsg = String.valueOf(body.get(FIELD_ERRMSG));
                if (ERR_CODE_INVALID_LOGIN_CODE == errCode) {
                    return Mono.error(new BadCredentialsException("WeChat login code invalid: " + errMsg));
                }
                return Mono.error(RestServerException.withMsg("WeChat code2Session error " + errCode + ":" + errMsg,
                        new IllegalStateException("WeChat code2Session error")));
            }
            return Mono.error(RestServerException.withMsg("WeChat code2Session empty response",
                    new IllegalStateException("WeChat code2Session empty response")));
        }
        String openid = String.valueOf(body.get(FIELD_OPENID));
        String sessionKey = body.get(FIELD_SESSION_KEY) == null ? null : String.valueOf(body.get(FIELD_SESSION_KEY));
        String unionid = body.get(FIELD_UNIONID) == null ? null : String.valueOf(body.get(FIELD_UNIONID));
        log.debug("WeChat code2Session success, unionid present: {}", unionid != null);
        return Mono.just(new WechatSession(openid, sessionKey, unionid));
    }
}
