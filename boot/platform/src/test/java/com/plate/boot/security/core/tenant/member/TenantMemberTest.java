package com.plate.boot.security.core.tenant.member;

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
 * Unit tests for {@link TenantMember} (no Spring / container required).
 */
class TenantMemberTest {

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
        TenantMember member = new TenantMember();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        LocalDateTime createdAt = LocalDateTime.now();

        member.setId(1L);
        member.setVersion(1L);
        member.setCode(code);
        member.setTenantCode(tenantCode);
        member.setExtend(extend);
        member.setCreatedBy(createdBy);
        member.setCreatedAt(createdAt);
        member.setUpdatedBy(createdBy);
        member.setUpdatedAt(createdAt);
        member.setQuery(Map.of("q", "v"));
        member.setSearch("search");
        member.setSecurityCode(UUID.randomUUID());
        member.setUserCode(userCode);
        member.setEnabled(true);

        assertThat(member.getId()).isEqualTo(1L);
        assertThat(member.getVersion()).isEqualTo(1L);
        assertThat(member.getCode()).isEqualTo(code);
        assertThat(member.getTenantCode()).isEqualTo(tenantCode);
        assertThat(member.getExtend()).isEqualTo(extend);
        assertThat(member.getCreatedBy()).isEqualTo(createdBy);
        assertThat(member.getCreatedAt()).isEqualTo(createdAt);
        assertThat(member.getUpdatedBy()).isEqualTo(createdBy);
        assertThat(member.getUpdatedAt()).isEqualTo(createdAt);
        assertThat(member.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(member.getSearch()).isEqualTo("search");
        assertThat(member.getUserCode()).isEqualTo(userCode);
        assertThat(member.getEnabled()).isTrue();
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        UUID userCode = UUID.randomUUID();

        TenantMember a = new TenantMember();
        a.setId(1L);
        a.setUserCode(userCode);
        a.setEnabled(true);

        TenantMember b = new TenantMember();
        b.setId(1L);
        b.setUserCode(userCode);
        b.setEnabled(true);

        TenantMember c = new TenantMember();
        c.setId(2L);
        c.setUserCode(userCode);
        c.setEnabled(true);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        TenantMember member = new TenantMember();
        member.setEnabled(true);
        assertThat(member.toString()).contains("true");
    }

    @Test
    void isNewBehaviour() {
        TenantMember member = new TenantMember();
        assertThat(member.isNew()).isTrue();
        assertThat(member.getCode()).isNotNull();

        TenantMember persisted = new TenantMember();
        persisted.setId(3L);
        assertThat(persisted.isNew()).isFalse();
    }
}
