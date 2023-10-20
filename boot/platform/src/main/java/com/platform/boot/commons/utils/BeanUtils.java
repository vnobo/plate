package com.platform.boot.commons.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.Maps;
import com.platform.boot.commons.annotation.exception.JsonException;
import com.platform.boot.commons.annotation.exception.RestServerException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public class BeanUtils {
    private static final ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream();
    private static final ObjectOutputStream OBJECT_OUTPUT_STREAM;

    static {
        try {
            OBJECT_OUTPUT_STREAM = new ObjectOutputStream(BYTE_ARRAY_OUTPUT_STREAM);
        } catch (IOException e) {
            throw RestServerException.withMsg("Init static ObjectOutputStream error.", e);
        }
    }

    public static DataSize getBeanSize(Object obj) {
        try {
            BYTE_ARRAY_OUTPUT_STREAM.reset();
            OBJECT_OUTPUT_STREAM.writeObject(obj);
            OBJECT_OUTPUT_STREAM.flush();
            return DataSize.ofBytes(BYTE_ARRAY_OUTPUT_STREAM.size());
        } catch (IOException e) {
            throw JsonException.withError(e);
        }
    }

    /**
     * This is a static method that copies the properties of a given source object to a given target object.
     *
     * @param <T>    The type of the target object
     * @param source The source object to be copied
     * @param clazz  The class of the target object
     * @return T The target object with the properties of the source object
     */
    public static <T> T copyProperties(Object source, Class<T> clazz) {
        T target = org.springframework.beans.BeanUtils.instantiateClass(clazz);
        BeanUtils.copyProperties(source, target, false);
        return target;
    }

    /**
     * copy source to target object if source is not null.
     * ignore source null properties.
     *
     * @param source source object
     * @param target target object
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, true);
    }

    public static void copyProperties(Object source, Object target, boolean ignoreNullValue) {
        Map<String, Object> targetMap = BeanUtils.beanToMap(source);
        String[] nullKeys = new String[0];
        if (ignoreNullValue) {
            nullKeys = Maps.filterEntries(targetMap, entry -> ObjectUtils.isEmpty(entry.getValue()))
                    .keySet().toArray(String[]::new);
        }
        org.springframework.beans.BeanUtils.copyProperties(source, target, nullKeys);
    }

    /**
     * This method is used to convert a java bean to a map.
     * Default no ignore Null Value.
     *
     * @param bean bean to be converted
     * @param <T>  generic type
     * @return result of conversion as a map
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        return BeanUtils.beanToMap(bean, false);
    }

    /**
     * This method is used to convert a java bean to a map.
     *
     * @param bean            bean to be converted
     * @param ignoreNullValue boolean flag to ignore null values
     * @param <T>             generic type
     * @return result of conversion as a map
     */
    public static <T> Map<String, Object> beanToMap(T bean, final boolean ignoreNullValue) {
        return BeanUtils.beanToMap(bean, false, ignoreNullValue);
    }

    /**
     * This method is used to convert Java bean to Map
     *
     * @param bean              Java bean to be converted
     * @param ignoreNullValue   whether to ignore null value in bean or not
     * @param isToUnderlineCase determines if the converted Map should have underlined keys
     * @param <T>               type of the bean
     * @return a Map containing the values from the bean
     */
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

}