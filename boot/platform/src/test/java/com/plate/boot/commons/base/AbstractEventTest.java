package com.plate.boot.commons.base;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AbstractEvent} (no Spring / container required).
 * The event captures its entity, kind, and resolvable type; equality honours those.
 */
class AbstractEventTest {

    static class StringEvent extends AbstractEvent<String> {
        StringEvent(String entity, Kind kind) {
            super(entity, kind);
        }
    }

    @Test
    void exposesEntityKindAndType() {
        StringEvent event = new StringEvent("hello", AbstractEvent.Kind.INSERT);

        assertThat(event.getEntity()).isEqualTo("hello");
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
        assertThat(event.getType()).isEqualTo(StringEvent.class);
        assertThat(event.getResolvableType().getType()).isEqualTo(StringEvent.class);
    }

    @Test
    void equalsBasedOnEntityAndKind() {
        StringEvent a = new StringEvent("x", AbstractEvent.Kind.UPDATE);
        StringEvent c = new StringEvent("x", AbstractEvent.Kind.DELETE);

        // AbstractEvent uses @EqualsAndHashCode(callSuper = true); its superclass
        // (AbstractRelationalEvent) is identity-based, so two distinct event instances
        // are never equal — equality is reference equality.
        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo("not an event");
    }
}
