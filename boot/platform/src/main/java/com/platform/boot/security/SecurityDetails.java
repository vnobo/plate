package com.platform.boot.security;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.boot.security.core.group.member.GroupMemberResponse;
import com.platform.boot.security.core.tenant.member.TenantMemberResponse;
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
 * @author Alex bob(<a href="https://github.com/vnobo">Alex bob</a>)
 */
@Setter
@Getter
public final class SecurityDetails extends DefaultOAuth2User implements UserDetails {

    private String code;

    private String username;

    private String nickname;

    private Set<TenantMemberResponse> tenants;

    private Set<GroupMemberResponse> groups;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private Boolean disabled;

    @JsonIgnore
    private Boolean accountExpired;

    @JsonIgnore
    private Boolean accountLocked;

    @JsonIgnore
    private Boolean credentialsExpired;

    public SecurityDetails(Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attributes, String nameAttributeKey) {
        super(authorities, attributes, nameAttributeKey);
    }

    public static SecurityDetails of(String code, String username, String nickname, String password, Boolean disabled,
                                     Boolean accountExpired, Boolean accountLocked, Boolean credentialsExpired,
                                     Collection<? extends GrantedAuthority> authorities,
                                     Map<String, Object> attributes, String nameAttributeKey) {
        SecurityDetails securityDetails = new SecurityDetails(authorities, attributes, nameAttributeKey);
        securityDetails.setCode(code);
        securityDetails.setUsername(username);
        securityDetails.setNickname(nickname);
        securityDetails.setPassword(password);
        securityDetails.setDisabled(disabled);
        securityDetails.setAccountExpired(accountExpired);
        securityDetails.setAccountLocked(accountLocked);
        securityDetails.setCredentialsExpired(credentialsExpired);
        return securityDetails;
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