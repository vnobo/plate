package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.UserAuditor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserAuthority} (no Spring / container required).
 */
class UserAuthorityTest {

    private static JsonMapper savedMapper;

    @BeforeAll
    static void setUpMapper() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDownMapper() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @Test
    void accessorsRoundTrip() {
        UserAuthority authority = new UserAuthority();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        LocalDateTime createdAt = LocalDateTime.now();

        authority.setId(1);
        authority.setVersion(1L);
        authority.setCode(code);
        authority.setTenantCode(tenantCode);
        authority.setExtend(extend);
        authority.setCreatedBy(createdBy);
        authority.setCreatedAt(createdAt);
        authority.setUpdatedBy(createdBy);
        authority.setUpdatedAt(createdAt);
        authority.setQuery(Map.of("q", "v"));
        authority.setSearch("search");
        authority.setSecurityCode(UUID.randomUUID());
        authority.setUserCode(userCode);
        authority.setAuthority("ROLE_USER");

        assertThat(authority.getId()).isEqualTo(1);
        assertThat(authority.getVersion()).isEqualTo(1L);
        assertThat(authority.getCode()).isEqualTo(code);
        assertThat(authority.getTenantCode()).isEqualTo(tenantCode);
        assertThat(authority.getExtend()).isEqualTo(extend);
        assertThat(authority.getCreatedBy()).isEqualTo(createdBy);
        assertThat(authority.getCreatedAt()).isEqualTo(createdAt);
        assertThat(authority.getUpdatedBy()).isEqualTo(createdBy);
        assertThat(authority.getUpdatedAt()).isEqualTo(createdAt);
        assertThat(authority.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(authority.getSearch()).isEqualTo("search");
        assertThat(authority.getUserCode()).isEqualTo(userCode);
        assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        UUID userCode = UUID.randomUUID();

        UserAuthority a = new UserAuthority();
        a.setId(1);
        a.setUserCode(userCode);
        a.setAuthority("ROLE_USER");

        UserAuthority b = new UserAuthority();
        b.setId(1);
        b.setUserCode(userCode);
        b.setAuthority("ROLE_USER");

        UserAuthority c = new UserAuthority();
        c.setId(2);
        c.setUserCode(userCode);
        c.setAuthority("ROLE_USER");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        UserAuthority authority = new UserAuthority();
        authority.setAuthority("ROLE_USER");
        assertThat(authority.toString()).contains("ROLE_USER");
    }

    @Test
    void isNewBehaviour() {
        UserAuthority authority = new UserAuthority();
        assertThat(authority.isNew()).isTrue();
        assertThat(authority.getCode()).isNotNull();

        UserAuthority persisted = new UserAuthority();
        persisted.setId(3);
        assertThat(persisted.isNew()).isFalse();
    }
}
