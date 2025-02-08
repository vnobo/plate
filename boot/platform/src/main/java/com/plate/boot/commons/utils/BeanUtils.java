package com.plate.boot.commons.utils;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.exception.JsonException;
import com.plate.boot.commons.exception.JsonPointerException;
import com.plate.boot.security.core.UserAuditor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Retrieves a list of property names from the source object that have null values.
 * This method is particularly useful when needing to filter out null properties during a copy operation.
 */
@Log4j2
@Component
public final class BeanUtils implements InitializingBean {

    /**
     * Represents the maximum size of data that can be held in memory.
     * This threshold is utilized to determine when data should be processed differently,
     * such as being written to disk, to avoid exceeding memory constraints.
     */
    public static DataSize MAX_IN_MEMORY_SIZE;

    /**
     * Converts a JSON node at a specified JSON Path into an object of the provided class type.
     * <p>
     * This method takes a JsonNode, a JSON Path string, and a target class type, then navigates to the
     * JSON node located at the specified path within the input JSON. If the node exists, it is converted
     * into an instance of the target class. If the path does not exist or conversion fails, exceptions are thrown.
     *
     * @param <T>   The type of the object to be returned.
     * @param json  The JsonNode from which to extract data.
     * @param path  A comma-separated string representing the JSON Path to the desired node.
     * @param clazz The class of the type that the JSON node should be converted into.
     * @return An instance of the specified class containing the data from the JSON node at the given path.
     * @throws JsonException If the JSON Pointer path does not exist in the JSON structure,
     *                       or if there is an issue converting the JsonNode to the target class.
     */
    public static <T> T jsonPathToBean(JsonNode json, String path, Class<T> clazz) {

        String[] paths = StringUtils.commaDelimitedListToStringArray(path);
        StringJoiner pathJoiner = new StringJoiner("/");
        for (String p : paths) {
            pathJoiner.add(p);
        }
        JsonPointer jsonPointer = JsonPointer.valueOf(pathJoiner.toString());
        JsonNode valueNode = json.at(jsonPointer);
        if (valueNode.isMissingNode()) {
            throw JsonPointerException.withError("Json pointer path error, path: {}" + pathJoiner,
                    new IllegalArgumentException(pathJoiner + " is not found in the json [" + json + "]"));
        }
        try {
            return ContextUtils.OBJECT_MAPPER.convertValue(valueNode, clazz);
        } catch (IllegalArgumentException e) {
            throw JsonPointerException.withError("Json pointer covert error", e);
        }
    }

    /**
     * Converts the given object into a byte array using JSON serialization.
     *
     * @param <T>    The type of the object to be serialized.
     * @param object The object instance to be converted into bytes.
     * @return A byte array representing the serialized form of the input object.
     * @throws JsonException If the object cannot be serialized into JSON.
     */
    public static <T> byte[] objectToBytes(T object) {
        try {
            return ContextUtils.OBJECT_MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw JsonException.withError("Json processing exception", e);
        }
    }

