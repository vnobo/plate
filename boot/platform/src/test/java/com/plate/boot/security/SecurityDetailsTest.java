package com.plate.boot.security;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.tenant.member.TenantMemberRes;
import com.plate.boot.security.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link SecurityDetails}.
 * Tests the functionality of the SecurityDetails class, including its factory method,
 * tenant-related methods, and UserDetails interface implementation.
 */
@ExtendWith(MockitoExtension.class)
class SecurityDetailsTest {

    @Mock
    private User mockUser;
    private UUID userCode;
    private Collection<GrantedAuthority> authorities;
    private Map<String, Object> attributes;
    private SecurityDetails securityDetails;

    @BeforeEach
    void setUp() {
        userCode = UUID.randomUUID();
        authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        attributes = Map.of("username", "testuser", "email", "test@example.com");
        // Setup mock user
        when(mockUser.getCode()).thenReturn(userCode);
        when(mockUser.getUsername()).thenReturn("testuser");
        when(mockUser.getPassword()).thenReturn("encodedPassword");
        when(mockUser.getName()).thenReturn("Test User");
        when(mockUser.getAvatar()).thenReturn("avatar.jpg");
        when(mockUser.getBio()).thenReturn("Test bio");
        when(mockUser.getDisabled()).thenReturn(false);
        when(mockUser.getAccountExpired()).thenReturn(false);
        when(mockUser.getAccountLocked()).thenReturn(false);
        when(mockUser.getCredentialsExpired()).thenReturn(false);
        // Create security details instance
        securityDetails = SecurityDetails.of(mockUser, authorities, attributes);
    }

