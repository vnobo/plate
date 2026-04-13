package com.plate.boot.relational.dictionaries;

import com.plate.boot.commons.base.AbstractEvent;

/**
 * Event class for Dictionary entity lifecycle events.
 * Publishes events when dictionaries are created, updated, or deleted.
 * <p>
 * These events can be consumed by event listeners for:
 * - Cache invalidation
 * - Audit logging
 * - Notification triggering
 * - Cross-module communication
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class DictionaryEvent extends AbstractEvent<Dictionary> {

    /**
     * Constructs a new DictionaryEvent.
     *
     * @param dictionary The dictionary entity associated with this event
     * @param kind       The kind of event (INSERT, UPDATE, DELETE)
     */
    private DictionaryEvent(Dictionary dictionary, Kind kind) {
        super(dictionary, kind);
    }

    /**
     * Creates an INSERT event for a newly created dictionary.
     *
     * @param dictionary The dictionary that was inserted
     * @return A new DictionaryEvent with INSERT kind
     */
    public static DictionaryEvent insert(Dictionary dictionary) {
        return new DictionaryEvent(dictionary, Kind.INSERT);
    }

    /**
     * Creates an UPDATE event for a modified dictionary.
     *
     * @param dictionary The dictionary that was updated
     * @return A new DictionaryEvent with UPDATE kind
     */
    public static DictionaryEvent update(Dictionary dictionary) {
        return new DictionaryEvent(dictionary, Kind.UPDATE);
    }

    /**
     * Creates a DELETE event for a removed dictionary.
     *
     * @param dictionary The dictionary that was deleted
     * @return A new DictionaryEvent with DELETE kind
     */
    public static DictionaryEvent delete(Dictionary dictionary) {
        return new DictionaryEvent(dictionary, Kind.DELETE);
    }
}
