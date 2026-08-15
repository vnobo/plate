package com.plate.boot.relational.logger;

import com.plate.boot.commons.base.AbstractEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerEvent} factory methods.
 */
class LoggerEventTest {

    @Test
    void insertCreatesInsertEvent() {
        LoggerReq req = new LoggerReq();
        LoggerEvent event = LoggerEvent.insert(req);

        assertThat(event.getEntity()).isSameAs(req);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.INSERT);
    }

    @Test
    void updateCreatesUpdateEvent() {
        LoggerReq req = new LoggerReq();
        LoggerEvent event = LoggerEvent.update(req);

        assertThat(event.getEntity()).isSameAs(req);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.UPDATE);
    }

    @Test
    void deleteCreatesDeleteEvent() {
        LoggerReq req = new LoggerReq();
        LoggerEvent event = LoggerEvent.delete(req);

        assertThat(event.getEntity()).isSameAs(req);
        assertThat(event.getKind()).isEqualTo(AbstractEvent.Kind.DELETE);
    }
}
