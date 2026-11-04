package com.plate.boot.security.core.group.authority;

import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.query.Criteria;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GroupAuthorityReq} (no Spring / container required).
 */
class GroupAuthorityReqTest {

    @Test
    void accessorsRoundTrip() {
        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setAuthorities(Set.of("ROLE_A", "ROLE_B"));
        req.setGroupCode(UUID.randomUUID());
        req.setAuthority("ROLE_A");

        assertThat(req.getAuthorities()).containsExactlyInAnyOrder("ROLE_A", "ROLE_B");
        assertThat(req.getAuthority()).isEqualTo("ROLE_A");
    }

    @Test
    void toGroupAuthorityCopiesProperties() {
        GroupAuthorityReq req = new GroupAuthorityReq();
        UUID groupCode = UUID.randomUUID();
        req.setGroupCode(groupCode);
        req.setAuthority("ROLE_USER");

        GroupAuthority authority = req.toGroupAuthority();

        assertThat(authority.getGroupCode()).isEqualTo(groupCode);
        assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void toCriteriaSkipsAuthoritiesField() {
        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(UUID.randomUUID());
        req.setAuthorities(Set.of("ROLE_X"));

        Criteria criteria = req.toCriteria();

        assertThat(criteria).isNotNull();
    }

    @Test
    void equalsHashCodeAndToString() {
        GroupAuthorityReq a = new GroupAuthorityReq();
        a.setId(1);
        GroupAuthorityReq b = new GroupAuthorityReq();
        b.setId(1);
        GroupAuthorityReq c = new GroupAuthorityReq();
        c.setId(2);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("GroupAuthorityReq");
    }
}
