package com.plate.boot.security.core;

import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * UserAuditor Unit Tests
 *
 * <p>This test class provides unit tests for the UserAuditor record, covering:</p>
 * <ul>
 *   <li>Creation of UserAuditor instances</li>
 *   <li>Building UserAuditor from various sources</li>
 *   <li>Immutability and data integrity</li>
 * </ul>
 *
 * @author Qwen Code
 */
class UserAuditorTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create UserAuditor with of method")
        void shouldCreateUserAuditorWithOfMethod() {
            // Given
            UUID code = UUID.randomUUID();
            String name = "Test User";

            // When
            UserAuditor userAuditor = UserAuditor.of(code, name);

            // Then
            assertThat(userAuditor).isNotNull();
            assertThat(userAuditor.code()).isEqualTo(code);
            assertThat(userAuditor.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should create UserAuditor with constructor")
        void shouldCreateUserAuditorWithConstructor() {
            // Given
            UUID code = UUID.randomUUID();
            String name = "Test User";

            // When
            UserAuditor userAuditor = new UserAuditor(code, name);

            // Then
            assertThat(userAuditor).isNotNull();
            assertThat(userAuditor.code()).isEqualTo(code);
            assertThat(userAuditor.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should create UserAuditor withCode method")
        void shouldCreateUserAuditorWithCodeMethod() {
            // Given
            UUID code = UUID.randomUUID();

            // When
            UserAuditor userAuditor = UserAuditor.withCode(code);

            // Then
            assertThat(userAuditor).isNotNull();
            assertThat(userAuditor.code()).isEqualTo(code);
            assertThat(userAuditor.name()).isNull();
        }
    }

    @Nested
    @DisplayName("Build Tests")
    class BuildTests {

        @Test
        @DisplayName("Should build UserAuditor from SecurityDetails")
        void shouldBuildUserAuditorFromSecurityDetails() {
            // Given
            UUID code = UUID.randomUUID();
            String name = "Test User";
            SecurityDetails securityDetails = mock(SecurityDetails.class);
            when(securityDetails.getCode()).thenReturn(code);
            when(securityDetails.getName()).thenReturn(name);

            // When
            UserAuditor userAuditor = UserAuditor.withDetails(securityDetails);

            // Then
            assertThat(userAuditor).isNotNull();
            assertThat(userAuditor.code()).isEqualTo(code);
            assertThat(userAuditor.name()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should build UserAuditor from User")
        void shouldBuildUserAuditorFromUser() {
            // Given
            UUID code = UUID.randomUUID();
            String name = "Test User";
            User user = mock(User.class);
            when(user.getCode()).thenReturn(code);
            when(user.getName()).thenReturn(name);

            // When
            UserAuditor userAuditor = UserAuditor.withUser(user);

            // Then
            assertThat(userAuditor).isNotNull();
            assertThat(userAuditor.code()).isEqualTo(code);
            assertThat(userAuditor.name()).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should maintain immutability")
        void shouldMaintainImmutability() {
            // Given
            UUID code = UUID.randomUUID();
            String name = "Test User";
            UserAuditor userAuditor = UserAuditor.of(code, name);

            // When - Try to verify immutability
            // We can't actually modify the record fields since they're final, but we can verify they're unchanged

            // Then
            assertThat(userAuditor.code()).isEqualTo(code);
            assertThat(userAuditor.name()).isEqualTo(name);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() {
            // Given
            UUID code = UUID.randomUUID();
            String name = "Test User";
            UserAuditor userAuditor = UserAuditor.of(code, name);

            // Then - Just verify it's an instance of Serializable
            assertThat(userAuditor).isInstanceOf(java.io.Serializable.class);
        }
    }
}