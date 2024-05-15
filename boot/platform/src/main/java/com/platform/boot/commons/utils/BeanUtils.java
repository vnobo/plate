package com.platform.boot.commons.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.common.collect.Maps;
import com.platform.boot.commons.exception.RestServerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Component
public final class BeanUtils implements InitializingBean {

    private final static ByteArrayOutputStream BYTE_ARRAY_OUTPUT_STREAM;
    private final static ObjectOutputStream OBJECT_OUTPUT_STREAM;

    static {
        try {
            BYTE_ARRAY_OUTPUT_STREAM = new ByteArrayOutputStream();
            OBJECT_OUTPUT_STREAM = new ObjectOutputStream(BYTE_ARRAY_OUTPUT_STREAM);
        } catch (IOException e) {
            throw RestServerException.withMsg("Init static ObjectOutputStream error.", e);
        }
    }

    public static DataSize MAX_IN_MEMORY_SIZE;

    public static void cachePut(Cache cache, String cacheKey, Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return;
        }
        DataSize objectSize = getBeanSize(obj);
        if (objectSize.toBytes() > MAX_IN_MEMORY_SIZE.toBytes()) {
            log.warn("Object size is too large, Max memory size is {}, Object size is {}.",
                    MAX_IN_MEMORY_SIZE, objectSize);
            return;
        }
        cache.put(cacheKey, obj);
    }

    public static String cacheKey(Object... objects) {
        int hashCode = Objects.hash(objects);
        return String.valueOf(hashCode);
    }

    public static DataSize getBeanSize(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            log.warn("Object is empty,This object not null.");
            return DataSize.ofBytes(0);
        }
        try {
            BYTE_ARRAY_OUTPUT_STREAM.reset();
            OBJECT_OUTPUT_STREAM.writeObject(obj);
            OBJECT_OUTPUT_STREAM.flush();
            return DataSize.ofBytes(BYTE_ARRAY_OUTPUT_STREAM.size());
        } catch (IOException e) {
            log.error("Bean Size IO exception! msg: {}", e.getLocalizedMessage());
            return DataSize.ofBytes(0);
        }
    }

    @Value("${spring.codec.max-in-memory-size:256kb}")
    public void setMaxInMemorySize(DataSize dataSize) {
        MAX_IN_MEMORY_SIZE = dataSize;
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
            nullKeys = Maps.filterEntries(targetMap, entry -> ObjectUtils.isEmpty(entry.getValue())).keySet().toArray(String[]::new);
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
        var type = new TypeReference<Map<String, Object>>() {
        };
        return objectMapper.convertValue(bean, type);
    }

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing utils [BeanUtils] ...");
    }
}