package com.plate.boot.security.core.user;

import com.plate.boot.commons.base.AbstractEvent;

/**
 * UserEvent class that extends the BaseEvent class to handle user-related events.
 * This class is used to create and manage events related to user operations.
 *
 * @see AbstractEvent
 * @see User
 * @see Kind
 * @see <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class UserEvent extends AbstractEvent<User> {

    /**
     * Constructs a new UserEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected UserEvent(User entity, Kind kind) {
        super(entity, kind);
    }

    /**
     * Creates a new UserEvent for an insert operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new UserEvent instance for the insert operation
     */
    public static UserEvent insert(User entity) {
        return new UserEvent(entity, Kind.INSERT);
    }

    /**
     * Creates a new UserEvent for a save operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new UserEvent instance for the save operation
     */
    public static UserEvent save(User entity) {
        return new UserEvent(entity, Kind.SAVE);
    }

    /**
     * Creates a new UserEvent for a delete operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new UserEvent instance for the delete operation
     */
    public static UserEvent delete(User entity) {
        return new UserEvent(entity, Kind.DELETE);
    }
}