package com.plate.boot.commons.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.core.ResolvableType;
import org.springframework.data.relational.core.mapping.event.AbstractRelationalEvent;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEvent<E> extends AbstractRelationalEvent<E> {

    public final E entity;

    @Getter
    public final Kind kind;

    protected BaseEvent(@NonNull E entity, @NonNull Kind kind) {
        super(entity);
        this.entity = entity;
        this.kind = kind;
    }

    @Override
    public @NonNull ResolvableType getResolvableType() {
        return ResolvableType.forType(getType());
    }

    public Kind kind() {
        return getKind();
    }

    public E entity() {
        return getEntity();
    }

    @Override
    public E getEntity() {
        return this.entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull Class<E> getType() {
        return (Class<E>) this.getClass();
    }

    public enum Kind {

        /**
         * A {@code CREATE} of an aggregate typically involves an {@code insert} on the aggregate root.
         */
        INSERT,

        /**
         * A {@code SAVE} of an aggregate typically involves an {@code insert} or {@code update} on the aggregate root plus
         * {@code insert}s, {@code update}s, and {@code delete}s on the other elements of an aggregate.
         */
        SAVE,

        /**
         * A {@code DELETE} of an aggregate typically involves a {@code delete} on all contained entities.
         */
        DELETE
    }
}
