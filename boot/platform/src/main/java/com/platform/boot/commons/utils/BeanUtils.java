package com.platform.boot.commons.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public class BeanUtils {

    /**
     * 生成缓存键的方法
     *
     * @param objects 可变参数，用于生成缓存键的对象
     * @return 生成的缓存键字符串
     */
    public static String cacheKey(Object... objects) {
        // Convert objects to a map using ObjectMapper
        StringBuilder keyBuilder = new StringBuilder();
        for (Object object : objects) {
            // Convert object to a map using ObjectMapper
            Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
            // Check if the object map is empty
            if (ObjectUtils.isEmpty(objectMap)) {
                // Append the class name of the object to the key builder
                keyBuilder.append(object.getClass().getName()).append("&");
                continue;
            }
            // Append each key-value pair from the object map to the key builder
            objectMap.forEach((k, v) -> keyBuilder.append(k).append("=").append(v).append("&"));
        }
        // Return the final cache key as a string
        return keyBuilder.toString();
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
        org.springframework.beans.BeanUtils.copyProperties(source, target);
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

    /**
     * 如果源对象不为空，则将源对象复制到目标对象。
     * 忽略源对象中的空属性。
     *
     * @param source          源对象
     * @param target          目标对象
     * @param ignoreNullValue 是否忽略空值
     */
    public static void copyProperties(Object source, Object target, boolean ignoreNullValue) {
        Map<String, Object> targetMap = BeanUtils.beanToMap(source);
        String[] nullKeys = new String[0];
        if (ignoreNullValue) {
            nullKeys = targetMap.entrySet().stream()
                    .filter(entry -> ObjectUtils.isEmpty(entry.getValue()))
                    .map(Map.Entry::getKey).distinct().toArray(String[]::new);
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