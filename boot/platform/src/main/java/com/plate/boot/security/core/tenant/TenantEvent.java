package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.base.AbstractEvent;
import lombok.NonNull;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class TenantEvent extends AbstractEvent<Tenant> {
    /**
     * Constructs a new BaseEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected TenantEvent(@NonNull Tenant entity, @NonNull Kind kind) {
        super(entity, kind);
    }

    public static TenantEvent insert(Tenant entity) {
        return new TenantEvent(entity, Kind.INSERT);
    }

    public static TenantEvent update(Tenant entity) {
        return new TenantEvent(entity, Kind.UPDATE);
    }

    public static TenantEvent delete(Tenant entity) {
        return new TenantEvent(entity, Kind.DELETE);
    }
}
