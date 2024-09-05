package com.plate.auth.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.auth.commons.utils.ContextUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * Represents the base entity definition with common functionality shared across all entities.
 * This interface extends {@link Serializable} and {@link Persistable}, ensuring entities can be serialized
 * and have basic persistence-related operations defined.
 *
 * <p>{@code BaseEntity} introduces a default method to set a code value and overrides the `isNew` method
 * to determine if an entity instance is new (typically based on the absence of an identifier).
 * It also suggests a flexible way to handle entity identifiers through the `setCode` method.
 *
 * <h3>Key Methods:</h3>
 * <ul>
 *   <li>{@link #setCode(String)}: Assigns a code value to the entity, allowing customization of how codes are handled.</li>
 *   <li>{@link #isNew()}: Determines whether the entity instance represents a new record that needs to be persisted.</li>
 * </ul>
 *
 * @param <T> The type of the identifier for this entity.
 */
public interface BaseEntity<T> extends Serializable, Persistable<T> {

    /**
     * Assigns a code to the entity.
     *
     * This method is intended to set a unique code identifier for an entity. The implementation should define
     * how the `code` parameter is utilized, as the current implementation is a placeholder.
     *
     * @param code The code value to be assigned to the entity. The nature of this code is dependent on the
     *             business logic or system requirements where this interface is implemented.
     */
    default void setCode(String code) {
        //todo
    }

    /**
     * Determines whether the entity instance represents a new record that has not yet been persisted.
     * This method checks if the entity's identifier ({@code getId}) is empty to assess its novelty.
     * If the entity is deemed new, it assigns a new unique code*/
    @Override
    @JsonIgnore
    default boolean isNew() {
        // 判断对象是否为新对象，通过检查ID是否为空来确定
        boolean isNew = ObjectUtils.isEmpty(getId());
        if (isNew) {
            // 如果是新对象，则生成并设置一个新的ID
            setCode(ContextUtils.nextId());
        }
        return isNew;
    }

}