    /**
     * Generates a cache key based on the hash codes of the provided objects.
     * This method is useful for creating unique keys for caching purposes when the key is derived from multiple parameters.
     *
     * @param objects A variable number of objects whose hash codes will be combined to form the cache key.
     * @return A string representation of the combined hash code, serving as a unique cache key.
     */
    public static String cacheKey(Object... objects) {
        StringJoiner keyJoiner = new StringJoiner("&");
        for (var obj : objects) {
            if (obj instanceof Pageable pageable) {
                keyJoiner.add(pageable.getPageNumber() + "_" + pageable.getPageSize());
                for (var sort : pageable.getSort()) {
                    keyJoiner.add(sort.getProperty() + "_" + sort.getDirection().name());
                }
            }
            var objMap = BeanUtils.beanToMap(obj, true);
            var setStr = objMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toSet());
            setStr.forEach(keyJoiner::add);
        }
        return ContextUtils.encodeToSHA256(keyJoiner.toString());
    }

    /**
     * Inserts an object into the specified cache if its size does not exceed the maximum allowed in-memory size.
     *
     * @param cache    The cache instance where the object will be stored.
     * @param cacheKey The key under which the object will be stored in the cache.
     * @param obj      The object to be cached. Its size will be evaluated against the maximum allowed size.
     *                 If the object exceeds the limit, it will not be cached and a warning will be logged.
     */
    public static void cachePut(Cache cache, String cacheKey, Object obj) {
        DataSize objectSize = getBeanSize(obj);
        if (objectSize.toBytes() > MAX_IN_MEMORY_SIZE.toBytes()) {
            log.warn("Object size is too large, Max memory size is {}, Object size is {}.",
                    MAX_IN_MEMORY_SIZE, objectSize);
            return;
        }
        cache.put(cacheKey, obj);
    }

    /**
     * Calculates the size of the given Java bean by serializing it into a byte array and measuring its length.
     * This method provides an estimate of the space the object occupies when serialized.
     *
     * @param obj The Java bean object whose size is to be calculated. Must not be null.
     * @return The size of the bean as a {@link DataSize} object, representing the size in a human-readable format.
     * If the object is empty or serialization fails, returns a DataSize of 0 bytes.
     * @throws IllegalArgumentException If the provided object is null, since null cannot be sized.
     */
    public static DataSize getBeanSize(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            log.warn("Object is empty,This object not null.");
            return DataSize.ofBytes(0);
        }
        try {
            int size = objectToBytes(obj).length;
            return DataSize.ofBytes(size);
        } catch (Exception e) {
            log.error("Bean Size IO exception! msg: {}", e.getLocalizedMessage());
            return DataSize.ofBytes(0);
        }
    }

    /**
     * Copies the properties from the source object to a new instance of the specified target class.
     * This method utilizes Spring's BeanUtils to perform the deep copy, allowing for copying nested properties as well.
     *
     * @param <T>    The type of the target class.
     * @param source The source object from which properties are to be copied.
     * @param clazz  The class of the target object to be instantiated and populated with the source's properties.
     * @return A new instance of the target class with properties copied from the source object.
     * @throws IllegalArgumentException If the source is null or the clazz cannot be instantiated.
     */
    public static <T> T copyProperties(Object source, Class<T> clazz) {
        T target = org.springframework.beans.BeanUtils.instantiateClass(clazz);
        BeanUtils.copyProperties(source, target, true);
        return target;
    }

    /**
     * Copies the properties from the source object to the target object.
     *
     * @param source The source object whose properties are to be copied.
     * @param target The target object where the properties from the source will be set.
     */
    public static void copyProperties(Object source, Object target) {
        BeanUtils.copyProperties(source, target, false);
    }

    /**
     * Copies properties from the source object to the target object.
     * Optionally, properties with null values can be ignored during the copy process.
     *
     * @param source          The source object from which properties are to be copied.
     * @param target          The target object to which properties are to be copied.
     * @param ignoreNullValue If true, properties with null values in the source object will not be copied to the target object.
     *                        If false, all properties including those with null values are copied.
     */
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

    /**
     * Converts a JavaBean object into a Map representation.
     *
     * @param <T>  The type of the bean.
     * @param bean The JavaBean object to be converted.
     * @return A Map containing the properties of the JavaBean with keys as String and values as Object.
     * The returned Map reflects the properties of the input bean including any nested beans.
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        return BeanUtils.beanToMap(bean, false);
    }

    /**
     * Converts a JavaBean object into a Map, with keys representing the property names
     * and values representing the corresponding property values.
     *
     * @param <T>             The type of the bean to convert.
     * @param bean            The JavaBean object to be converted into a Map.
     * @param ignoreNullValue If true, properties with null values will not be included in the Map.
     * @return A Map where each key-value pair corresponds to a property name and its value from the input bean.
     * If ignoreNullValue is true, properties with null values are excluded.
     */
    public static <T> Map<String, Object> beanToMap(T bean, final boolean ignoreNullValue) {
        return BeanUtils.beanToMap(bean, false, ignoreNullValue);
    }

    /**
     * Converts a JavaBean object into a Map, with options to transform field names to snake_case and ignore null values.
     *
     * @param <T>               The type of the bean to convert.
     * @param bean              The JavaBean object to be converted into a Map.
     * @param isToUnderlineCase If true, converts camelCase keys in the Map to snake_case. Defaults to false.
     * @param ignoreNullValue   If true, excludes keys with null values from the resulting Map. Defaults to false.
     * @return A Map representation of the input JavaBean, optionally with keys transformed to snake_case and null values excluded.
     */
    public static <T> Map<String, Object> beanToMap(T bean, final boolean isToUnderlineCase, final boolean ignoreNullValue) {
        if (ObjectUtils.isEmpty(bean)) {
            return null;
        }
        PropertyDescriptor[] propertyDescriptors = org.springframework.beans.BeanUtils
                .getPropertyDescriptors(bean.getClass());
        Map<String, Object> resultMap = Maps.newHashMap();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getReadMethod() == null) {
                continue;
            }
            try {
                ReflectionUtils.makeAccessible(propertyDescriptor.getReadMethod());
                var method = propertyDescriptor.getReadMethod();
                if (method == null) {
                    continue;
                }
                var readValue = method.invoke(bean);
                if (ignoreNullValue && ObjectUtils.isEmpty(readValue)) {
                    continue;
                }
                var key = isToUnderlineCase ? CaseFormat.LOWER_CAMEL
                        .to(CaseFormat.LOWER_UNDERSCORE, propertyDescriptor.getName()) : propertyDescriptor.getName();
                resultMap.put(key, readValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("BeanUtils beanToMap getReadMethod for property [{}]  error", propertyDescriptor.getName(), e);
                continue;
            }
        }
        return resultMap;
    }

    /**
     * Serializes the {@link UserAuditor} objects contained within the properties of the given input object.
     * This method inspects the object's properties to identify those of type {@link UserAuditor} and applies
     * a serialization process to them. It is designed to work with objects that may contain auditor fields
     * which need to be processed before further operations, such as saving to a database or sending over a network.
     *
     * @param <T>    The generic type of the object whose properties are to be serialized.
     * @param object The object whose properties are to be inspected and potentially serialized.
     * @return A {@link Mono} wrapping the original object after all {@link UserAuditor} properties have been processed.
     * The Mono completes when all processing is finished.
     */
    public static <T> Mono<T> serializeUserAuditor(T object) {
        PropertyDescriptor[] propertyDescriptors = org.springframework.beans.BeanUtils.getPropertyDescriptors(object.getClass());
        var propertyFlux = Flux.fromArray(propertyDescriptors)
                .filter(propertyDescriptor -> propertyDescriptor.getPropertyType() == UserAuditor.class)
                .flatMap(propertyDescriptor -> serializeUserAuditorProperty(object, propertyDescriptor));
        return propertyFlux.then(Mono.just(object));
    }

    /**
     * Handles the property descriptor for an object, particularly focusing on serializing
     * a {@link UserAuditor} within the given object by fetching additional user details
     * and updating the object accordingly.
     *
     * @param <T>                The type of the object containing the property to be handled.
     * @param object             The object whose property described by {@code propertyDescriptor} is to be processed.
     * @param propertyDescriptor The descriptor of the property within {@code object} that refers to a {@link UserAuditor}.
     * @return A {@link Mono} emitting a message indicating success or failure:
     * - If the user auditor is empty, emits a warning message and completes.
     * - If successful in serializing the user auditor, emits a success message and completes.
     * - Emits an error signal if there are issues invoking methods on the property descriptor.
     */
    private static <T> Mono<String> serializeUserAuditorProperty(T object, PropertyDescriptor propertyDescriptor) {
        UserAuditor userAuditor = (UserAuditor) ReflectionUtils.invokeMethod(propertyDescriptor.getReadMethod(), object);
        if (ObjectUtils.isEmpty(userAuditor)) {
            String msg = "User auditor is empty, No serializable." + propertyDescriptor.getName();
            log.warn(msg);
            return Mono.just(msg);
        }
        return ContextUtils.USERS_SERVICE.loadByCode(userAuditor.code()).cache().flatMap(user -> {
            ReflectionUtils.invokeMethod(propertyDescriptor.getWriteMethod(), object, UserAuditor.withUser(user));
            log.debug("{} serialize user auditor property: {}",
                    object.getClass().getSimpleName(), propertyDescriptor.getName());
            return Mono.just("User auditor serializable success. " + propertyDescriptor.getName());
        });
    }

    /**
     * Sets the maximum size of data that can be stored in memory before being written to disk.
     * This method allows configuration of the maximum in-memory size limit, which is particularly
     * useful for managing memory usage when handling large amounts of data, such as file uploads.
     *
     * @param dataSize The maximum in-memory size limit defined as a {@link DataSize}. Defaults to 256 kilobytes if not explicitly set.
     */
    @Value("${spring.codec.max-in-memory-size:256kb}")
    public void setMaxInMemorySize(DataSize dataSize) {
        MAX_IN_MEMORY_SIZE = dataSize;
    }

    /**
     * Initializes the utils bean by performing necessary setup steps.
     * This method is called after all properties of this bean have been set.
     * It is part of the Spring Framework's InitializingBean interface contract.
     * <p>
     * Logs a message indicating the start of the initialization process for BeanUtils.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing utils [BeanUtils]...");
    }
}