package com.plate.boot.security.core.tenant;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantReq} (no Spring / container required).
 */
class TenantReqTest {

    @Test
    void toTenantCopiesProperties() {
        TenantReq req = new TenantReq();
        req.setName("Acme");
        req.setDescription("Acme tenant");
        req.setPcode(UUID.randomUUID());

        Tenant tenant = req.toTenant();

        assertThat(tenant.getName()).isEqualTo("Acme");
        assertThat(tenant.getDescription()).isEqualTo("Acme tenant");
        assertThat(tenant.getPcode()).isEqualTo(req.getPcode());
    }

    @Test
    void equalsHashCodeAndToString() {
        TenantReq a = new TenantReq();
        a.setId(1);
        TenantReq b = new TenantReq();
        b.setId(1);
        TenantReq c = new TenantReq();
        c.setId(2);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.toString()).contains("TenantReq");
    }
}
