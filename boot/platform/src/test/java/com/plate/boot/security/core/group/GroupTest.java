package com.plate.boot.security.core.group;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Group Unit Tests
 *
 * <p>This test class provides unit tests for the Group class, covering:</p>
 * <ul>
 *   <li>Group creation and initialization</li>
 *   <li>Group property access and modification</li>
 *   <li>Validation constraints</li>
 * </ul>
 *
 * @author Qwen Code
 */
class GroupTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create Group with default values")
        void shouldCreateGroupWithDefaultValues() {
            // When
            Group group = new Group();

            // Then
            assertThat(group).isNotNull();
            assertThat(group.getName()).isNull();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get name")
        void shouldSetAndGetName() {
            // Given
            Group group = new Group();
            String name = "Test Group";

            // When
            group.setName(name);

            // Then
            assertThat(group.getName()).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid group name")
        void shouldAcceptValidGroupName() {
            // Given
            Group group = new Group();
            String name = "Valid Group Name";

            // When
            group.setName(name);

            // Then
            assertThat(group.getName()).isEqualTo(name);
        }
    }
}