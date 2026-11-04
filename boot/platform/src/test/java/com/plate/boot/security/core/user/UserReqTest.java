package com.plate.boot.security.core.user;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserReq} (no Spring / container required).
 */
class UserReqTest {

    @Test
    void securityCodeSetsAndReturnsSelf() {
        UserReq req = new UserReq();
        UUID securityCode = UUID.randomUUID();

        UserReq result = req.securityCode(securityCode);

        assertThat(result).isSameAs(req);
        assertThat(req.getSecurityCode()).isEqualTo(securityCode);
    }

    @Test
    void toUserCopiesProperties() {
        UserReq req = new UserReq();
        req.setUsername("alice");
        req.setName("Alice");
        req.setEmail("alice@example.com");

        User user = req.toUser();

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getName()).isEqualTo("Alice");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void equalsHashCodeAndToString() {
        UserReq a = new UserReq();
        a.setId(1L);
        UserReq b = new UserReq();
        b.setId(1L);
        UserReq c = new UserReq();
        c.setId(2L);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("UserReq");
    }
}
