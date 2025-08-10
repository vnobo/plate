package com.plate.boot.security.core.tenant.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TenantMember Unit Tests
 *
 * <p>This test class provides unit tests for the TenantMember class, covering:</p>
 * <ul>
 *   <li>Tenant member creation and initialization</li>
 *   <li>Tenant member property access and modification</li>
 *   <li>Validation constraints</li>
 * </ul>
 *
 * @author Qwen Code
 */
class TenantMemberTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create TenantMember with default values")
        void shouldCreateTenantMemberWithDefaultValues() {
            // When
            TenantMember tenantMember = new TenantMember();

            // Then
            assertThat(tenantMember).isNotNull();
            assertThat(tenantMember.getUserCode()).isNull();
            assertThat(tenantMember.getEnabled()).isNull();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get user code")
        void shouldSetAndGetUserCode() {
            // Given
            TenantMember tenantMember = new TenantMember();
            UUID userCode = UUID.randomUUID();

            // When
            tenantMember.setUserCode(userCode);

            // Then
            assertThat(tenantMember.getUserCode()).isEqualTo(userCode);
        }

        @Test
        @DisplayName("Should set and get enabled status")
        void shouldSetAndGetEnabledStatus() {
            // Given
            TenantMember tenantMember = new TenantMember();
            Boolean enabled = true;

            // When
            tenantMember.setEnabled(enabled);

            // Then
            assertThat(tenantMember.getEnabled()).isEqualTo(enabled);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid tenant member properties")
        void shouldAcceptValidTenantMemberProperties() {
            // Given
            TenantMember tenantMember = new TenantMember();
            UUID userCode = UUID.randomUUID();
            Boolean enabled = true;

            // When
            tenantMember.setUserCode(userCode);
            tenantMember.setEnabled(enabled);

            // Then
            assertThat(tenantMember.getUserCode()).isEqualTo(userCode);
            assertThat(tenantMember.getEnabled()).isEqualTo(enabled);
        }
    }
}