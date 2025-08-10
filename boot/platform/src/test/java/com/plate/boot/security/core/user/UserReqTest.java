package com.plate.boot.security.core.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserReq Unit Tests
 *
 * <p>This test class provides unit tests for the UserReq class, covering:</p>
 * <ul>
 *   <li>User request creation and initialization</li>
 *   <li>Security code setting with fluent API</li>
 *   <li>Conversion to User entity</li>
 * </ul>
 *
 * @author Qwen Code
 */
class UserReqTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create UserReq with default values")
        void shouldCreateUserReqWithDefaultValues() {
            // When
            UserReq userReq = new UserReq();

            // Then
            assertThat(userReq).isNotNull();
            // Inherits all properties from User
            assertThat(userReq.getUsername()).isNull();
            assertThat(userReq.getPassword()).isNull();
            assertThat(userReq.getDisabled()).isNull();
            assertThat(userReq.getAccountExpired()).isNull();
            assertThat(userReq.getAccountLocked()).isNull();
            assertThat(userReq.getCredentialsExpired()).isNull();
            assertThat(userReq.getEmail()).isNull();
            assertThat(userReq.getPhone()).isNull();
            assertThat(userReq.getName()).isNull();
            assertThat(userReq.getAvatar()).isNull();
            assertThat(userReq.getBio()).isNull();
            assertThat(userReq.getLoginTime()).isNull();
        }
    }

    @Nested
    @DisplayName("Security Code Tests")
    class SecurityCodeTests {

        @Test
        @DisplayName("Should set security code with fluent API")
        void shouldSetSecurityCodeWithFluentAPI() {
            // Given
            UserReq userReq = new UserReq();
            String securityCode = "SEC123456";

            // When
            UserReq result = userReq.securityCode(securityCode);

            // Then
            assertThat(result).isSameAs(userReq); // Fluent API returns self
            // Note: securityCode is not a standard field in User, so we can't directly test it
            // This would depend on the actual implementation of setSecurityCode
        }
    }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("Should convert to User entity")
        void shouldConvertToUserEntity() {
            // Given
            UserReq userReq = new UserReq();
            userReq.setUsername("testuser");
            userReq.setPassword("TestPass123");
            userReq.setName("Test User");
            userReq.setEmail("test@example.com");

            // When
            User user = userReq.toUser();

            // Then
            assertThat(user).isNotNull();
            assertThat(user).isNotSameAs(userReq); // Should be a new instance
            assertThat(user.getUsername()).isEqualTo(userReq.getUsername());
            assertThat(user.getPassword()).isEqualTo(userReq.getPassword());
            assertThat(user.getName()).isEqualTo(userReq.getName());
            assertThat(user.getEmail()).isEqualTo(userReq.getEmail());
        }
    }
}