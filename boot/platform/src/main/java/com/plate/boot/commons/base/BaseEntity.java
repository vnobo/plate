package com.plate.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.query.CriteriaUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * Represents the base entity contract for entities that require common functionality
 * such as having a unique code, being serializable, and persistable with a generic type identifier.
 * Implementing classes should provide concrete behavior for these base operations.
 */
public interface BaseEntity<T> extends Serializable, Persistable<T> {

    /**
     * Sets the unique code for an entity.
     * This method is intended to assign or update the code attribute of an entity,
     * which serves as a unique identifier in accordance with the BaseEntity interface.
     * The implementation should handle the logic of how the code is generated or validated,
     * based on specific business rules, which may involve prefixing, formatting, or checking for uniqueness.
     *
     * @param code The code to be set for the entity. It could be a plain string or follow a predefined format.
     */
    default void setCode(String code) {
    }

    /**
     * Determines whether the entity instance is considered new.
     * This is typically used to decide if the entity needs to be inserted or updated when persisted.
     * The method checks if the identifier ({@code getId()}) is empty to assess newness.
     * If the entity is determined to be new, it generates and assigns a new unique code ({@code setCode(ContextUtils.nextId());}).
     *
     * @return {@code true} if the entity is considered new (i.e., its identifier is empty), otherwise {@code false}.
     */
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

    /**
     * Constructs a {@link Criteria} instance based on the current entity,
     * allowing for the specification of properties to be excluded from criteria creation.
     *
     * @param skipKeys A {@link Collection} of {@link String} property keys to be skipped when building the criteria.
     *                 These properties will not be included in the generated criteria.
     * @return A {@link Criteria} object tailored according to the current entity,
     *         excluding the properties specified in the {@code skipKeys} collection.
     */
    default Criteria criteria(Collection<String> skipKeys) {
        // 调用CriteriaUtils的静态方法build来创建Criteria对象，并传入当前对象和要忽略的属性键集合。
        return CriteriaUtils.build(this, skipKeys);
    }
}