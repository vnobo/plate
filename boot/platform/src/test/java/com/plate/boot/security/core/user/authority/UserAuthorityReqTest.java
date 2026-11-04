package com.plate.boot.security.core.user.authority;

import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.query.Criteria;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserAuthorityReq} (no Spring / container required).
 */
class UserAuthorityReqTest {

    @Test
    void toAuthorityCopiesProperties() {
        UserAuthorityReq req = new UserAuthorityReq();
        UUID userCode = UUID.randomUUID();
        req.setUserCode(userCode);
        req.setAuthority("ROLE_USER");

        UserAuthority authority = req.toAuthority();

        assertThat(authority.getUserCode()).isEqualTo(userCode);
        assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void toCriteriaForEmptyRequest() {
        assertThat(new UserAuthorityReq().toCriteria()).isNotNull();
    }

    @Test
    void toCriteriaAddsTenantCodeAndAuthority() {
        UserAuthorityReq req = new UserAuthorityReq();
        req.setTenantCode(UUID.randomUUID());
        req.setAuthority("ROLE_USER");

        Criteria criteria = req.toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void equalsHashCodeAndToString() {
        UserAuthorityReq a = new UserAuthorityReq();
        a.setId(1);
        UserAuthorityReq b = new UserAuthorityReq();
        b.setId(1);
        UserAuthorityReq c = new UserAuthorityReq();
        c.setId(2);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("UserAuthorityReq");
    }
}
