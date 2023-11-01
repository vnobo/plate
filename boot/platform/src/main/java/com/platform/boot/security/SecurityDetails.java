package com.platform.boot.security;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.boot.security.group.member.GroupMemberResponse;
import com.platform.boot.security.tenant.member.TenantMemberResponse;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a SecurityDetails object that implements UserDetails and holds
 * user-related information such as username, password, and granted authorities.
 *
 * @author Alex bob(<a href="https://github.com/vnobo">Alex bob</a>)
 */
@Data
public final class SecurityDetails implements UserDetails {

    private String code;

    private String username;

    private String name;

    private Set<GrantedAuthority> authorities;

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

    public static SecurityDetails of(String code, String username, String name, String password, Boolean disabled,
                                     Boolean accountExpired, Boolean accountLocked, Boolean credentialsExpired) {
        SecurityDetails securityDetails = new SecurityDetails();
        securityDetails.setCode(code);
        securityDetails.setUsername(username);
        securityDetails.setName(name);
        securityDetails.setPassword(password);
        securityDetails.setDisabled(disabled);
        securityDetails.setAccountExpired(accountExpired);
        securityDetails.setAccountLocked(accountLocked);
        securityDetails.setCredentialsExpired(credentialsExpired);
        return securityDetails;
    }

    public SecurityDetails authorities(Set<GrantedAuthority> authorities) {
        this.setAuthorities(Collections.unmodifiableSet(sortAuthorities(authorities)));
        return this;
    }

    public SecurityDetails password(String password) {
        this.setPassword(password);
        return this;
    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
        // Ensure array iteration order is predictable (as per
        // UserDetails.getAuthorities() contract and SEC-717)
        SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(new AuthorityComparator());
        for (GrantedAuthority grantedAuthority : authorities) {
            Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
            sortedAuthorities.add(grantedAuthority);
        }
        return sortedAuthorities;
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

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {
        @Override
        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            if (g2.getAuthority() == null) {
                return -1;
            }
            if (g1.getAuthority() == null) {
                return 1;
            }
            return g1.getAuthority().compareTo(g2.getAuthority());
        }
    }
}