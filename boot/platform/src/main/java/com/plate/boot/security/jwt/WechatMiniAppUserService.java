package com.plate.boot.security.jwt;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import com.plate.boot.security.oauth2.Oauth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * Provisions and resolves local users for WeChat mini-program authentication.
 * <p>
 * The flow mirrors the OAuth2 binding strategy used by {@link Oauth2UserService}: the WeChat
 * {@code openid} is stored under {@code extend -> oauth2 -> wechat_miniapp -> openid} and
 * located via {@link SecurityManager#loadByOauth2(String, String)}. First-time visitors are
 * registered automatically with a random (unusable) password so the account can only be used
 * through the WeChat binding.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class WechatMiniAppUserService {

    /**
     * Bind type key persisted in the user {@code extend} JSON column. Must match
     * {@code ^[a-zA-Z0-9_]+$} as enforced by {@link SecurityManager}.
     */
    public static final String BIND_TYPE = "wechat_miniapp";

    /**
     * Display name assigned to auto-registered mini-program users.
     */
    private static final String DEFAULT_USER_NAME = "微信用户";

    private final SecurityManager securityManager;
    private final WechatClient wechatClient;

    /**
     * Exchanges the mini-program login code and resolves the matching local user,
     * auto-registering the account on first login.
     *
     * @param code the {@code wx.login} code issued by the WeChat mini-program runtime
     * @return a {@link Mono} emitting the fully populated {@link SecurityDetails}
     */
    public Mono<SecurityDetails> login(String code) {
        return this.wechatClient.code2Session(code)
                .flatMap(this::loadOrRegister)
                .flatMap(user -> this.securityManager.findByUsername(user.getUsername()))
                .ofType(SecurityDetails.class);
    }

    /**
     * Loads the local user bound to the WeChat identity, registering it when absent.
     */
    private Mono<User> loadOrRegister(WechatSession session) {
        return this.securityManager.loadByOauth2(BIND_TYPE, session.openid())
                .switchIfEmpty(Mono.defer(() -> this.register(session)));
    }

    /**
     * Registers a new local user bound to the given WeChat identity.
     */
    private Mono<User> register(WechatSession session) {
        log.info("Registering mini-program user, unionid present: {}", session.unionid() != null);
        UserReq request = new UserReq();
        request.setUsername(BIND_TYPE + "#" + session.openid());
        request.setPassword(Oauth2UserService.generateRandoPassword());
        request.setName(DEFAULT_USER_NAME);
        ObjectNode extend = JsonNodeFactory.instance.objectNode();
        ObjectNode bind = JsonNodeFactory.instance.objectNode();
        bind.put("openid", session.openid());
        if (Optional.ofNullable(session.unionid()).isPresent()) {
            bind.put("unionid", session.unionid());
        }
        ObjectNode oauth2 = JsonNodeFactory.instance.objectNode();
        oauth2.set(BIND_TYPE, bind);
        extend.set("oauth2", oauth2);
        request.setExtend(extend);
        return this.securityManager.registerOrModifyUser(request);
    }
}
