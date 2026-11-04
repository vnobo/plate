package com.plate.boot.relational.dictionaries;

import com.plate.boot.commons.base.AbstractEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DictionaryEvent} factory methods.
 */
class DictionaryEventTest {

    @Test
    void insertCreatesInsertEvent() {
        Dictionary dictionary = new Dictionary();
        DictionaryEvent event = DictionaryEvent.insert(dictionary);

        assertThat(event.getEntity()).isSameAs(dictionary);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
    }

    @Test
    void updateCreatesUpdateEvent() {
        Dictionary dictionary = new Dictionary();
        DictionaryEvent event = DictionaryEvent.update(dictionary);

        assertThat(event.getEntity()).isSameAs(dictionary);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.UPDATE);
    }

    @Test
    void deleteCreatesDeleteEvent() {
        Dictionary dictionary = new Dictionary();
        DictionaryEvent event = DictionaryEvent.delete(dictionary);

        assertThat(event.getEntity()).isSameAs(dictionary);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.DELETE);
    }
}
