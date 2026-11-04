package com.plate.boot.security.core.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserRes} (no Spring / container required).
 * Focuses on the phone/email masking and the password suppression.
 */
class UserResTest {

    @Test
    void getPhoneReturnsNullWhenAbsent() {
        assertThat(new UserRes().getPhone()).isNull();
    }

    @Test
    void getPhoneReturnsUnchangedWhenShort() {
        UserRes res = new UserRes();
        res.setPhone("123456");
        assertThat(res.getPhone()).isEqualTo("123456");
    }

    @Test
    void getPhoneMasksMiddleDigits() {
        UserRes res = new UserRes();
        res.setPhone("13812345678");
        assertThat(res.getPhone()).isEqualTo("138****5678");
    }

    @Test
    void getEmailReturnsNullWhenAbsent() {
        assertThat(new UserRes().getEmail()).isNull();
    }

    @Test
    void getEmailReturnsUnchangedWhenNoAtSign() {
        UserRes res = new UserRes();
        res.setEmail("no-at-sign");
        assertThat(res.getEmail()).isEqualTo("no-at-sign");
    }

    @Test
    void getEmailReturnsUnchangedForShortUsername() {
        UserRes res = new UserRes();
        res.setEmail("ab@example.com");
        assertThat(res.getEmail()).isEqualTo("ab@example.com");
    }

    @Test
    void getEmailMasksLongUsername() {
        UserRes res = new UserRes();
        res.setEmail("alice@example.com");
        assertThat(res.getEmail()).isEqualTo("al****@example.com");
    }

    @Test
    void getPasswordReturnsSuperValue() {
        UserRes res = new UserRes();
        res.setPassword("Password1");
        assertThat(res.getPassword()).isEqualTo("Password1");
    }

    @Test
    void rankAccessorRoundTrip() {
        UserRes res = new UserRes();
        res.setRank(0.9D);
        assertThat(res.getRank()).isEqualTo(0.9D);
    }

    @Test
    void equalsHashCodeAndToString() {
        UserRes a = new UserRes();
        a.setId(1L);
        UserRes b = new UserRes();
        b.setId(1L);
        UserRes c = new UserRes();
        c.setId(2L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("UserRes");
    }
}
