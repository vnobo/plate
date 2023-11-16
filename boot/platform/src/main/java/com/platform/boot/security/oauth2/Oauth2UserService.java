package com.platform.boot.security.oauth2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.SecurityDetails;
import com.platform.boot.security.SecurityManager;
import com.platform.boot.security.core.user.User;
import com.platform.boot.security.core.user.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GITHUB;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Component
@RequiredArgsConstructor
public class Oauth2UserService extends DefaultReactiveOAuth2UserService {

    private final SecurityManager securityManager;

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Function<OAuth2User, Mono<OAuth2User>> loadByLocalUser = oAuth2User ->
                this.loadLocalUser(userRequest.getClientRegistration().getRegistrationId(), oAuth2User)
                        .switchIfEmpty(Mono.defer(() -> Mono.just(oAuth2User)));
        return super.loadUser(userRequest).flatMap(loadByLocalUser);
    }

    public Mono<OAuth2User> loadLocalUser(String registrationId, OAuth2User oAuth2User) {
        return this.securityManager.loadByOauth2(registrationId, oAuth2User.getName())
                .delayUntil(user -> this.modifyUser(user, registrationId, oAuth2User))
                .switchIfEmpty(Mono.defer(() -> this.registerUser(registrationId, oAuth2User)))
                .map(user -> this.convertToOauth2User(user, oAuth2User));
    }

    public Mono<Void> modifyUser(User user, String registrationId, OAuth2User oAuth2User) {
        var request = this.convertToUserRequest(registrationId, oAuth2User);
        request.setId(user.getId());
        request.setUsername(user.getUsername());
        request.setCode(user.getCode());
        ObjectNode oldExtend = Optional.ofNullable(user.getExtend())
                .map(node -> (ObjectNode) node.deepCopy())
                .orElse(ContextUtils.OBJECT_MAPPER.createObjectNode());
        ObjectNode oauth2 = Optional.ofNullable(oldExtend.get("oauth2"))
                .map(node -> (ObjectNode) node.deepCopy())
                .orElse(ContextUtils.OBJECT_MAPPER.createObjectNode());
        oauth2.set("registrationId", request.getExtend().get("oauth2").get(registrationId));
        oldExtend.set("oauth2", oauth2);
        request.setExtend(oldExtend);
        return this.securityManager.registerOrModifyUser(request).then();
    }

    public Mono<User> registerUser(String registrationId, OAuth2User oAuth2User) {
        var request = this.convertToUserRequest(registrationId, oAuth2User);
        return this.securityManager.registerOrModifyUser(request);
    }

    public UserRequest convertToUserRequest(String registrationId, OAuth2User oAuth2User) {
        UserRequest request = new UserRequest();
        request.setPassword(generateRandoPassword());
        ObjectNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        if (registrationId.equalsIgnoreCase(GITHUB.name()) || "gitee".equalsIgnoreCase(registrationId)) {
            String username = registrationId + "#" + oAuth2User.getAttribute("login")
                    + "-" + oAuth2User.getAttribute("id");
            request.setUsername(username);
            request.setName(oAuth2User.getAttribute("name"));
            request.setEmail(oAuth2User.getAttribute("email"));
            request.setAvatar(oAuth2User.getAttribute("avatar_url"));
            request.setBio(oAuth2User.getAttribute("bio"));
            ObjectNode registrationNode = extend.deepCopy();
            registrationNode.putPOJO(registrationId, Map.of("openid", oAuth2User.getName(),
                    "attributes", oAuth2User.getAttributes()));
            extend.set("oauth2", registrationNode);
            request.setExtend(extend);
        } else {
            //todo register other oauth2 default username
            request.setUsername(oAuth2User.getName());
        }
        return request;
    }

    public OAuth2User convertToOauth2User(User details, OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("username", details.getUsername());
        return SecurityDetails.of(details.getCode(), details.getUsername(), details.getName(), details.getPassword(),
                details.getDisabled(), details.getAccountExpired(),
                details.getAccountLocked(), details.getCredentialsExpired(),
                oAuth2User.getAuthorities(), attributes, "username");
    }

    public static String generateRandoPassword() {
        byte[] randomBytes = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }
}