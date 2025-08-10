package com.plate.boot.security.core.group.authority;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupAuthority Unit Tests
 *
 * <p>This test class provides unit tests for the GroupAuthority class, covering:</p>
 * <ul>
 *   <li>Group authority creation and initialization</li>
 *   <li>Group authority property access and modification</li>
 *   <li>GrantedAuthority implementation</li>
 *   <li>Validation constraints</li>
 * </ul>
 *
 * @author Qwen Code
 */
class GroupAuthorityTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create GroupAuthority with default constructor")
        void shouldCreateGroupAuthorityWithDefaultConstructor() {
            // When
            GroupAuthority groupAuthority = new GroupAuthority();

            // Then
            assertThat(groupAuthority).isNotNull();
            assertThat(groupAuthority.getGroupCode()).isNull();
            assertThat(groupAuthority.getAuthority()).isNull();
        }

        @Test
        @DisplayName("Should create GroupAuthority with parameterized constructor")
        void shouldCreateGroupAuthorityWithParameterizedConstructor() {
            // Given
            UUID groupCode = UUID.randomUUID();
            String authority = "ROLE_TEST_GROUP";

            // When
            GroupAuthority groupAuthority = new GroupAuthority(groupCode, authority);

            // Then
            assertThat(groupAuthority).isNotNull();
            assertThat(groupAuthority.getGroupCode()).isEqualTo(groupCode);
            assertThat(groupAuthority.getAuthority()).isEqualTo(authority);
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get group code")
        void shouldSetAndGetGroupCode() {
            // Given
            GroupAuthority groupAuthority = new GroupAuthority();
            UUID groupCode = UUID.randomUUID();

            // When
            groupAuthority.setGroupCode(groupCode);

            // Then
            assertThat(groupAuthority.getGroupCode()).isEqualTo(groupCode);
        }

        @Test
        @DisplayName("Should set and get authority")
        void shouldSetAndGetAuthority() {
            // Given
            GroupAuthority groupAuthority = new GroupAuthority();
            String authority = "ROLE_TEST_GROUP";

            // When
            groupAuthority.setAuthority(authority);

            // Then
            assertThat(groupAuthority.getAuthority()).isEqualTo(authority);
        }
    }

    @Nested
    @DisplayName("GrantedAuthority Implementation Tests")
    class GrantedAuthorityImplementationTests {

        @Test
        @DisplayName("Should implement GrantedAuthority interface")
        void shouldImplementGrantedAuthorityInterface() {
            // Given
            GroupAuthority groupAuthority = new GroupAuthority();

            // Then
            assertThat(groupAuthority).isInstanceOf(GrantedAuthority.class);
        }

        @Test
        @DisplayName("Should return authority from getAuthority method")
        void shouldReturnAuthorityFromGetAuthorityMethod() {
            // Given
            String authority = "ROLE_TEST_GROUP";
            GroupAuthority groupAuthority = new GroupAuthority(UUID.randomUUID(), authority);

            // When
            String result = groupAuthority.getAuthority();

            // Then
            assertThat(result).isEqualTo(authority);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid group authority properties")
        void shouldAcceptValidGroupAuthorityProperties() {
            // Given
            GroupAuthority groupAuthority = new GroupAuthority();
            UUID groupCode = UUID.randomUUID();
            String authority = "ROLE_TEST_GROUP";

            // When
            groupAuthority.setGroupCode(groupCode);
            groupAuthority.setAuthority(authority);

            // Then
            assertThat(groupAuthority.getGroupCode()).isEqualTo(groupCode);
            assertThat(groupAuthority.getAuthority()).isEqualTo(authority);
        }
    }
}