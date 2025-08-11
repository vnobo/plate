package com.plate.boot.security;

import com.plate.boot.security.core.group.member.GroupMemberRes;
import com.plate.boot.security.core.tenant.member.TenantMemberRes;
import com.plate.boot.security.core.user.User;

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
 *   <li>Edge cases and null handling</li>
 *   <li>Boolean field combinations</li>
 *   <li>Interface compliance</li>
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

    @Nested
    @DisplayName("Edge Cases and Null Handling Tests")
    class EdgeCasesAndNullHandlingTests {

        @Test
        @DisplayName("Should handle null user fields gracefully")
        void shouldHandleNullUserFieldsGracefully() {
            // Given
            User userWithNulls = new User();
            userWithNulls.setCode(UUID.randomUUID());
            userWithNulls.setUsername("testuser");
            userWithNulls.setPassword("password");
            // Leave other fields as null

            // When
            SecurityDetails securityDetails = SecurityDetails.of(userWithNulls, authorities, attributes);

            // Then
            assertThat(securityDetails.getNickname()).isNull();
            assertThat(securityDetails.getAvatar()).isNull();
            assertThat(securityDetails.getBio()).isNull();
            assertThat(securityDetails.isEnabled()).isTrue(); // null disabled should be treated as enabled
            assertThat(securityDetails.isAccountNonExpired()).isTrue(); // null expired should be treated as non-expired
            assertThat(securityDetails.isAccountNonLocked()).isTrue(); // null locked should be treated as non-locked
            assertThat(securityDetails.isCredentialsNonExpired()).isTrue(); // null credentials expired should be treated as non-expired
        }

        @Test
        @DisplayName("Should handle empty authorities collection")
        void shouldHandleEmptyAuthoritiesCollection() {
            // Given
            Collection<GrantedAuthority> emptyAuthorities = Collections.emptyList();

            // When
            SecurityDetails securityDetails = SecurityDetails.of(user, emptyAuthorities, attributes);

            // Then
            assertThat(securityDetails.getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty attributes map")
        void shouldHandleEmptyAttributesMap() {
            // Given
            Map<String, Object> emptyAttributes = Collections.emptyMap();

            // When
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, emptyAttributes);

            // Then
            assertThat(securityDetails.getAttributes()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty tenants set")
        void shouldHandleEmptyTenantsSet() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            securityDetails.setTenants(Collections.emptySet());

            // When
            String tenantCode = securityDetails.getTenantCode();
            String tenantName = securityDetails.getTenantName();

            // Then
            assertThat(tenantCode).isEqualTo("0");
            assertThat(tenantName).isEqualTo("Default Tenant");
        }

        @Test
        @DisplayName("Should handle multiple tenants with only one enabled")
        void shouldHandleMultipleTenantsWithOnlyOneEnabled() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            TenantMemberRes disabledTenant = new TenantMemberRes();
            disabledTenant.setEnabled(false);
            disabledTenant.setTenantCode("disabled-tenant");
            disabledTenant.setName("Disabled Tenant");
            
            TenantMemberRes enabledTenant = new TenantMemberRes();
            enabledTenant.setEnabled(true);
            enabledTenant.setTenantCode("enabled-tenant");
            enabledTenant.setName("Enabled Tenant");
            
            Set<TenantMemberRes> tenants = new HashSet<>();
            tenants.add(disabledTenant);
            tenants.add(enabledTenant);
            securityDetails.setTenants(tenants);

            // When
            String tenantCode = securityDetails.getTenantCode();
            String tenantName = securityDetails.getTenantName();

            // Then
            assertThat(tenantCode).isEqualTo("enabled-tenant");
            assertThat(tenantName).isEqualTo("Enabled Tenant");
        }

        @Test
        @DisplayName("Should handle tenant with null name")
        void shouldHandleTenantWithNullName() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            TenantMemberRes tenantWithNullName = new TenantMemberRes();
            tenantWithNullName.setEnabled(true);
            tenantWithNullName.setTenantCode("tenant-123");
            tenantWithNullName.setName(null);
            
            Set<TenantMemberRes> tenants = new HashSet<>();
            tenants.add(tenantWithNullName);
            securityDetails.setTenants(tenants);

            // When
            String tenantName = securityDetails.getTenantName();

            // Then
            assertThat(tenantName).isNull();
        }

        @Test
        @DisplayName("Should handle tenant with null code")
        void shouldHandleTenantWithNullCode() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            TenantMemberRes tenantWithNullCode = new TenantMemberRes();
            tenantWithNullCode.setEnabled(true);
            tenantWithNullCode.setTenantCode(null);
            tenantWithNullCode.setName("Test Tenant");
            
            Set<TenantMemberRes> tenants = new HashSet<>();
            tenants.add(tenantWithNullCode);
            securityDetails.setTenants(tenants);

            // When
            String tenantCode = securityDetails.getTenantCode();

            // Then
            assertThat(tenantCode).isNull();
        }
    }

    @Nested
    @DisplayName("Groups Management Tests")
    class GroupsManagementTests {

        @Test
        @DisplayName("Should handle null groups")
        void shouldHandleNullGroups() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            securityDetails.setGroups(null);

            // When & Then
            assertThat(securityDetails.getGroups()).isNull();
        }

        @Test
        @DisplayName("Should handle empty groups set")
        void shouldHandleEmptyGroupsSet() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            securityDetails.setGroups(Collections.emptySet());

            // When & Then
            assertThat(securityDetails.getGroups()).isEmpty();
        }

        @Test
        @DisplayName("Should set and get groups correctly")
        void shouldSetAndGetGroupsCorrectly() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            
            Set<GroupMemberRes> groups = new HashSet<>();
            GroupMemberRes group1 = new GroupMemberRes();
            group1.setName("Admin Group");
            
            GroupMemberRes group2 = new GroupMemberRes();
            group2.setName("User Group");
            
            groups.add(group1);
            groups.add(group2);

            // When
            securityDetails.setGroups(groups);

            // Then
            assertThat(securityDetails.getGroups()).hasSize(2);
            assertThat(securityDetails.getGroups()).containsExactlyInAnyOrder(group1, group2);
        }
    }

    @Nested
    @DisplayName("Fluent API Tests")
    class FluentApiTests {

        @Test
        @DisplayName("Should support method chaining with password")
        void shouldSupportMethodChainingWithPassword() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            String newPassword = "newPassword123";

            // When
            SecurityDetails result = securityDetails.password(newPassword);

            // Then
            assertThat(result).isSameAs(securityDetails);
            assertThat(result.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("Should allow multiple password changes")
        void shouldAllowMultiplePasswordChanges() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);
            String firstPassword = "firstPassword123";
            String secondPassword = "secondPassword456";

            // When
            securityDetails.password(firstPassword).password(secondPassword);

            // Then
            assertThat(securityDetails.getPassword()).isEqualTo(secondPassword);
        }
    }

    @Nested
    @DisplayName("Boolean Field Tests")
    class BooleanFieldTests {

        @Test
        @DisplayName("Should handle all boolean combinations correctly")
        void shouldHandleAllBooleanCombinationsCorrectly() {
            // Test all combinations of boolean flags
            boolean[] booleanValues = {true, false};
            
            for (boolean disabled : booleanValues) {
                for (boolean accountExpired : booleanValues) {
                    for (boolean accountLocked : booleanValues) {
                        for (boolean credentialsExpired : booleanValues) {
                            // Given
                            User testUser = new User();
                            testUser.setCode(UUID.randomUUID());
                            testUser.setUsername("testuser");
                            testUser.setPassword("password");
                            testUser.setDisabled(disabled);
                            testUser.setAccountExpired(accountExpired);
                            testUser.setAccountLocked(accountLocked);
                            testUser.setCredentialsExpired(credentialsExpired);

                            // When
                            SecurityDetails securityDetails = SecurityDetails.of(testUser, authorities, attributes);

                            // Then
                            assertThat(securityDetails.isEnabled()).isEqualTo(!disabled);
                            assertThat(securityDetails.isAccountNonExpired()).isEqualTo(!accountExpired);
                            assertThat(securityDetails.isAccountNonLocked()).isEqualTo(!accountLocked);
                            assertThat(securityDetails.isCredentialsNonExpired()).isEqualTo(!credentialsExpired);
                        }
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Inheritance and Interface Tests")
    class InheritanceAndInterfaceTests {

        @Test
        @DisplayName("Should implement UserDetails interface correctly")
        void shouldImplementUserDetailsInterfaceCorrectly() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails).isInstanceOf(org.springframework.security.core.userdetails.UserDetails.class);
            
            // Verify UserDetails methods
            assertThat(securityDetails.getUsername()).isNotNull();
            assertThat(securityDetails.getPassword()).isNotNull();
            assertThat(securityDetails.getAuthorities()).isNotNull();
            assertThat(securityDetails.isAccountNonExpired()).isNotNull();
            assertThat(securityDetails.isAccountNonLocked()).isNotNull();
            assertThat(securityDetails.isCredentialsNonExpired()).isNotNull();
            assertThat(securityDetails.isEnabled()).isNotNull();
        }

        @Test
        @DisplayName("Should extend DefaultOAuth2User correctly")
        void shouldExtendDefaultOAuth2UserCorrectly() {
            // Given
            SecurityDetails securityDetails = SecurityDetails.of(user, authorities, attributes);

            // Then
            assertThat(securityDetails).isInstanceOf(org.springframework.security.oauth2.core.user.DefaultOAuth2User.class);
            
            // Verify OAuth2User methods
            assertThat(securityDetails.getName()).isNotNull();
            assertThat(securityDetails.getAttributes()).isNotNull();
            assertThat(securityDetails.getAuthorities()).isNotNull();
        }
    }
}