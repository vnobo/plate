package com.plate.boot.security.captcha;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link CaptchaToken} validation behaviour.
 */
class CaptchaTokenTest {

    @Test
    void validateShouldReturnTrueForMatchingCodeCaseInsensitive() {
        CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "Ab12");

        assertThat(token.validate("ab12")).isTrue();
    }

    @Test
    void validateShouldReturnFalseForMismatchedCode() {
        CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "12345");

        assertThat(token.validate("00000")).isFalse();
    }

    @Test
    void validateShouldThrowForNullCode() {
        CaptchaToken token = CaptchaToken.of("X-CAPTCHA-TOKEN", "_captcha", "12345");

        assertThatThrownBy(() -> token.validate(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
