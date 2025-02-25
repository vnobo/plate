package com.plate.boot.security.core.user;

import com.plate.boot.commons.base.BaseEvent;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class UserEvent extends BaseEvent<User> {

    protected UserEvent(User entity, Kind kind) {
        super(entity, kind);
    }

    public static UserEvent insert(User entity) {
        return new UserEvent(entity, Kind.INSERT);
    }

    public static UserEvent save(User entity) {
        return new UserEvent(entity, Kind.SAVE);
    }

    public static UserEvent delete(User entity) {
        return new UserEvent(entity, Kind.DELETE);
    }

}
