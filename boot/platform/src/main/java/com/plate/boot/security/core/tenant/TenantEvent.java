package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.base.AbstractEvent;
import lombok.NonNull;

/**
 * Domain event emitted when a {@link Tenant} is inserted, updated, or deleted.
 * Use the static factory methods {@link #insert(Tenant)}, {@link #update(Tenant)} and
 * {@link #delete(Tenant)} to build an event of the corresponding {@link Kind}.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class TenantEvent extends AbstractEvent<Tenant> {
    /**
     * Constructs a new TenantEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected TenantEvent(@NonNull Tenant entity, @NonNull Kind kind) {
        super(entity, kind);
    }

    /**
     * Creates an insert event for the given tenant.
     *
     * @param entity the tenant to be inserted, must not be null
     * @return a new insert event wrapping the supplied tenant
     */
    public static TenantEvent insert(Tenant entity) {
        return new TenantEvent(entity, Kind.INSERT);
    }

    /**
     * Creates an update event for the given tenant.
     *
     * @param entity the tenant to be updated, must not be null
     * @return a new update event wrapping the supplied tenant
     */
    public static TenantEvent update(Tenant entity) {
        return new TenantEvent(entity, Kind.UPDATE);
    }

    /**
     * Creates a delete event for the given tenant.
     *
     * @param entity the tenant to be deleted, must not be null
     * @return a new delete event wrapping the supplied tenant
     */
    public static TenantEvent delete(Tenant entity) {
        return new TenantEvent(entity, Kind.DELETE);
    }
}
