package com.plate.auth.commons.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.auth.commons.utils.ContextUtils;
import org.springframework.data.domain.Persistable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface BaseEntity<T> extends Serializable, Persistable<T> {

    /**
     * 设置代码值。
     * <p>
     * 此方法提供了一个接口来为相关对象设置一个代码值。具体的实现可能会根据实际需求来决定如何处理这个代码值。
     * 由于这是一个默认方法，它为接口的实现提供了一种灵活的方式来处理代码值，而不需要强制实现这个方法。
     *
     * @param code 要设置的代码值。这个参数允许调用者指定一个代码值，该值可以是任何字符串，具体的含义和使用方式取决于实现。
     */
    default void setCode(String code) {
        //todo 方法体为空，具体的实现可能需要根据实际需求来决定如何处理code参数。
    }

    /**
     * 判断当前对象是否为新对象，即是否具有ID。
     * 如果对象尚未分配ID，则将其视为新对象，并生成一个新的ID。
     * 此方法用于标识对象是否已存在于持久化存储中，如果没有ID，则认为是新对象需要进行持久化操作。
     *
     * @return 如果对象是新对象（没有ID），则返回true；否则返回false。
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

}