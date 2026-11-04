package com.plate.boot.security.core.user;

import com.plate.boot.commons.base.AbstractEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserEvent} factory methods.
 */
class UserEventTest {

    @Test
    void insertCreatesInsertEvent() {
        User user = new User();
        UserEvent event = UserEvent.insert(user);

        assertThat(event.getEntity()).isSameAs(user);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
    }

    @Test
    void updateCreatesUpdateEvent() {
        User user = new User();
        UserEvent event = UserEvent.update(user);

        assertThat(event.getEntity()).isSameAs(user);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.UPDATE);
    }

    @Test
    void deleteCreatesDeleteEvent() {
        User user = new User();
        UserEvent event = UserEvent.delete(user);

        assertThat(event.getEntity()).isSameAs(user);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.DELETE);
    }
}
