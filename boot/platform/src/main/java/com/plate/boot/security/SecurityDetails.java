package com.plate.boot.security;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.group.member.GroupMemberResponse;
import com.plate.boot.security.core.tenant.member.TenantMemberResponse;
import com.plate.boot.security.core.user.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents security details for a user, extending the DefaultOAuth2User and implementing UserDetails.
 * This class encapsulates user identity and authentication information along with additional security-related properties.
 * It is designed to work seamlessly with OAuth2-based authentication systems and Spring Security's user details interface.
 */
@Setter
@Getter
public final class SecurityDetails extends DefaultOAuth2User implements UserDetails {

    /**
     * Represents the unique identifier code for a security detail within the {@link SecurityDetails} class.
     * This string value is used to distinguish different security details instances.
     */
    private String code;

    /**
     * Represents the username of the security principal.
     * This field stores the unique identifier for a user within the system, typically used for authentication and authorization purposes.
     */
    private String username;

    /**
     * Represents the nickname of a user within the security details.
     * This string holds the colloquial or display name associated with the user's account.
     */
    private String nickname;

    /**
     * A collection of {@link TenantMemberResponse} objects representing the tenants associated with the security details.
     * Each {@code TenantMemberResponse} encapsulates information about a tenant member, including a unique identifier,
     * tenant code, user code, enabled status, and audit details provided by {@link UserAuditor}.
     * This set facilitates management and access to tenant-specific data for the authenticated user within a security context.
     */
    private Set<TenantMemberResponse> tenants;

    /**
     * Represents a collection of {@link GroupMemberResponse} instances associated with a security details object.
     * This set encapsulates the group membership responses for a user, providing information about the groups
     * the user is a part of, including each group's name and additional metadata in the form of a JSON node.
     */
    private Set<GroupMemberResponse> groups;

    /**
     * The password field securely stores the authentication credential for a user.
     * This field is marked as ignored for JSON serialization and deserialization to prevent
     * password exposure in transit or storage. Use the provided password methods to handle
     * password operations safely.
     */
    @JsonIgnore
    private String password;

    /**
     * Indicates whether the security details are disabled.
     * This field is not serialized during JSON processing due to the {@link JsonIgnore} annotation.
     */
    @JsonIgnore
    private Boolean disabled;

    /**
     * Indicates whether the account has expired or not.
     * This field is not serialized in JSON responses due to the {@link JsonIgnore} annotation.
     */
    @JsonIgnore
    private Boolean accountExpired;

    /**
     * Indicates whether the account is locked or not.
     * This field is not serialized in JSON responses due to the {@link JsonIgnore} annotation.
     */
    @JsonIgnore
    private Boolean accountLocked;

    /**
     * Indicates whether the user's credentials (password) have expired.
     * This flag is used to determine if the user needs to reset their password upon next login attempt.
     */
    @JsonIgnore
    private Boolean credentialsExpired;
    /**
     * The email address associated with the user.
     * This field holds the user's email which is used for communication and can be a primary contact point.
     */
    private String email;

    /**
     * Represents the user's contact phone number.
     * This string field holds the phone number associated with a user's profile.
     * It is used for communication purposes, such as account verification, service notifications, or support contacts.
     */
    private String phone;

    /**
     * The private field representing the name of the user.
     * This string holds the personal name or full name of the user account.
     */
    private String name;

    /**
     * Represents the profile picture or graphical representation associated with a user.
     * This string field holds the reference or URL to the user's avatar image.
     */
    private String avatar;

    /**
     * Represents the biography or a brief description about the user.
     * This string field can include personal background, professional experience, or any other relevant long-form text
     * that provides more insight into the user's identity or profile.
     */
    private String bio;

    public SecurityDetails(Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
    }

    public static SecurityDetails of(String code, Collection<? extends GrantedAuthority> authorities,
                                     Map<String, Object> attributes, String nameAttributeKey) {
        SecurityDetails securityDetails = new SecurityDetails(authorities, attributes, nameAttributeKey);
        securityDetails.setCode(code);
        return securityDetails;
    }

    public SecurityDetails buildUser(User user) {
        this.setCode(user.getCode());
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
        this.setNickname(user.getName());
        this.setEmail(user.getEmail());
        this.setPhone(user.getPhone());
        this.setAvatar(user.getAvatar());
        this.setBio(user.getBio());
        this.setDisabled(user.getDisabled());
        this.setAccountExpired(user.getAccountExpired());
        this.setAccountLocked(user.getAccountLocked());
        this.setCredentialsExpired(user.getCredentialsExpired());
        return this;
    }


    public SecurityDetails password(String password) {
        this.setPassword(password);
        return this;
    }

    public String getTenantCode() {
        var defaultTenantCode = "0";
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return defaultTenantCode;
        }
        return this.getTenants().stream().filter(TenantMemberResponse::getEnabled).findAny()
                .map(TenantMemberResponse::getTenantCode).orElse(defaultTenantCode);
    }

    @JsonGetter
    public String getTenantName() {
        var defaultTenantName = "默认租户";
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return defaultTenantName;
        }
        return this.getTenants().stream().filter(TenantMemberResponse::getEnabled).findAny()
                .map(TenantMemberResponse::getName).orElse(defaultTenantName);
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return !this.disabled;
    }
}