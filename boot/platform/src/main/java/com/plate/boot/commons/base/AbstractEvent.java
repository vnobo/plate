package com.plate.boot.commons.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.ResolvableType;
import org.springframework.data.relational.core.mapping.event.AbstractRelationalEvent;

/**
 * Base event class for handling relational events in Spring Data.
 * Extends the AbstractRelationalEvent to provide additional context and functionality.
 *
 * @param <E> the type of the entity associated with the event
 * @see AbstractRelationalEvent
 * @see ResolvableType
 */
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractEvent<E> extends AbstractRelationalEvent<E> {

    /**
     * The entity associated with the event.
     */
    public final E entity;

    /**
     * The kind of event (INSERT, MODIFY, DELETE).
     */
    @Getter
    public final Kind kind;

    /**
     * Constructs a new BaseEvent.
     *
     * @param entity the entity associated with the event, must not be null
     * @param kind   the kind of event, must not be null
     */
    protected AbstractEvent(@NonNull E entity, @NonNull Kind kind) {
        super(entity);
        this.entity = entity;
        this.kind = kind;
    }

    /**
     * Returns the resolvable type of the event.
     *
     * @return the resolvable type
     */
    @Override
    public @NonNull ResolvableType getResolvableType() {
        return ResolvableType.forType(getType());
    }

    /**
     * Returns the kind of event.
     *
     * @return the kind of event
     */
    public Kind kind() {
        return getKind();
    }

    /**
     * Returns the entity associated with the event.
     *
     * @return the entity
     */
    public E entity() {
        return getEntity();
    }

    /**
     * Returns the entity associated with the event.
     *
     * @return the entity
     */
    @Override
    public E getEntity() {
        return this.entity;
    }

    /**
     * Returns the type of the entity associated with the event.
     *
     * @return the entity type
     */
    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Class<E> getType() {
        return (Class<E>) this.getClass();
    }

    /**
     * Enumeration of event kinds.
     */
    public enum Kind {

        /**
         * An INSERT event, typically involves an insert on the aggregate root.
         */
        INSERT,

        /**
         * A UPDATE event, typically involves an update on the aggregate root.
         */
        UPDATE,

        /**
         * A DELETE event, typically involves a delete on all contained entities.
         */
        DELETE
    }
}