package com.plate.boot.security;

import com.plate.boot.security.core.tenant.member.TenantMemberRes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityDetails Unit Tests
 *
 * <p>This test class provides unit tests for the SecurityDetails class, covering:</p>
 * <ul>
 *   <li>Creation and initialization of SecurityDetails</li>
 *   <li>User details implementation methods</li>
 *   <li>Tenant and group management</li>
 *   <li>Password handling</li>
 * </ul>
 *
 * @author Qwen Code
 */
class SecurityDetailsTest {

    private Collection<GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private User user;

    @BeforeEach
    void setUp() {
        authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        attributes = Map.of(
            "username", "testuser",
            "email", "test@example.com"
        );
        
        user = new User();
        user.setCode(UUID.randomUUID());
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setName("Test User");
        user.setAvatar("avatar-url");
        user.setBio("Test bio");
        user.setDisabled(false);
        user.setAccountExpired(false);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
    }

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create SecurityDetails with of method")
        void shouldCreateSecurityDetailsWithOfMethod() {
            // When
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails).isNotNull();
            assertThat(securityDetails.getCode()).isEqualTo(user.getCode());
            assertThat(securityDetails.getUsername()).isEqualTo(user.getUsername());
            assertThat(securityDetails.getPassword()).isEqualTo(user.getPassword());
            assertThat(securityDetails.getNickname()).isEqualTo(user.getName());
            assertThat(securityDetails.getAvatar()).isEqualTo(user.getAvatar());
            assertThat(securityDetails.getBio()).isEqualTo(user.getBio());
            assertThat(securityDetails.isAccountNonExpired()).isTrue();
            assertThat(securityDetails.isAccountNonLocked()).isTrue();
            assertThat(securityDetails.isCredentialsNonExpired()).isTrue();
            assertThat(securityDetails.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should create SecurityDetails with constructor")
        void shouldCreateSecurityDetailsWithConstructor() {
            // When
            SecurityDetails securityDetails = new SecurityDetails(authorities, attributes, "username");

            // Then
            assertThat(securityDetails).isNotNull();
            assertThat(securityDetails.getAuthorities()).hasSize(authorities.size());
            assertThat(securityDetails.getAttributes()).containsAllEntriesOf(attributes);
        }
    }

    @Nested
    @DisplayName("User Details Implementation Tests")
    class UserDetailsImplementationTests {

        @Test
        @DisplayName("Should return correct account expiration status")
        void shouldReturnCorrectAccountExpirationStatus() {
            // Given
            user.setAccountExpired(true);
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails.isAccountNonExpired()).isFalse();
            
            // When - change to non-expired
            user.setAccountExpired(false);
            securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            // Then
            assertThat(securityDetails.isAccountNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Should return correct account lock status")
        void shouldReturnCorrectAccountLockStatus() {
            // Given
            user.setAccountLocked(true);
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails.isAccountNonLocked()).isFalse();
            
            // When - change to non-locked
            user.setAccountLocked(false);
            securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            // Then
            assertThat(securityDetails.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName("Should return correct credentials expiration status")
        void shouldReturnCorrectCredentialsExpirationStatus() {
            // Given
            user.setCredentialsExpired(true);
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails.isCredentialsNonExpired()).isFalse();
            
            // When - change to non-expired
            user.setCredentialsExpired(false);
            securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            // Then
            assertThat(securityDetails.isCredentialsNonExpired()).isTrue();
        }

        @Test
        @DisplayName("Should return correct account enabled status")
        void shouldReturnCorrectAccountEnabledStatus() {
            // Given
            user.setDisabled(true);
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails.isEnabled()).isFalse();
            
            // When - change to enabled
            user.setDisabled(false);
            securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            // Then
            assertThat(securityDetails.isEnabled()).isTrue();
        }
    }

    @Nested
    @DisplayName("Tenant and Group Tests")
    class TenantAndGroupTests {

