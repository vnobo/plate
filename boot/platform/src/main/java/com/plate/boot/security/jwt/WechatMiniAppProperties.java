package com.plate.boot.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the WeChat mini-program authentication integration.
 * <p>
 * Bound from the {@code plate.security.wechat.miniapp} prefix. The {@code appId} and
 * {@code appSecret} are issued by the WeChat open platform and are used by
 * {@link WechatApiClient} to exchange a {@code wx.login} code for a stable {@code openid}
 * (plus {@code unionid} when the mini-program is bound to an open-platform account).
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@ConfigurationProperties(prefix = "plate.security.wechat.miniapp")
public class WechatMiniAppProperties {

    /**
     * WeChat mini-program application id.
     */
    private String appId = "";

    /**
     * WeChat mini-program application secret.
     */
    private String appSecret = "";

    /**
     * WeChat code-to-session endpoint URL.
     */
    private String code2SessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
}
