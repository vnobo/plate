package com.plate.boot.security.core.tenant;

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
 * Unit tests for {@link Tenant} (no Spring / container required).
 */
class TenantTest {

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
        Tenant tenant = new Tenant();
        UUID code = UUID.randomUUID();
        UUID tenantCode = UUID.randomUUID();
        UUID pcode = UUID.randomUUID();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        UserAuditor createdBy = UserAuditor.of(UUID.randomUUID(), "creator");
        LocalDateTime createdAt = LocalDateTime.now();

        tenant.setId(1);
        tenant.setVersion(1L);
        tenant.setCode(code);
        tenant.setTenantCode(tenantCode);
        tenant.setExtend(extend);
        tenant.setCreatedBy(createdBy);
        tenant.setCreatedAt(createdAt);
        tenant.setUpdatedBy(createdBy);
        tenant.setUpdatedAt(createdAt);
        tenant.setQuery(Map.of("q", "v"));
        tenant.setSearch("search");
        tenant.setSecurityCode(UUID.randomUUID());
        tenant.setPcode(pcode);
        tenant.setName("Platform");
        tenant.setDescription("Tenant");

        assertThat(tenant.getId()).isEqualTo(1);
        assertThat(tenant.getVersion()).isEqualTo(1L);
        assertThat(tenant.getCode()).isEqualTo(code);
        assertThat(tenant.getTenantCode()).isEqualTo(tenantCode);
        assertThat(tenant.getExtend()).isEqualTo(extend);
        assertThat(tenant.getCreatedBy()).isEqualTo(createdBy);
        assertThat(tenant.getCreatedAt()).isEqualTo(createdAt);
        assertThat(tenant.getUpdatedBy()).isEqualTo(createdBy);
        assertThat(tenant.getUpdatedAt()).isEqualTo(createdAt);
        assertThat(tenant.getQuery()).isEqualTo(Map.of("q", "v"));
        assertThat(tenant.getSearch()).isEqualTo("search");
        assertThat(tenant.getPcode()).isEqualTo(pcode);
        assertThat(tenant.getName()).isEqualTo("Platform");
        assertThat(tenant.getDescription()).isEqualTo("Tenant");
    }

    @Test
    void equalsAndHashCodeBasedOnIdAndFields() {
        Tenant a = new Tenant();
        a.setId(1);
        a.setName("Platform");

        Tenant b = new Tenant();
        b.setId(1);
        b.setName("Platform");

        Tenant c = new Tenant();
        c.setId(2);
        c.setName("Platform");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        Tenant tenant = new Tenant();
        tenant.setName("Platform");
        assertThat(tenant.toString()).contains("Platform");
    }

    @Test
    void isNewBehaviour() {
        Tenant tenant = new Tenant();
        assertThat(tenant.isNew()).isTrue();
        assertThat(tenant.getCode()).isNotNull();

        Tenant persisted = new Tenant();
        persisted.setId(3);
        assertThat(persisted.isNew()).isFalse();
    }
}
