package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantMemberRes} (no Spring / container required).
 */
class TenantMemberResTest {

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
        TenantMemberRes res = new TenantMemberRes();
        JsonNode extend = ContextUtils.OBJECT_MAPPER.createObjectNode().put("k", "v");

        res.setName("Acme");
        res.setExtend(extend);
        res.setEnabled(true);

        assertThat(res.getName()).isEqualTo("Acme");
        assertThat(res.getExtend()).isEqualTo(extend);
        assertThat(res.getEnabled()).isTrue();
    }

    @Test
    void equalsAndHashCodeIncludeFields() {
        TenantMemberRes a = new TenantMemberRes();
        a.setId(1L);
        a.setName("Acme");

        TenantMemberRes b = new TenantMemberRes();
        b.setId(1L);
        b.setName("Acme");

        TenantMemberRes c = new TenantMemberRes();
        c.setId(1L);
        c.setName("Other");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void toStringContainsOwnFields() {
        TenantMemberRes res = new TenantMemberRes();
        res.setName("Acme");
        assertThat(res.toString()).contains("Acme");
    }
}
