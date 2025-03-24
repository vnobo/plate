package com.plate.boot.security.oauth2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
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
 * Oauth2UserService is an extension of the DefaultReactiveOAuth2UserService that customizes the loading process
 * for OAuth2 users, allowing for additional operations such as local user loading, modification, and registration.
 * It collaborates with a SecurityManager to manage user data securely and handles various OAuth2 providers.
 */
@Component
@RequiredArgsConstructor
public class Oauth2UserService extends DefaultReactiveOAuth2UserService {

    /**
     * A thread-safe, cryptographically strong pseudo-random number generator (PRNG) instance
     * used for generating secure random numbers. This instance of {@link SecureRandom} is
     * initialized statically and can be utilized for cryptographic purposes that require
     * unpredictable and unguessable sequences, enhancing the security of operations like
     * password generation, token creation, and other sensitive processes within the application.
     */
    private final static SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * The {@code SecurityManager} instance is a core component responsible for handling
     * security-related operations such as user authentication, authorization, and management.
     * It encapsulates logic for loading user details, updating passwords, registering new users,
     * and modifying existing ones based on OAuth2 user information. This field provides direct access
     * to the security manager within the {@code Oauth2UserService} class, enabling tight integration
     * with security functionalities throughout the OAuth2 user service process.
     */
    private final SecurityManager securityManager;

    /**
     * Generates a random password encoded in Base64 format.
     * <p>
     * This method utilizes a secure random number generator to create a sequence of 16 random bytes,
     * which are then encoded using Base64 encoding to form a password string that is both secure and compatible with various systems.
     *
     * @return A randomly generated password string encoded in Base64.
     */
    public static String generateRandoPassword() {
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getEncoder().encodeToString(randomBytes);
    }

    /**
     * Loads an OAuth2User based on the provided OAuth2UserRequest.
     * If a local user corresponding to the OAuth2 user is found, it is returned after potential modification.
     * Otherwise, the original OAuth2User is returned.
     *
     * @param userRequest The request containing all the information about the authentication request.
     * @return A Mono that emits the loaded OAuth2User, either modified from the local system or the original one.
     * @throws OAuth2AuthenticationException If there is an error during the loading process.
     */
    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Function<OAuth2User, Mono<OAuth2User>> loadByLocalUser = oAuth2User ->
                this.loadLocalUser(userRequest.getClientRegistration().getRegistrationId(), oAuth2User)
                        .switchIfEmpty(Mono.defer(() -> Mono.just(oAuth2User)));
        return super.loadUser(userRequest).flatMap(loadByLocalUser);
    }

    /**
     * Asynchronously loads a local user based on the provided registration ID and OAuth2User instance.
     * If a user is found, it is potentially modified according to the OAuth2 information and returned.
     * If no local user is found, a new user registration process is initiated.
     *
     * @param registrationId The identifier for the OAuth2 registration.
     * @param oAuth2User     The OAuth2 user details obtained from the authentication provider.
     * @return A Mono that, upon subscription, emits the modified or newly registered OAuth2User instance.
     */
    public Mono<OAuth2User> loadLocalUser(String registrationId, OAuth2User oAuth2User) {
        return this.securityManager.loadByOauth2(registrationId, oAuth2User.getName())
                .delayUntil(user -> this.modifyUser(user, registrationId, oAuth2User))
                .switchIfEmpty(Mono.defer(() -> this.registerUser(registrationId, oAuth2User)))
                .map(user -> this.convertToOauth2User(user, oAuth2User));
    }

    /**
     * Modifies an existing user with details from the OAuth2User object and updates the user's extended data.
     * The method merges the OAuth2-specific data into the user's 'extend' field under the 'oauth2' key,
     * taking care to preserve existing data and update it with the new registrationId details.
     *
     * @param user           The User entity to be modified, identified by its unique attributes.
     * @param registrationId The identifier for the OAuth2 registration that is associated with the OAuth2User.
     * @param oAuth2User     The OAuth2User object containing additional details to integrate into the user entity.
     * @return A Mono<Void> that signals completion when the user has been successfully modified asynchronously.
     */
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

    /**
     * Registers a new user or modifies an existing user based on the provided OAuth2 user information.
     * This method first converts the OAuth2User object into a UserReq and then
     * delegates the registration or modification process to the SecurityManager.
     *
     * @param registrationId The unique identifier for the OAuth2 registration.
     * @param oAuth2User     The OAuth2User object containing user details obtained from the OAuth2 provider.
     * @return A Mono that, when subscribed to, emits the User object representing the registered or modified user.
     */
    public Mono<User> registerUser(String registrationId, OAuth2User oAuth2User) {
        var request = this.convertToUserRequest(registrationId, oAuth2User);
        return this.securityManager.registerOrModifyUser(request);
    }

    /**
     * Converts an OAuth2User object into a UserReq, preparing it for user registration or update.
     * This method handles specifics based on the registrationId, setting default attributes
     * like a random password and populating fields from the OAuth2User's attributes.
     *
     * @param registrationId The identifier for the OAuth2 registration (e.g., 'github', 'gitee').
     * @param oAuth2User     The OAuth2User object containing user data fetched from the OAuth provider.
     * @return A UserReq instance populated with data necessary for user registration or profile update,
     * including a generated password and extended attributes specific to the OAuth2 registration.
     */
    public UserReq convertToUserRequest(String registrationId, OAuth2User oAuth2User) {
        UserReq request = new UserReq();
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

    /**
     * Converts a User object along with an OAuth2User object into a customized OAuth2User instance.
     * It augments the OAuth2User's attributes with additional details from the User object,
     * preparing the user information for security context population with enriched data.
     *
     * @param user    The User entity containing local user details like username, password, and account status.
     * @param oAuth2User The original OAuth2User object carrying OAuth provider-specific attributes and authorities.
     * @return A new OAuth2User instance, SecurityDetails, with combined attributes from both the User and OAuth2User,
     * including custom attributes such as 'username'.
     */
    public OAuth2User convertToOauth2User(User user, OAuth2User oAuth2User) {
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("username", user.getUsername());
        return SecurityDetails.of(user, oAuth2User.getAuthorities(),attributes);
    }
}