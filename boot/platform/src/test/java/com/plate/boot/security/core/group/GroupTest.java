package com.plate.boot.security.core.group;

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
 * Unit tests for {@link Group} (no Spring / container required).
 */
class GroupTest {

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
        Group group = new Group();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID pcode = UUID.randomUUID();
        UUID securityCode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        group.setId(1);
        group.setVersion(1L);
        group.setCode(code);
        group.setTenantCode(tenantCode);
        group.setExtend(extend);
        group.setCreatedBy(createdBy);
        group.setCreatedAt(createdAt);
        group.setUpdatedBy(createdBy);
        group.setUpdatedAt(createdAt);
        group.setQuery(Map.of("q", "v"));
        group.setSearch("search");
        group.setSecurityCode(securityCode);
        group.setPcode(pcode);
        group.setName("Admins");
        group.setDescription("Admin group");

        assertThat(group.getId()).isEqualTo(1);
        assertThat(group.getVersion()).isEqualTo(1L);
        assertThat(group.getCode()).isEqualTo(code);
        assertThat(group.getTenantCode()).isEqualTo(tenantCode);
        assertThat(group.getExtend()).isEqualTo(extend);
        assertThat(group.getCreatedBy()).isEqualTo(createdBy);
        assertThat(group.getCreatedAt()).isEqualTo(createdAt);
        assertThat(group.getUpdatedBy()).isEqualTo(createdBy);
        assertThat(group.getUpdatedAt()).isEqualTo(createdAt);
        assertThat(group.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(group.getSearch()).isEqualTo("search");
        assertThat(group.getSecurityCode()).isEqualTo(securityCode);
        assertThat(group.getPcode()).isEqualTo(pcode);
        assertThat(group.getName()).isEqualTo("Admins");
        assertThat(group.getDescription()).isEqualTo("Admin group");
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        Group a = new Group();
        a.setId(1);
        a.setName("Admins");

        Group b = new Group();
        b.setId(1);
        b.setName("Admins");

        Group c = new Group();
        c.setId(2);
        c.setName("Admins");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        Group group = new Group();
        group.setName("Users");
        group.setDescription("desc");
        assertThat(group.toString()).contains("Users", "desc");
    }

    @Test
    void isNewBehaviour() {
        Group group = new Group();
        assertThat(group.isNew()).isTrue();
        assertThat(group.getCode()).isNotNull();

        Group persisted = new Group();
        persisted.setId(3);
        assertThat(persisted.isNew()).isFalse();
    }
}
