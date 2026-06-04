package com.plate.boot.relational.menus;

import com.plate.boot.commons.base.AbstractEvent;
import lombok.NonNull;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class MenuEvent extends AbstractEvent<Menu> {
    /**
     * Constructs a new BaseEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected MenuEvent(@NonNull Menu entity, @NonNull Kind kind) {
        super(entity, kind);
    }

    /**
     * Creates a new BaseEvent for an insert operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new BaseEvent instance for the insert operation
     */
    public static MenuEvent insert(Menu entity) {
        return new MenuEvent(entity, Kind.INSERT);
    }

    /**
     * Creates a new BaseEvent for a update operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new BaseEvent instance for the update operation
     */
    public static MenuEvent update(Menu entity) {
        return new MenuEvent(entity, Kind.UPDATE);
    }

    /**
     * Creates a new BaseEvent for a delete operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new BaseEvent instance for the delete operation
     */
    public static MenuEvent delete(Menu entity) {
        return new MenuEvent(entity, Kind.DELETE);
    }
}
