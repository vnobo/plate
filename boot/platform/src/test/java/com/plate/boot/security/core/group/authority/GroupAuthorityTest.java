package com.plate.boot.security.core.group.authority;

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
 * Unit tests for {@link GroupAuthority} (no Spring / container required).
 */
class GroupAuthorityTest {

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
        GroupAuthority authority = new GroupAuthority();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID groupCode = UUID.randomUUID();
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
        authority.setGroupCode(groupCode);
        authority.setAuthority("ROLE_ADMIN");

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
        assertThat(authority.getGroupCode()).isEqualTo(groupCode);
        assertThat(authority.getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        UUID groupCode = UUID.randomUUID();

        GroupAuthority a = new GroupAuthority();
        a.setId(1);
        a.setGroupCode(groupCode);
        a.setAuthority("ROLE_ADMIN");

        GroupAuthority b = new GroupAuthority();
        b.setId(1);
        b.setGroupCode(groupCode);
        b.setAuthority("ROLE_ADMIN");

        GroupAuthority c = new GroupAuthority();
        c.setId(2);
        c.setGroupCode(groupCode);
        c.setAuthority("ROLE_ADMIN");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        GroupAuthority authority = new GroupAuthority();
        authority.setAuthority("ROLE_ADMIN");
        assertThat(authority.toString()).contains("ROLE_ADMIN");
    }

    @Test
    void isNewBehaviour() {
        GroupAuthority authority = new GroupAuthority();
        assertThat(authority.isNew()).isTrue();
        assertThat(authority.getCode()).isNotNull();

        GroupAuthority persisted = new GroupAuthority();
        persisted.setId(3);
        assertThat(persisted.isNew()).isFalse();
    }
}
