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
}