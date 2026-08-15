package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.base.AbstractEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantEvent} factory methods.
 */
class TenantEventTest {

    @Test
    void insertCreatesInsertEvent() {
        Tenant tenant = new Tenant();
        TenantEvent event = TenantEvent.insert(tenant);

        assertThat(event.getEntity()).isSameAs(tenant);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
    }

    @Test
    void updateCreatesUpdateEvent() {
        Tenant tenant = new Tenant();
        TenantEvent event = TenantEvent.update(tenant);

        assertThat(event.getEntity()).isSameAs(tenant);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.UPDATE);
    }

    @Test
    void deleteCreatesDeleteEvent() {
        Tenant tenant = new Tenant();
        TenantEvent event = TenantEvent.delete(tenant);

        assertThat(event.getEntity()).isSameAs(tenant);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.DELETE);
    }
}
