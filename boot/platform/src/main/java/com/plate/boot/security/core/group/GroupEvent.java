package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEvent;
import lombok.NonNull;

/**
 * GroupEvent class that extends the AbstractEvent class to handle group-related events.
 * This class is used to create and manage events related to group operations.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 * @see AbstractEvent
 * @see Group
 * @see Kind
 */
public class GroupEvent extends AbstractEvent<Group> {

    /**
     * Constructs a new GroupEvent.
     *
     * @param entity the group entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected GroupEvent(@NonNull Group entity, @NonNull Kind kind) {
        super(entity, kind);
    }

    /**
     * Creates a new GroupEvent for an insert operation.
     *
     * @param entity the group entity associated with the event, must not be null
     * @return a new GroupEvent instance for the insert operation
     */
    public static GroupEvent insert(Group entity) {
        return new GroupEvent(entity, Kind.INSERT);
    }

    /**
     * Creates a new GroupEvent for a update operation.
     *
     * @param entity the group entity associated with the event, must not be null
     * @return a new GroupEvent instance for the update operation
     */
    public static GroupEvent update(Group entity) {
        return new GroupEvent(entity, Kind.UPDATE);
    }

    /**
     * Creates a new GroupEvent for a delete operation.
     *
     * @param entity the group entity associated with the event, must not be null
     * @return a new GroupEvent instance for the delete operation
     */
    public static GroupEvent delete(Group entity) {
        return new GroupEvent(entity, Kind.DELETE);
    }

}