package com.plate.boot.security.core.group.member;

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
 * Unit tests for {@link GroupMember} (no Spring / container required).
 */
class GroupMemberTest {

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
        GroupMember member = new GroupMember();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID groupCode = UUID.randomUUID();
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
        member.setGroupCode(groupCode);
        member.setUserCode(userCode);

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
        assertThat(member.getGroupCode()).isEqualTo(groupCode);
        assertThat(member.getUserCode()).isEqualTo(userCode);
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        UUID groupCode = UUID.randomUUID();
        UUID userCode = UUID.randomUUID();

        GroupMember a = new GroupMember();
        a.setId(1L);
        a.setGroupCode(groupCode);
        a.setUserCode(userCode);

        GroupMember b = new GroupMember();
        b.setId(1L);
        b.setGroupCode(groupCode);
        b.setUserCode(userCode);

        GroupMember c = new GroupMember();
        c.setId(2L);
        c.setGroupCode(groupCode);
        c.setUserCode(userCode);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        GroupMember member = new GroupMember();
        UUID groupCode = UUID.randomUUID();
        member.setGroupCode(groupCode);
        assertThat(member.toString()).contains(groupCode.toString());
    }

    @Test
    void isNewBehaviour() {
        GroupMember member = new GroupMember();
        assertThat(member.isNew()).isTrue();
        assertThat(member.getCode()).isNotNull();

        GroupMember persisted = new GroupMember();
        persisted.setId(3L);
        assertThat(persisted.isNew()).isFalse();
    }
}
