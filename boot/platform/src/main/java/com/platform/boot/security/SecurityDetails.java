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


    /**
     * Creates a {@link SecurityDetails} instance with the specified parameters.
     *
     * @param username           the username
     * @param password           the password
     * @param disabled           whether the account is disabled or not
     * @param accountExpired     whether the account has expired or not
     * @param accountLocked      whether the account is locked or not
     * @param credentialsExpired whether the credentials have expired or not
     * @return a {@link SecurityDetails} instance
     */
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

    /**
     * Set the authorities of the user
     *
     * @param authorities the authorities of the user
     * @return the updated {@link SecurityDetails} instance with a set of authorities
     */
    public SecurityDetails authorities(Set<GrantedAuthority> authorities) {
        this.setAuthorities(Collections.unmodifiableSet(sortAuthorities(authorities)));
        return this;
    }

    public SecurityDetails password(String password) {
        this.setPassword(password);
        return this;
    }

    /**
     * Helper method to sort the given collection of GrantedAuthority objects
     *
     * @param authorities the collection of GrantedAuthority objects
     * @return a sorted set of GrantedAuthority objects
     */
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

    /**
     * Get the tenant code of the user
     *
     * @return the tenant code of the user
     */
    public String getTenantCode() {
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return null;
        }
        return this.getTenants().stream().filter(TenantMemberResponse::getEnabled).findAny()
                .map(TenantMemberResponse::getTenantCode).orElse(null);
    }

    /**
     * Get the tenant name of the user
     *
     * @return the tenant name of the user
     */
    @JsonGetter
    public String getTenantName() {
        if (ObjectUtils.isEmpty(this.getTenants())) {
            return null;
        }
        return this.getTenants().stream().filter(TenantMemberResponse::getEnabled).findAny()
                .map(TenantMemberResponse::getTenantName).orElse(null);
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

    /**
     * Comparator for comparing GrantedAuthority objects
     */
    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {
        @Override
        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            // Neither should ever be null as each entry is checked before adding it to
            // the set. If the authority is null, it is a custom authority and should
            // precede others.
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