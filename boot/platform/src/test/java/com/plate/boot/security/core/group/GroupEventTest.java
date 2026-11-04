package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GroupEvent} factory methods.
 */
class GroupEventTest {

    @Test
    void insertCreatesInsertEvent() {
        Group group = new Group();
        GroupEvent event = GroupEvent.insert(group);

        assertThat(event.getEntity()).isSameAs(group);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
    }

    @Test
    void updateCreatesUpdateEvent() {
        Group group = new Group();
        GroupEvent event = GroupEvent.update(group);

        assertThat(event.getEntity()).isSameAs(group);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.UPDATE);
    }

    @Test
    void deleteCreatesDeleteEvent() {
        Group group = new Group();
        GroupEvent event = GroupEvent.delete(group);

        assertThat(event.getEntity()).isSameAs(group);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.DELETE);
    }
}
