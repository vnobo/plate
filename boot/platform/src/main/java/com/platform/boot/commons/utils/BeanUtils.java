package com.platform.boot.commons.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.Maps;
import com.platform.boot.commons.exception.JsonException;
import com.platform.boot.commons.exception.RestServerException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Component
public class BeanUtils implements InitializingBean {
    private final static ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream();
    private final static ObjectOutputStream OBJECT_OUTPUT_STREAM;

    static {
        try {
            OBJECT_OUTPUT_STREAM = new ObjectOutputStream(BYTE_ARRAY_OUTPUT_STREAM);
        } catch (IOException e) {
            throw RestServerException.withMsg("Init static ObjectOutputStream error.", e);
        }
    }

    public static DataSize getBeanSize(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            throw RestServerException.withMsg("Object is empty!", "This object not null.");
        }
        try {
            BYTE_ARRAY_OUTPUT_STREAM.reset();
            OBJECT_OUTPUT_STREAM.writeObject(obj);
            OBJECT_OUTPUT_STREAM.flush();
            return DataSize.ofBytes(BYTE_ARRAY_OUTPUT_STREAM.size());
        } catch (IOException e) {
            throw JsonException.withError(e);
        }
    }

    public static <T> T copyProperties(Object source, Class<T> clazz) {
        T target = org.springframework.beans.BeanUtils.instantiateClass(clazz);
        BeanUtils.copyProperties(source, target, true);
        return target;
    }

    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, false);
    }

    public static void copyProperties(Object source, Object target, boolean ignoreNullValue) {
        Map<String, Object> targetMap = BeanUtils.beanToMap(source);
        String[] nullKeys = new String[0];
        if (ignoreNullValue) {
            nullKeys = Maps.filterEntries(targetMap, entry -> ObjectUtils.isEmpty(entry.getValue()))
                    .keySet().toArray(String[]::new);
        }
        if (nullKeys.length > 0) {
            org.springframework.beans.BeanUtils.copyProperties(source, target, nullKeys);
        } else {
            org.springframework.beans.BeanUtils.copyProperties(source, target);
        }
    }

    public static <T> Map<String, Object> beanToMap(T bean) {
        return BeanUtils.beanToMap(bean, false);
    }

    public static <T> Map<String, Object> beanToMap(T bean, final boolean ignoreNullValue) {
        return BeanUtils.beanToMap(bean, false, ignoreNullValue);
    }

    public static <T> Map<String, Object> beanToMap(T bean, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        if (ObjectUtils.isEmpty(bean)) {
            return null;
        }
        ObjectMapper objectMapper = ContextUtils.OBJECT_MAPPER.copy();
        if (isToUnderlineCase) {
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        }
        if (ignoreNullValue) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        return objectMapper.convertValue(bean, new TypeReference<>() {
        });
    }

    @Override
    public void afterPropertiesSet() {
    }
}