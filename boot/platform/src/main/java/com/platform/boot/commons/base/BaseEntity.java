package com.platform.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Collection;

/**
 * This interface defines the base entity for all entities in the application.
 * It extends the Spring Data Perishable interface and adds additional functionality.
 * <p>
 * The setCode method is used to set the code of the entity.
 * <p>
 * The isNew method is used to determine if the entity is new or not.
 * If it is new, it sets the code of the entity using the ContextHolder utility class.
 *
 * @param <T> the type of the entity's id
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface BaseEntity<T> extends Serializable, Persistable<T> {

    /**
     * Sets the code of the entity.
     *
     * @param code the code to set
     */
    default void setCode(String code) {
    }

    /**
     * Determines if the entity is new or not.
     * If it is new, it sets the code of the entity using the ContextHolder utility class.
     *
     * @return true if the entity is new, false otherwise
     */
    @Override
    @JsonIgnore
    default boolean isNew() {
        boolean isNew = ObjectUtils.isEmpty(getId());
        if (isNew) {
            setCode(ContextUtils.nextId());
        }
        return isNew;
    }

    /**
     * Method to convert this BaseEntity to a Criteria
     *
     * @param skipKeys the keys to skip
     * @return the criteria
     */
    default Criteria criteria(Collection<String> skipKeys) {
        return CriteriaUtils.build(this, skipKeys);
    }
}