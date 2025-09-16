package com.plate.boot.relational.logger;

import com.plate.boot.commons.base.AbstractEvent;

/**
 * LoggerEvent class that extends the BaseEvent class to handle logging events.
 * This class is used to create and manage events related to logging requests.
 *
 * @see AbstractEvent
 * @see LoggerReq
 * @see Kind
 * @see <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class LoggerEvent extends AbstractEvent<LoggerReq> {

    /**
     * Constructs a new LoggerEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected LoggerEvent(LoggerReq entity, Kind kind) {
        super(entity, kind);
    }

    /**
     * Creates a new LoggerEvent for an insert operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new LoggerEvent instance for the insert operation
     */
    public static LoggerEvent insert(LoggerReq entity) {
        return new LoggerEvent(entity, Kind.INSERT);
    }

    /**
     * Creates a new LoggerEvent for a update operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new LoggerEvent instance for the update operation
     */
    public static LoggerEvent update(LoggerReq entity) {
        return new LoggerEvent(entity, Kind.UPDATE);
    }

    /**
     * Creates a new LoggerEvent for a delete operation.
     *
     * @param entity the entity associated with the event, must not be null
     * @return a new LoggerEvent instance for the delete operation
     */
    public static LoggerEvent delete(LoggerReq entity) {
        return new LoggerEvent(entity, Kind.DELETE);
    }
}