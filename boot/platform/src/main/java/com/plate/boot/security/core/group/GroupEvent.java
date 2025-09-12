package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEvent;
import lombok.NonNull;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class GroupEvent extends AbstractEvent<Group> {
    /**
     * Constructs a new BaseEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected GroupEvent(@NonNull Group entity, @NonNull Kind kind) {
        super(entity, kind);
    }
}
