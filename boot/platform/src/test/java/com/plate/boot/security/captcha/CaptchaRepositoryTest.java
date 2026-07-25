package com.plate.boot.security.captcha;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CaptchaRepository#createCaptchaToken()}, verifying that the generated
 * captcha code is randomly produced (no longer the previously hardcoded {@code "54321"}) and that
 * successive codes vary.
 */
class CaptchaRepositoryTest {

    @Test
    void createCaptchaTokenShouldGenerateRandomFiveDigitCodeNotHardcoded() {
        CaptchaRepository repository = new CaptchaRepository();

        CaptchaToken token = repository.createCaptchaToken();

        assertThat(token.captcha()).isNotEqualTo("54321");
        assertThat(token.captcha()).matches("\\d{5}");
    }

    @Test
    void createCaptchaTokenShouldVaryAcrossCalls() {
        CaptchaRepository repository = new CaptchaRepository();

        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            codes.add(repository.createCaptchaToken().captcha());
        }

        assertThat(codes.size()).isGreaterThan(1);
    }
}