        @Test
        @DisplayName("Should return default tenant code when no tenants")
        void shouldReturnDefaultTenantCodeWhenNoTenants() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            securityDetails.setTenants(null);

            // When
            String tenantCode = securityDetails.getTenantCode();

            // Then
            assertThat(tenantCode).isEqualTo("0");
        }

        @Test
        @DisplayName("Should return default tenant name when no tenants")
        void shouldReturnDefaultTenantNameWhenNoTenants() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            securityDetails.setTenants(null);

            // When
            String tenantName = securityDetails.getTenantName();

            // Then
            assertThat(tenantName).isEqualTo("Default Tenant");
        }

        @Test
        @DisplayName("Should return enabled tenant code when tenants exist")
        void shouldReturnEnabledTenantCodeWhenTenantsExist() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            TenantMemberRes tenantMember = new TenantMemberRes();
            tenantMember.setEnabled(true);
            tenantMember.setTenantCode("tenant-123");
            
            Set<TenantMemberRes> tenants = new HashSet<>();
            tenants.add(tenantMember);
            securityDetails.setTenants(tenants);

            // When
            String tenantCode = securityDetails.getTenantCode();

            // Then
            assertThat(tenantCode).isEqualTo("tenant-123");
        }

        @Test
        @DisplayName("Should return enabled tenant name when tenants exist")
        void shouldReturnEnabledTenantNameWhenTenantsExist() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            TenantMemberRes tenantMember = new TenantMemberRes();
            tenantMember.setEnabled(true);
            tenantMember.setName("Test Tenant");
            
            Set<TenantMemberRes> tenants = new HashSet<>();
            tenants.add(tenantMember);
            securityDetails.setTenants(tenants);

            // When
            String tenantName = securityDetails.getTenantName();

            // Then
            assertThat(tenantName).isEqualTo("Test Tenant");
        }

        @Test
        @DisplayName("Should return default when no enabled tenants")
        void shouldReturnDefaultWhenNoEnabledTenants() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            TenantMemberRes tenantMember = new TenantMemberRes();
            tenantMember.setEnabled(false);
            tenantMember.setTenantCode("tenant-123");
            tenantMember.setName("Test Tenant");
            
            Set<TenantMemberRes> tenants = new HashSet<>();
            tenants.add(tenantMember);
            securityDetails.setTenants(tenants);

            // When
            String tenantCode = securityDetails.getTenantCode();
            String tenantName = securityDetails.getTenantName();

            // Then
            assertThat(tenantCode).isEqualTo("0");
            assertThat(tenantName).isEqualTo("Default Tenant");
        }
    }

    @Nested
    @DisplayName("Password Handling Tests")
    class PasswordHandlingTests {

        @Test
        @DisplayName("Should set password correctly")
        void shouldSetPasswordCorrectly() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            String newPassword = "newEncodedPassword";

            // When
            SecurityDetails updatedDetails = securityDetails.password(newPassword);

            // Then
            assertThat(updatedDetails.getPassword()).isEqualTo(newPassword);
            assertThat(updatedDetails).isSameAs(securityDetails); // Fluent API
        }
    }

    @Nested
    @DisplayName("OAuth2 User Implementation Tests")
    class OAuth2UserImplementationTests {

        @Test
        @DisplayName("Should return correct name attribute")
        void shouldReturnCorrectNameAttribute() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // When
            String name = securityDetails.getName();

            // Then
            assertThat(name).isEqualTo("testuser");
        }

        @Test
        @DisplayName("Should return correct attributes")
        void shouldReturnCorrectAttributes() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // When
            Map<String, Object> returnedAttributes = securityDetails.getAttributes();

            // Then
            assertThat(returnedAttributes).containsAllEntriesOf(attributes);
        }

        @Test
        @DisplayName("Should return correct authorities")
        void shouldReturnCorrectAuthorities() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // When
            Collection<? extends GrantedAuthority> returnedAuthorities = securityDetails.getAuthorities();

            // Then
            assertThat(returnedAuthorities).hasSize(authorities.size());
        }
    }
}