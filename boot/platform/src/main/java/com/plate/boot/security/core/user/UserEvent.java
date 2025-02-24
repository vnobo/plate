package com.plate.boot.security.core.user;

import lombok.Getter;
import org.springframework.data.relational.core.conversion.AggregateChange;
import org.springframework.data.relational.core.mapping.event.AfterSaveEvent;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Getter
public class UserEvent extends AfterSaveEvent<User> {

    private final User user;

    /**
     * @param instance the saved entity. Must not be {@literal null}.
     * @param change   the {@link AggregateChange} encoding the actions performed on the database as part of the delete.
     *                 Must not be {@literal null}.
     */
    public UserEvent(User instance, AggregateChange<User> change) {
        super(instance, change);
        this.user = instance;
    }

}
