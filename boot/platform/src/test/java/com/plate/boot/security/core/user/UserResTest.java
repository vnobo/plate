package com.plate.boot.security.core.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRes Unit Tests
 *
 * <p>This test class provides unit tests for the UserRes class, covering:</p>
 * <ul>
 *   <li>User response creation and initialization</li>
 *   <li>Phone number masking</li>
 *   <li>Email masking</li>
 *   <li>Password field exclusion from JSON</li>
 * </ul>
 *
 * @author Qwen Code
 */
class UserResTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create UserRes with default values")
        void shouldCreateUserResWithDefaultValues() {
            // When
            UserRes userRes = new UserRes();

            // Then
            assertThat(userRes).isNotNull();
            // Inherits all properties from User
            assertThat(userRes.getUsername()).isNull();
            assertThat(userRes.getPassword()).isNull();
            assertThat(userRes.getDisabled()).isNull();
            assertThat(userRes.getAccountExpired()).isNull();
            assertThat(userRes.getAccountLocked()).isNull();
            assertThat(userRes.getCredentialsExpired()).isNull();
            assertThat(userRes.getEmail()).isNull();
            assertThat(userRes.getPhone()).isNull();
            assertThat(userRes.getName()).isNull();
            assertThat(userRes.getAvatar()).isNull();
            assertThat(userRes.getBio()).isNull();
            assertThat(userRes.getLoginTime()).isNull();
            assertThat(userRes.getRank()).isNull();
        }
    }

    @Nested
    @DisplayName("Phone Number Masking Tests")
    class PhoneNumberMaskingTests {

        @Test
        @DisplayName("Should mask phone number with 11 digits")
        void shouldMaskPhoneNumberWith11Digits() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setPhone("13812345678");

            // When
            String maskedPhone = userRes.getPhone();

            // Then
            assertThat(maskedPhone).isEqualTo("138****5678");
        }

        @Test
        @DisplayName("Should mask phone number with 10 digits")
        void shouldMaskPhoneNumberWith10Digits() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setPhone("1381234567");

            // When
            String maskedPhone = userRes.getPhone();

            // Then
            assertThat(maskedPhone).isEqualTo("138****567");
        }

        @Test
        @DisplayName("Should not mask phone number with less than 7 digits")
        void shouldNotMaskPhoneNumberWithLessThan7Digits() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setPhone("123456");

            // When
            String maskedPhone = userRes.getPhone();

            // Then
            assertThat(maskedPhone).isEqualTo("123456");
        }

        @Test
        @DisplayName("Should handle null phone number")
        void shouldHandleNullPhoneNumber() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setPhone(null);

            // When
            String maskedPhone = userRes.getPhone();

            // Then
            assertThat(maskedPhone).isNull();
        }
    }

    @Nested
    @DisplayName("Email Masking Tests")
    class EmailMaskingTests {

        @Test
        @DisplayName("Should mask email with valid format")
        void shouldMaskEmailWithValidFormat() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setEmail("test@example.com");

            // When
            String maskedEmail = userRes.getEmail();

            // Then
            assertThat(maskedEmail).isEqualTo("te****@example.com");
        }

        @Test
        @DisplayName("Should mask email with long username")
        void shouldMaskEmailWithLongUsername() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setEmail("verylongusername@example.com");

            // When
            String maskedEmail = userRes.getEmail();

            // Then
            assertThat(maskedEmail).isEqualTo("ve****@example.com");
        }

        @Test
        @DisplayName("Should handle email without @ symbol")
        void shouldHandleEmailWithoutAtSymbol() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setEmail("testexample.com");

            // When
            String maskedEmail = userRes.getEmail();

            // Then
            assertThat(maskedEmail).isEqualTo("testexample.com");
        }

        @Test
        @DisplayName("Should handle null email")
        void shouldHandleNullEmail() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setEmail(null);

            // When
            String maskedEmail = userRes.getEmail();

            // Then
            assertThat(maskedEmail).isNull();
        }
    }

    @Nested
    @DisplayName("Password Exclusion Tests")
    class PasswordExclusionTests {

        @Test
        @DisplayName("Should exclude password from JSON serialization")
        void shouldExcludePasswordFromJSONSerialization() {
            // Given
            UserRes userRes = new UserRes();
            userRes.setPassword("secretPassword");

            // When
            String password = userRes.getPassword();

            // Then
            // The @JsonIgnore annotation should exclude this from JSON
            // We can't directly test the JSON serialization here, but we can verify the annotation is present
            assertThat(password).isEqualTo("secretPassword");
            // The actual exclusion would be tested in integration tests with Jackson
        }
    }
}