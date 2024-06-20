package com.platform.boot.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.boot.commons.utils.ContextUtils;
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

    default void setCode(String code) {
    }

    @Override
    @JsonIgnore
    default boolean isNew() {
        boolean isNew = ObjectUtils.isEmpty(getId());
        if (isNew) {
            setCode(ContextUtils.nextId());
        }
        return isNew;
    }

    default Criteria criteria(Collection<String> skipKeys) {
        return CriteriaUtils.build(this, skipKeys);
    }
}