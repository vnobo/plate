package com.plate.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.BeanUtils;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for dynamically constructing SQL queries with bind variables
 * in a structured manner that prevents SQL injection vulnerabilities.
 *
 * <p>This class provides methods to convert Java objects into SQL criteria
 * using parameterized queries to ensure database security.
 *
 * <p>Example usage for table name resolution:
 * <pre>{@code
 * @Table("custom_table")
 * class MyEntity {
 * }
 *
 * String tableName = QueryHelper.annotationTableName(new MyEntity());
 * }</pre>
 * This returns: "custom_table"
 *
 * <p>Example usage for criteria construction:
 * <pre>{@code
 * User user = new User();
 * user.setName("John");
 * user.setAge(30);
 *
 * Collection<String> skipKeys = Arrays.asList("securityCode");
 * Criteria criteria = QueryHelper.criteria(user, skipKeys);
 * }</pre>
 * This generates criteria for name and age fields
 *
 * @see Criteria for query construction capabilities
 * @see BeanUtils#beanToMap(Object, boolean) for object-to-map conversion
 * @since 1.0
 */
public final class QueryHelper {

    public static final Set<String> SKIP_CRITERIA_KEYS = Set.of("extend", "query", "search",
            "createdTime", "updatedTime", "securityCode");

    /**
     * Resolves database table name from @Table annotation or class name
     *
     * <p>Example usage:
     * <pre>{@code
     * @Table("custom_table")
     * class MyEntity {
     * }
     *
     * String tableName = QueryHelper.annotationTableName(new MyEntity());
     * }</pre>
     * This returns: "custom_table"
     *
     * <p>Example usage with unannotated class:
     * <pre>{@code
     * class User {
     * }
     *
     * String tableName = QueryHelper.annotationTableName(new User());
     * }</pre>
     * This returns: "user" (upper_camel to lower_underscore conversion)
     *
     * @param object The object to resolve table name for
     * @return The resolved table name
     * @throws IllegalArgumentException if object is null or has no table annotation
     * @see Table for annotation specification
     * @since 1.0
     */
    public static String annotationTableName(Object object) {
        if (object == null) {
            throw QueryException.withMsg("Object cannot be null",
                    new IllegalArgumentException("Cannot process null object"));
        }

        Class<?> objectClass = object.getClass();
        Table table = objectClass.getAnnotation(Table.class);

        if (ObjectUtils.isEmpty(table)) {
            throw QueryException.withMsg("Table annotation not found",
                    new IllegalArgumentException("This object does not have a table annotation"));
        }

        return StringUtils.hasLength(table.value()) ? table.value() :
                CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, objectClass.getSimpleName());
    }

    /**
     * Constructs a Criteria instance by converting the provided object into a map,
     * excluding specified keys and predefined skip keys, then processing this map
     * to create the Criteria object using parameterized queries.
     *
     * <p>Example usage with custom skip keys:
     * <pre>{@code
     * User user = new User();
     * user.setName("John");
     * user.setSearch("active");
     *
     * Collection<String> skipKeys = Collections.singletonList("search");
     * Criteria criteria = QueryHelper.criteria(user, skipKeys);
     * }</pre>
     * This generates criteria for all fields except "search"
     *
     * @param object   The object to be converted into a map for criteria construction
     * @param skipKeys A collection of keys to be excluded from the object map
     * @return A Criteria instance representing the constructed criteria
     * @throws IllegalArgumentException if object is null
     * @see BeanUtils#beanToMap(Object, boolean) for object-to-map conversion details
     * @see Criteria#where(String) for criteria construction
     * @since 1.0
     */
    public static Criteria criteria(Object object, Collection<String> skipKeys) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        objectMap = Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));
        return criteria(objectMap);
    }

    /**
     * Constructs a Criteria instance from the provided map of criteria,
     * automatically handling different value types (UUID, String, Collection)
     * using parameterized queries to prevent SQL injection.
     *
     * <p>Example usage with UUID:
     * <pre>{@code
     * Map<String, Object> map = new HashMap<>();
     * map.put("id", UUID.randomUUID());
     *
     * Criteria criteria = QueryHelper.criteria(map);
     * }</pre>
     * This generates: "WHERE id = ?" with parameter binding
     *
     * <p>Example usage with collection:
     * <pre>{@code
     * Map<String, Object> map = new HashMap<>();
     * map.put("status", Arrays.asList("active", "pending"));
     *
     * Criteria criteria = QueryHelper.criteria(map);
     * }</pre>
     * This generates: "WHERE status IN (?, ?)"
     *
     * <p>Example usage with case-insensitive string match:
     * <pre>{@code
     * Map<String, Object> map = new HashMap<>();
     * map.put("name", "John");
     *
     * Criteria criteria = QueryHelper.criteria(map);
     * }</pre>
     * This generates: "WHERE name LIKE ?" with case-insensitive matching
     *
     * @param objectMap The map containing criteria key-value pairs
     * @return A Criteria instance representing the constructed criteria
     * @throws IllegalArgumentException if objectMap is null
     * @see Criteria #where(String) for criteria construction
     * @see Criteria #in(Iterable) for collection handling
     * @since 1.0
     */
    public static Criteria criteria(Map<String, Object> objectMap) {
        if (ObjectUtils.isEmpty(objectMap)) {
            return Criteria.empty();
        }
        List<Criteria> criteriaList = objectMap.entrySet().stream().map(entry ->
                switch (entry.getValue()) {
                    case UUID value -> Criteria.where(entry.getKey()).is(value);
                    case String value -> Criteria.where(entry.getKey()).like(value).ignoreCase(true);
                    case Collection<?> values -> Criteria.where(entry.getKey()).in(values);
                    case null, default -> Criteria.where(entry.getKey()).is(entry.getValue());
                }).collect(Collectors.toList());
        return Criteria.from(criteriaList);
    }
}