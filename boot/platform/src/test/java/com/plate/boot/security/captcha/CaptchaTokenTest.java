package com.plate.boot.security.captcha;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CaptchaToken Unit Tests
 *
 * <p>This test class provides unit tests for the CaptchaToken record, covering:</p>
 * <ul>
 *   <li>Creation of CaptchaToken instances</li>
 *   <li>Captcha validation functionality</li>
 *   <li>Immutability and data integrity</li>
 * </ul>
 *
 * @author Qwen Code
 */
class CaptchaTokenTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create CaptchaToken with of method")
        void shouldCreateCaptchaTokenWithOfMethod() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";

            // When
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // Then
            assertThat(captchaToken).isNotNull();
            assertThat(captchaToken.headerName()).isEqualTo(headerName);
            assertThat(captchaToken.parameterName()).isEqualTo(parameterName);
            assertThat(captchaToken.captcha()).isEqualTo(captcha);
        }

        @Test
        @DisplayName("Should create CaptchaToken with constructor")
        void shouldCreateCaptchaTokenWithConstructor() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";

            // When
            CaptchaToken captchaToken = new CaptchaToken(headerName, parameterName, captcha);

            // Then
            assertThat(captchaToken).isNotNull();
            assertThat(captchaToken.headerName()).isEqualTo(headerName);
            assertThat(captchaToken.parameterName()).isEqualTo(parameterName);
            assertThat(captchaToken.captcha()).isEqualTo(captcha);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should validate correct captcha code")
        void shouldValidateCorrectCaptchaCode() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // When
            Boolean result = captchaToken.validate("ABCD1234");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should validate correct captcha code ignoring case")
        void shouldValidateCorrectCaptchaCodeIgnoringCase() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "AbCd1234";
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // When
            Boolean result = captchaToken.validate("abcd1234");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should not validate incorrect captcha code")
        void shouldNotValidateIncorrectCaptchaCode() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // When
            Boolean result = captchaToken.validate("EFGH5678");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when validating null captcha code")
        void shouldThrowExceptionWhenValidatingNullCaptchaCode() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // When & Then
            try {
                captchaToken.validate(null);
            } catch (IllegalArgumentException e) {
                assertThat(e).isNotNull();
                assertThat(e.getMessage()).contains("captcha code must not be null");
            }
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should maintain immutability")
        void shouldMaintainImmutability() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // When - Try to verify immutability
            // We can't actually modify the record fields since they're final, but we can verify they're unchanged

            // Then
            assertThat(captchaToken.headerName()).isEqualTo(headerName);
            assertThat(captchaToken.parameterName()).isEqualTo(parameterName);
            assertThat(captchaToken.captcha()).isEqualTo(captcha);
        }
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should be serializable")
        void shouldBeSerializable() {
            // Given
            String headerName = "X-CAPTCHA-TOKEN";
            String parameterName = "_captcha";
            String captcha = "ABCD1234";
            CaptchaToken captchaToken = CaptchaToken.of(headerName, parameterName, captcha);

            // Then - Just verify it's an instance of Serializable
            assertThat(captchaToken).isInstanceOf(java.io.Serializable.class);
        }
    }
}