    @Test
    @DisplayName("Factory method 'of' should correctly create SecurityDetails from User")
    void testFactoryMethodOf() {
        // Assert
        assertThat(securityDetails).isNotNull();
        assertThat(securityDetails.getCode()).isEqualTo(userCode);
        assertThat(securityDetails.getUsername()).isEqualTo("testuser");
        assertThat(securityDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(securityDetails.getNickname()).isEqualTo("Test User");
        assertThat(securityDetails.getAvatar()).isEqualTo("avatar.jpg");
        assertThat(securityDetails.getBio()).isEqualTo("Test bio");
        assertThat(securityDetails.getDisabled()).isFalse();
        assertThat(securityDetails.getAccountExpired()).isFalse();
        assertThat(securityDetails.getAccountLocked()).isFalse();
        assertThat(securityDetails.getCredentialsExpired()).isFalse();
        assertThat(securityDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(securityDetails.getAttributes()).isEqualTo(attributes);
    }

    @Test
    @DisplayName("password() method should set password and return SecurityDetails instance")
    void testPasswordMethod() {
        // Act
        SecurityDetails result = securityDetails.password("newPassword");
        // Assert
        assertThat(result).isSameAs(securityDetails);
        assertThat(result.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("getTenantCode() should return default code when tenants is empty")
    void testGetTenantCodeWithEmptyTenants() {
        // Arrange
        securityDetails.setTenants(null);
        // Act & Assert
        assertThat(securityDetails.getTenantCode()).isEqualTo("0");
        // Arrange
        securityDetails.setTenants(Collections.emptySet());
        // Act & Assert
        assertThat(securityDetails.getTenantCode()).isEqualTo("0");
    }

    @Test
    @DisplayName("getTenantCode() should return first enabled tenant code")
    void testGetTenantCodeWithEnabledTenant() {
        // Arrange
        TenantMemberRes tenant1 = new TenantMemberRes();
        tenant1.setTenantCode("tenant1");
        tenant1.setName("Tenant 1");
        tenant1.setEnabled(false);

        TenantMemberRes tenant2 = new TenantMemberRes();
        tenant2.setTenantCode("tenant2");
        tenant2.setName("Tenant 2");
        tenant2.setEnabled(false);

        TenantMemberRes tenant3 = new TenantMemberRes();
        tenant3.setTenantCode("tenant3");
        tenant3.setName("Tenant 3");
        tenant3.setEnabled(true);

        securityDetails.setTenants(Set.of(tenant1, tenant2, tenant3));

        // Act
        String tenantCode = securityDetails.getTenantCode();

        // Assert
        assertThat(tenantCode).isEqualTo("tenant3");
    }

    @Test
    @DisplayName("getTenantCode() should return default when no enabled tenants")
    void testGetTenantCodeWithNoEnabledTenants() {
        // Arrange
        TenantMemberRes tenant1 = new TenantMemberRes();
        tenant1.setCode(ContextUtils.nextId()); // Added for completeness
        tenant1.setTenantCode("tenant1");
        tenant1.setEnabled(false);
        TenantMemberRes tenant2 = new TenantMemberRes();
        tenant2.setCode(ContextUtils.nextId()); // Added for completeness
        tenant2.setTenantCode("tenant2");
        tenant2.setEnabled(false);
        Set<TenantMemberRes> tenants = new HashSet<>();
        tenants.add(tenant1);
        tenants.add(tenant2);
        securityDetails.setTenants(tenants);
        // Act & Assert
        assertThat(securityDetails.getTenantCode()).isEqualTo("0");
    }

    @Test
    @DisplayName("getTenantName() should return default name when tenants is empty")
    void testGetTenantNameWithEmptyTenants() {
        // Arrange
        securityDetails.setTenants(null);
        // Act & Assert
        assertThat(securityDetails.getTenantName()).isEqualTo("Default Tenant");
        // Arrange
        securityDetails.setTenants(Collections.emptySet());
        // Act & Assert
        assertThat(securityDetails.getTenantName()).isEqualTo("Default Tenant");
    }

    @Test
    @DisplayName("getTenantName() should return first enabled tenant name")
    void testGetTenantNameWithEnabledTenant() {
        // Arrange
        TenantMemberRes tenant1 = new TenantMemberRes();
        tenant1.setCode(ContextUtils.nextId()); // Added for completeness
        tenant1.setName("Tenant 1");
        tenant1.setEnabled(false);
        TenantMemberRes tenant2 = new TenantMemberRes();
        tenant2.setCode(ContextUtils.nextId()); // Added for completeness
        tenant2.setName("Tenant 2");
        tenant2.setEnabled(true);
        TenantMemberRes tenant3 = new TenantMemberRes();
        tenant3.setCode(ContextUtils.nextId()); // Added for completeness
        tenant3.setName("Tenant 3");
        tenant3.setEnabled(true);
        securityDetails.setTenants(Set.of(tenant1, tenant2, tenant3));
        // Act
        String tenantName = securityDetails.getTenantName();
        // Assert
        // Since we're using a Set and findAny(), we can't guarantee which enabled tenant will be returned
        assertThat(tenantName).isIn("Tenant 2", "Tenant 3");
    }

    @Test
    @DisplayName("getTenantName() should return default when no enabled tenants")
    void testGetTenantNameWithNoEnabledTenants() {
        // Arrange
        TenantMemberRes tenant1 = new TenantMemberRes();
        tenant1.setCode(ContextUtils.nextId()); // Added for completeness
        tenant1.setName("Tenant 1");
        tenant1.setEnabled(false);
        TenantMemberRes tenant2 = new TenantMemberRes();
        tenant2.setCode(ContextUtils.nextId()); // Added for completeness
        tenant2.setName("Tenant 2");
        tenant2.setEnabled(false);
        securityDetails.setTenants(Set.of(tenant1, tenant2));
        // Act & Assert
        assertThat(securityDetails.getTenantName()).isEqualTo("Default Tenant");
    }

    @Test
    @DisplayName("isAccountNonExpired() should return the inverse of accountExpired")
    void testIsAccountNonExpired() {
        // Arrange & Act & Assert
        securityDetails.setAccountExpired(false);
        assertThat(securityDetails.isAccountNonExpired()).isTrue();
        securityDetails.setAccountExpired(true);
        assertThat(securityDetails.isAccountNonExpired()).isFalse();
    }

    @Test
    @DisplayName("isAccountNonLocked() should return the inverse of accountLocked")
    void testIsAccountNonLocked() {
        // Arrange & Act & Assert
        securityDetails.setAccountLocked(false);
        assertThat(securityDetails.isAccountNonLocked()).isTrue();
        securityDetails.setAccountLocked(true);
        assertThat(securityDetails.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("isCredentialsNonExpired() should return the inverse of credentialsExpired")
    void testIsCredentialsNonExpired() {
        // Arrange & Act & Assert
        securityDetails.setCredentialsExpired(false);
        assertThat(securityDetails.isCredentialsNonExpired()).isTrue();
        securityDetails.setCredentialsExpired(true);
        assertThat(securityDetails.isCredentialsNonExpired()).isFalse();
    }

    @Test
    @DisplayName("isEnabled() should return the inverse of disabled")
    void testIsEnabled() {
        // Arrange & Act & Assert
        securityDetails.setDisabled(false);
        assertThat(securityDetails.isEnabled()).isTrue();
        securityDetails.setDisabled(true);
        assertThat(securityDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Constructor should correctly initialize SecurityDetails")
    void testConstructor() {
        // Arrange
        Collection<? extends GrantedAuthority> testAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Map<String, Object> testAttributes = Map.of("username", "username");
        String nameAttributeKey = "username";
        // Act
        SecurityDetails details = new SecurityDetails(testAuthorities, testAttributes, nameAttributeKey);
        // Assert
        assertThat(details).isNotNull();

        // 验证权限集合
        assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        // 验证属性映射
        assertThat(details.getAttributes())
                .isNotNull()
                .hasSize(1)
                .containsEntry("username", "username");

        // 验证名称属性
        // 如果SecurityDetails扩展了DefaultOAuth2User，getName()应该返回正确值
        // 这里添加null检查并提供更明确的错误信息
        assertThat(details.getName())
                .withFailMessage("Name should be extracted from attributes using key: " + nameAttributeKey)
                .isEqualTo("username");
    }

    @Test
    @DisplayName("Getters and setters should work correctly")
    void testGettersAndSetters() {
        // Arrange
        UUID testCode = UUID.randomUUID();
        String testUsername = "newUsername";
        String testPassword = "newPassword";
        String testNickname = "newNickname";
        String testAvatar = "newAvatar.jpg";
        String testBio = "New bio";
        Boolean testDisabled = true;
        Boolean testAccountExpired = true;
        Boolean testAccountLocked = true;
        Boolean testCredentialsExpired = true;
        // Act
        securityDetails.setCode(testCode);
        securityDetails.setUsername(testUsername);
        securityDetails.setPassword(testPassword);
        securityDetails.setNickname(testNickname);
        securityDetails.setAvatar(testAvatar);
        securityDetails.setBio(testBio);
        securityDetails.setDisabled(testDisabled);
        securityDetails.setAccountExpired(testAccountExpired);
        securityDetails.setAccountLocked(testAccountLocked);
        securityDetails.setCredentialsExpired(testCredentialsExpired);
        // Assert
        assertThat(securityDetails.getCode()).isEqualTo(testCode);
        assertThat(securityDetails.getUsername()).isEqualTo(testUsername);
        assertThat(securityDetails.getPassword()).isEqualTo(testPassword);
        assertThat(securityDetails.getNickname()).isEqualTo(testNickname);
        assertThat(securityDetails.getAvatar()).isEqualTo(testAvatar);
        assertThat(securityDetails.getBio()).isEqualTo(testBio);
        assertThat(securityDetails.getDisabled()).isEqualTo(testDisabled);
        assertThat(securityDetails.getAccountExpired()).isEqualTo(testAccountExpired);
        assertThat(securityDetails.getAccountLocked()).isEqualTo(testAccountLocked);
        assertThat(securityDetails.getCredentialsExpired()).isEqualTo(testCredentialsExpired);
    }
}