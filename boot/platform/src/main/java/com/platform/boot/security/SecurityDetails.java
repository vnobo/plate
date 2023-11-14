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

    /**
     * 根据给定参数创建一个SecurityDetails对象
     *
     * @param code               唯一标识码
     * @param username           用户名
     * @param name               昵称
     * @param password           密码
     * @param disabled           禁用标志
     * @param accountExpired     账号过期标志
     * @param accountLocked      账号被锁定标志
     * @param credentialsExpired 密码过期标志
     * @param authorities        授权信息集合
     * @param attributes         属性集合
     * @return 创建的SecurityDetails对象
     */
    public static SecurityDetails of(String code, String username, String nickname, String password, Boolean disabled,
                                     Boolean accountExpired, Boolean accountLocked, Boolean credentialsExpired,
                                     Collection<? extends GrantedAuthority> authorities,
                                     Map<String, Object> attributes) {
        SecurityDetails securityDetails = new SecurityDetails(authorities, attributes, username);
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
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return null;
        }
        return this.getTenants().stream().filter(TenantMemberResponse::getEnabled).findAny()
                .map(TenantMemberResponse::getTenantCode).orElse(null);
    }

    @JsonGetter
    public String getTenantName() {
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return null;
        }
        return this.getTenants().stream().filter(TenantMemberResponse::getEnabled).findAny()
                .map(TenantMemberResponse::getName).orElse(null);
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