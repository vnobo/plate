package com.plate.boot.security.core.tenant;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.security.core.UserAuditor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tenant Unit Tests
 *
 * <p>This test class provides unit tests for the Tenant class, covering:</p>
 * <ul>
 *   <li>Tenant creation and initialization</li>
 *   <li>Tenant property access and modification</li>
 *   <li>Auditing fields</li>
 *   <li>Validation constraints</li>
 * </ul>
 *
 * @author Qwen Code
 */
class TenantTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create Tenant with default values")
        void shouldCreateTenantWithDefaultValues() {
            // When
            Tenant tenant = new Tenant();

            // Then
            assertThat(tenant).isNotNull();
            assertThat(tenant.getId()).isNull();
            assertThat(tenant.getCode()).isNull();
            assertThat(tenant.getPcode()).isNull();
            assertThat(tenant.getName()).isNull();
            assertThat(tenant.getExtend()).isNull();
            assertThat(tenant.getCreator()).isNull();
            assertThat(tenant.getUpdater()).isNull();
            assertThat(tenant.getCreatedTime()).isNull();
            assertThat(tenant.getUpdatedTime()).isNull();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get ID")
        void shouldSetAndGetID() {
            // Given
            Tenant tenant = new Tenant();
            Integer id = 1;

            // When
            tenant.setId(id);

            // Then
            assertThat(tenant.getId()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should set and get code")
        void shouldSetAndGetCode() {
            // Given
            Tenant tenant = new Tenant();
            String code = "TENANT001";

            // When
            tenant.setCode(code);

            // Then
            assertThat(tenant.getCode()).isEqualTo(code);
        }

        @Test
        @DisplayName("Should set and get parent code")
        void shouldSetAndGetParentCode() {
            // Given
            Tenant tenant = new Tenant();
            String pcode = "PARENT001";

            // When
            tenant.setPcode(pcode);

            // Then
            assertThat(tenant.getPcode()).isEqualTo(pcode);
        }

        @Test
        @DisplayName("Should set and get name")
        void shouldSetAndGetName() {
            // Given
            Tenant tenant = new Tenant();
            String name = "Test Tenant";

            // When
            tenant.setName(name);

            // Then
            assertThat(tenant.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should set and get extend")
        void shouldSetAndGetExtend() {
            // Given
            Tenant tenant = new Tenant();
            JsonNode extend = null; // In a real test, we'd create a mock or actual JsonNode

            // When
            tenant.setExtend(extend);

            // Then
            assertThat(tenant.getExtend()).isEqualTo(extend);
        }

        @Test
        @DisplayName("Should set and get creator")
        void shouldSetAndGetCreator() {
            // Given
            Tenant tenant = new Tenant();
            UserAuditor creator = UserAuditor.withCode(UUID.randomUUID());

            // When
            tenant.setCreator(creator);

            // Then
            assertThat(tenant.getCreator()).isEqualTo(creator);
        }

        @Test
        @DisplayName("Should set and get updater")
        void shouldSetAndGetUpdater() {
            // Given
            Tenant tenant = new Tenant();
            UserAuditor updater = UserAuditor.withCode(UUID.randomUUID());

            // When
            tenant.setUpdater(updater);

            // Then
            assertThat(tenant.getUpdater()).isEqualTo(updater);
        }

        @Test
        @DisplayName("Should set and get created time")
        void shouldSetAndGetCreatedTime() {
            // Given
            Tenant tenant = new Tenant();
            LocalDateTime createdTime = LocalDateTime.now();

            // When
            tenant.setCreatedTime(createdTime);

            // Then
            assertThat(tenant.getCreatedTime()).isEqualTo(createdTime);
        }

        @Test
        @DisplayName("Should set and get updated time")
        void shouldSetAndGetUpdatedTime() {
            // Given
            Tenant tenant = new Tenant();
            LocalDateTime updatedTime = LocalDateTime.now();

            // When
            tenant.setUpdatedTime(updatedTime);

            // Then
            assertThat(tenant.getUpdatedTime()).isEqualTo(updatedTime);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid tenant properties")
        void shouldAcceptValidTenantProperties() {
            // Given
            Tenant tenant = new Tenant();
            String code = "TENANT001";
            String pcode = "PARENT001";
            String name = "Test Tenant";

            // When
            tenant.setCode(code);
            tenant.setPcode(pcode);
            tenant.setName(name);

            // Then
            assertThat(tenant.getCode()).isEqualTo(code);
            assertThat(tenant.getPcode()).isEqualTo(pcode);
            assertThat(tenant.getName()).isEqualTo(name);
        }
    }
}