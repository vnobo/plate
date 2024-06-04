package com.platform.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.boot.commons.utils.Ulid;
import com.platform.boot.commons.utils.query.CriteriaUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Collection;

/**
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
            setCode(Ulid.random());
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