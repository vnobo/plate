package com.plate.boot.security;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.group.member.GroupMemberRes;
import com.plate.boot.security.core.tenant.member.TenantMemberRes;
import com.plate.boot.security.core.user.User;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;

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
    private UUID code;

    /**
     * Represents the username of the security principal.
     * This field stores the unique identifier for a user within the system, typically used for authentication and authorization purposes.
     */
    @JsonIgnore
    private String username;
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
     * Represents the nickname of a user within the security details.
     * This string holds the colloquial or display name associated with the user's account.
     */
    private String nickname;

    /**
     * A collection of {@link TenantMemberRes} objects representing the tenants associated with the security details.
     * Each {@code TenantMemberResponse} encapsulates information about a tenant member, including a unique identifier,
     * tenant code, user code, enabled status, and audit details provided by {@link UserAuditor}.
     * This set facilitates management and access to tenant-specific data for the authenticated user within a security context.
     */
    @JsonIgnore
    private Set<TenantMemberRes> tenants;

    /**
     * Represents a collection of {@link GroupMemberRes} instances associated with a security details object.
     * This set encapsulates the group membership responses for a user, providing information about the groups
     * the user is a part of, including each group's name and additional metadata in the form of a JSON node.
     */
    @JsonIgnore
    private Set<GroupMemberRes> groups;

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

    /**
     * Constructs a new SecurityDetails instance with the specified authorities, attributes, and name attribute key.
     *
     * @param authorities      the collection of granted authorities
     * @param attributes       the attributes associated with the user
     * @param nameAttributeKey the key used to access the user's name attribute
     */
    public SecurityDetails(Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
    }

    /**
     * Creates a new SecurityDetails instance with the specified code, authorities, attributes, and name attribute key.
     *
     * @param authorities the collection of granted authorities
     * @param user        the user information to populate the security details
     * @return a new SecurityDetails instance
     */
    public static SecurityDetails of(User user, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        SecurityDetails details = new SecurityDetails(authorities, attributes, "username");
        details.setCode(user.getCode());
        details.setUsername(user.getUsername());
        details.setPassword(user.getPassword());
        details.setNickname(user.getName());
        details.setAvatar(user.getAvatar());
        details.setBio(user.getBio());
        details.setDisabled(user.getDisabled());
        details.setAccountExpired(user.getAccountExpired());
        details.setAccountLocked(user.getAccountLocked());
        details.setCredentialsExpired(user.getCredentialsExpired());
        return details;
    }

    /**
     * Retrieves the tenant code associated with the security details.
     * If no tenants are associated, returns the default tenant code "0".
     *
     * @return the tenant code
     */
    public UUID getTenantCode() {
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return DEFAULT_UUID_CODE;
        }
        return this.getTenants().stream().filter(TenantMemberRes::getEnabled).findAny()
                .map(TenantMemberRes::getTenantCode).orElse(DEFAULT_UUID_CODE);
    }

    /**
     * Retrieves the tenant name associated with the security details.
     * If no tenants are associated, returns the default tenant name "Default Tenant".
     *
     * @return the tenant name
     */
    @JsonGetter
    public String getTenantName() {
        var defaultTenantName = "Default Tenant";
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return defaultTenantName;
        }
        return this.getTenants().stream().filter(TenantMemberRes::getEnabled).findAny()
                .map(TenantMemberRes::getName).orElse(defaultTenantName);
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        var authorities = super.getAuthorities();
        return authorities.stream().map(a ->
                new SimpleGrantedAuthority(a.getAuthority())).toList();
    }

    /**
     * Checks if the account is non-expired.
     *
     * @return true if the account is non-expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return !this.accountExpired;
    }

    /**
     * Checks if the account is non-locked.
     *
     * @return true if the account is non-locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return !this.accountLocked;
    }

    /**
     * Checks if the credentials are non-expired.
     *
     * @return true if the credentials are non-expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return !this.credentialsExpired;
    }

    /**
     * Checks if the account is enabled.
     *
     * @return true if the account is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return !this.disabled;
    }
}