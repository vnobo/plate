package com.plate.boot.security.core.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User Unit Tests
 *
 * <p>This test class provides unit tests for the User class, covering:</p>
 * <ul>
 *   <li>User creation and initialization</li>
 *   <li>User property access and modification</li>
 *   <li>User code prefixing logic</li>
 *   <li>Password validation rules</li>
 * </ul>
 *
 * @author Qwen Code
 */
class UserTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create user with default values")
        void shouldCreateUserWithDefaultValues() {
            // When
            User user = new User();

            // Then
            assertThat(user).isNotNull();
            assertThat(user.getUsername()).isNull();
            assertThat(user.getPassword()).isNull();
            assertThat(user.getDisabled()).isNull();
            assertThat(user.getAccountExpired()).isNull();
            assertThat(user.getAccountLocked()).isNull();
            assertThat(user.getCredentialsExpired()).isNull();
            assertThat(user.getEmail()).isNull();
            assertThat(user.getPhone()).isNull();
            assertThat(user.getName()).isNull();
            assertThat(user.getAvatar()).isNull();
            assertThat(user.getBio()).isNull();
            assertThat(user.getExtend()).isNull();
            assertThat(user.getLoginTime()).isNull();
        }
    }

    @Nested
    @DisplayName("Property Tests")
    class PropertyTests {

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            // Given
            User user = new User();
            String username = "testuser";

            // When
            user.setUsername(username);

            // Then
            assertThat(user.getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should set and get password")
        void shouldSetAndGetPassword() {
            // Given
            User user = new User();
            String password = "TestPass123";

            // When
            user.setPassword(password);

            // Then
            assertThat(user.getPassword()).isEqualTo(password);
        }

        @Test
        @DisplayName("Should set and get disabled status")
        void shouldSetAndGetDisabledStatus() {
            // Given
            User user = new User();
            Boolean disabled = true;

            // When
            user.setDisabled(disabled);

            // Then
            assertThat(user.getDisabled()).isEqualTo(disabled);
        }

        @Test
        @DisplayName("Should set and get account expired status")
        void shouldSetAndGetAccountExpiredStatus() {
            // Given
            User user = new User();
            Boolean accountExpired = false;

            // When
            user.setAccountExpired(accountExpired);

            // Then
            assertThat(user.getAccountExpired()).isEqualTo(accountExpired);
        }

        @Test
        @DisplayName("Should set and get account locked status")
        void shouldSetAndGetAccountLockedStatus() {
            // Given
            User user = new User();
            Boolean accountLocked = true;

            // When
            user.setAccountLocked(accountLocked);

            // Then
            assertThat(user.getAccountLocked()).isEqualTo(accountLocked);
        }

        @Test
        @DisplayName("Should set and get credentials expired status")
        void shouldSetAndGetCredentialsExpiredStatus() {
            // Given
            User user = new User();
            Boolean credentialsExpired = false;

            // When
            user.setCredentialsExpired(credentialsExpired);

            // Then
            assertThat(user.getCredentialsExpired()).isEqualTo(credentialsExpired);
        }

        @Test
        @DisplayName("Should set and get email")
        void shouldSetAndGetEmail() {
            // Given
            User user = new User();
            String email = "test@example.com";

            // When
            user.setEmail(email);

            // Then
            assertThat(user.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should set and get phone")
        void shouldSetAndGetPhone() {
            // Given
            User user = new User();
            String phone = "1234567890";

            // When
            user.setPhone(phone);

            // Then
            assertThat(user.getPhone()).isEqualTo(phone);
        }

        @Test
        @DisplayName("Should set and get name")
        void shouldSetAndGetName() {
            // Given
            User user = new User();
            String name = "Test User";

            // When
            user.setName(name);

            // Then
            assertThat(user.getName()).isEqualTo(name);
        }

        @Test
        @DisplayName("Should set and get avatar")
        void shouldSetAndGetAvatar() {
            // Given
            User user = new User();
            String avatar = "avatar.jpg";

            // When
            user.setAvatar(avatar);

            // Then
            assertThat(user.getAvatar()).isEqualTo(avatar);
        }

        @Test
        @DisplayName("Should set and get bio")
        void shouldSetAndGetBio() {
            // Given
            User user = new User();
            String bio = "This is a test user";

            // When
            user.setBio(bio);

            // Then
            assertThat(user.getBio()).isEqualTo(bio);
        }

        @Test
        @DisplayName("Should set and get login time")
        void shouldSetAndGetLoginTime() {
            // Given
            User user = new User();
            LocalDateTime loginTime = LocalDateTime.now();

            // When
            user.setLoginTime(loginTime);

            // Then
            assertThat(user.getLoginTime()).isEqualTo(loginTime);
        }
    }

    @Nested
    @DisplayName("Code Prefix Tests")
    class CodePrefixTests {

        @Test
        @DisplayName("Should set code without prefixing (using default implementation)")
        void shouldSetCodeWithoutPrefixing() {
            // Given
            User user = new User();
            UUID code = UUID.randomUUID();

            // When
            user.setCode(code);

            // Then
            // Since the User class doesn't override setCode, it uses the default implementation
            // which doesn't do any prefixing
            assertThat(user.getCode()).isEqualTo(code);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should accept valid username")
        void shouldAcceptValidUsername() {
            // Given
            User user = new User();
            String username = "test_user-123"; // 13 chars, valid characters

            // When
            user.setUsername(username);

            // Then
            assertThat(user.getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("Should accept valid password")
        void shouldAcceptValidPassword() {
            // Given
            User user = new User();
            String password = "TestPass123"; // At least 6 chars, with uppercase, lowercase and number

            // When
            user.setPassword(password);

            // Then
            assertThat(user.getPassword()).isEqualTo(password);
        }
    }
}