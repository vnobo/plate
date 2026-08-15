package com.plate.boot.security;

import com.plate.boot.security.core.tenant.member.TenantMemberRes;
import com.plate.boot.security.core.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SecurityDetails} (no Spring / container required).
 * Covers the static factory, tenant resolution, authority normalisation and
 * the account-status flags inherited from {@code UserDetails}.
 */
class SecurityDetailsTest {

    private static User user() {
        User user = new User();
        user.setCode(UUID.randomUUID());
        user.setUsername("admin");
        user.setPassword("{bcrypt}secret");
        user.setName("Admin");
        user.setAvatar("avatar.png");
        user.setBio("bio");
        user.setDisabled(false);
        user.setAccountExpired(false);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
        return user;
    }

    private static SecurityDetails details() {
        return new SecurityDetails(List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("username", "admin"), "username");
    }

    @Test
    void ofCopiesUserFields() {
        User user = user();

        SecurityDetails details = SecurityDetails.of(user,
                List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("username", "admin"));

        assertThat(details.getCode()).isEqualTo(user.getCode());
        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("{bcrypt}secret");
        assertThat(details.getNickname()).isEqualTo("Admin");
        assertThat(details.getAvatar()).isEqualTo("avatar.png");
        assertThat(details.getBio()).isEqualTo("bio");
        assertThat(details.getDisabled()).isFalse();
        assertThat(details.getAccountExpired()).isFalse();
        assertThat(details.getAccountLocked()).isFalse();
        assertThat(details.getCredentialsExpired()).isFalse();
    }

    @Test
    void tenantCodeDefaultsWhenNoTenants() {
        assertThat(details().getTenantCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void tenantCodeReturnsFirstEnabledTenant() {
        SecurityDetails details = details();
        UUID tenantCode = UUID.randomUUID();
        TenantMemberRes tenant = new TenantMemberRes();
        tenant.setTenantCode(tenantCode);
        tenant.setEnabled(true);
        tenant.setName("Tenant A");
        details.setTenants(Set.of(tenant));

        assertThat(details.getTenantCode()).isEqualTo(tenantCode);
    }

    @Test
    void tenantCodeDefaultsWhenAllTenantsDisabled() {
        SecurityDetails details = details();
        TenantMemberRes tenant = new TenantMemberRes();
        tenant.setTenantCode(UUID.randomUUID());
        tenant.setEnabled(false);
        details.setTenants(Set.of(tenant));

        assertThat(details.getTenantCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void tenantNameDefaultsWhenNoTenants() {
        assertThat(details().getTenantName()).isEqualTo("Default Tenant");
    }

    @Test
    void tenantNameReturnsFirstEnabledTenantName() {
        SecurityDetails details = details();
        TenantMemberRes tenant = new TenantMemberRes();
        tenant.setEnabled(true);
        tenant.setName("Tenant A");
        details.setTenants(Set.of(tenant));

        assertThat(details.getTenantName()).isEqualTo("Tenant A");
    }

    @Test
    void tenantNameDefaultsWhenAllTenantsDisabled() {
        SecurityDetails details = details();
        TenantMemberRes tenant = new TenantMemberRes();
        tenant.setEnabled(false);
        tenant.setName("Tenant A");
        details.setTenants(Set.of(tenant));

        assertThat(details.getTenantName()).isEqualTo("Default Tenant");
    }

    @Test
    void authoritiesAreNormalisedToSimpleGrantedAuthority() {
        Collection<? extends GrantedAuthority> authorities = details().getAuthorities();

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void accountStatusFlagsNegateTheirUnderlyingBooleans() {
        SecurityDetails details = details();
        details.setDisabled(false);
        details.setAccountExpired(false);
        details.setAccountLocked(false);
        details.setCredentialsExpired(false);

        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();

        details.setDisabled(true);
        details.setAccountExpired(true);
        details.setAccountLocked(true);
        details.setCredentialsExpired(true);

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonExpired()).isFalse();
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isCredentialsNonExpired()).isFalse();
    }
}
