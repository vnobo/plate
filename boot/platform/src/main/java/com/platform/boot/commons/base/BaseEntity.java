package com.platform.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Set;

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
     * Gets the code of the entity.
     *
     * @return the code of the entity
     */
    default String getCode() {
        return null;
    }

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
            if (ObjectUtils.isEmpty(getCode())) {
                setCode(ContextUtils.nextId());
            }
        }
        return isNew;
    }

    /**
     * 默认的查询条件
     *
     * @return 返回一个Criteria对象
     */
    default Criteria criteria(Set<String> skipKeys) {
        return CriteriaUtils.build(this, skipKeys);
    }
}