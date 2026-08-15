package com.plate.boot.relational.menus;

import com.plate.boot.commons.base.AbstractEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MenuEvent} factory methods and inherited {@link AbstractEvent} behaviour.
 */
class MenuEventTest {

    @Test
    void insertCreatesInsertEvent() {
        Menu menu = new Menu();
        MenuEvent event = MenuEvent.insert(menu);

        assertThat(event.getEntity()).isSameAs(menu);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
    }

    @Test
    void updateCreatesUpdateEvent() {
        Menu menu = new Menu();
        MenuEvent event = MenuEvent.update(menu);

        assertThat(event.getEntity()).isSameAs(menu);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.UPDATE);
    }

    @Test
    void deleteCreatesDeleteEvent() {
        Menu menu = new Menu();
        MenuEvent event = MenuEvent.delete(menu);

        assertThat(event.getEntity()).isSameAs(menu);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.DELETE);
    }

    @Test
    void exposesTypeAndResolvableType() {
        MenuEvent event = MenuEvent.insert(new Menu());

        assertThat(event.getType()).isEqualTo(MenuEvent.class);
        assertThat(event.getResolvableType().getType()).isEqualTo(MenuEvent.class);
    }
}
