package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to hold both SQL string segment and its related parameters.
 * This is particularly useful for dynamically constructing SQL queries
 * with bind variables in a structured manner.
 */
public final class QueryHelper {

    public static final Set<String> SKIP_CRITERIA_KEYS = Set.of("extend", "query", "search",
            "createdTime", "updatedTime", "securityCode");

    /**
     * Constructs a QueryFragment instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create the QueryFragment.
     * This method is used to dynamically build SQL queries based on the given object's properties,
     * allowing for flexible and secure from construction.
     *
     * <p>The method's workflow is as follows:
     * <ul>
     *     <li>Converts the provided object into a map representation, excluding keys specified in
     *         {@link QueryHelper#SKIP_CRITERIA_KEYS} and any additional keys provided via the method parameters.</li>
     *     <li>Filters out the excluded keys from the map.</li>
     *     <li>Creates a QueryFragment instance with pagination information from the Pageable object.</li>
     *     <li>Applies sorting and WHERE conditions based on the object's properties.</li>
     *     <li>Generates the SQL from string by concatenating the columns, table name, WHERE clause, and ORDER BY clause.</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * UserReq userRequest = new UserReq();
     * userRequest.setUsername("john");
     * Pageable pageable = PageRequest.of(0, 10);
     * QueryFragment queryFragment = QueryHelper.from(userRequest, pageable);
     * String sqlQuery = queryFragment.querySql();
     * }
     * </pre>
     * In this example, a UserReq object is created with a username filter, and a Pageable object is defined for pagination.
     * The from method is then called to generate a QueryFragment, which can be used to execute a SQL from with pagination and filtering.
     *
     * @param object   The object to be converted into a map for from construction.
     * @param pageable The Pageable object containing pagination information.
     * @return A QueryFragment instance representing the constructed from.
     */
    public static QueryFragment query(Object object, Pageable pageable) {
        return query(object, pageable, List.of(), null);
    }

    /**
     * Constructs a QueryFragment instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create the QueryFragment.
     * This method is used to dynamically build SQL queries based on the given object's properties,
     * allowing for flexible and secure from construction.
     *
     * <p>The method's workflow is as follows:
     * <ul>
     *     <li>Converts the provided object into a map representation, excluding keys specified in
     *         {@link QueryHelper#SKIP_CRITERIA_KEYS} and any additional keys provided via the method parameters.</li>
     *     <li>Filters out the excluded keys from the map.</li>
     *     <li>Creates a QueryFragment instance with pagination information from a default Pageable object.</li>
     *     <li>Applies sorting and WHERE conditions based on the object's properties.</li>
     *     <li>Generates the SQL from string by concatenating the columns, table name, WHERE clause, and ORDER BY clause.</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * UserReq userRequest = new UserReq();
     * userRequest.setUsername("john");
     * QueryFragment queryFragment = QueryHelper.from(userRequest, List.of("username"));
     * String sqlQuery = queryFragment.querySql();
     * }
     * </pre>
     * In this example, a UserReq object is created with a username filter, and a collection of keys to be excluded is defined.
     * The from method is then called to generate a QueryFragment, which can be used to execute a SQL from with filtering.
     *
     * @param object   The object to be converted into a map for from construction.
     * @param skipKeys A collection of keys to be excluded from the object map.
     * @return A QueryFragment instance representing the constructed from.
     */
    public static QueryFragment query(Object object, Collection<String> skipKeys) {
        return query(object, Pageable.ofSize(25), skipKeys, null);
    }

    /**
     * Constructs a QueryFragment instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create the QueryFragment.
     * This method is used to dynamically build SQL queries based on the given object's properties,
     * allowing for flexible and secure from construction.
     *
     * <p>The method's workflow is as follows:
     * <ul>
     *     <li>Converts the provided object into a map representation, excluding keys specified in
     *         {@link QueryHelper#SKIP_CRITERIA_KEYS} and any additional keys provided via the method parameters.</li>
     *     <li>Filters out the excluded keys from the map.</li>
     *     <li>Creates a QueryFragment instance with pagination information from a default Pageable object.</li>
     *     <li>Applies sorting and WHERE conditions based on the object's properties.</li>
     *     <li>Generates the SQL from string by concatenating the columns, table name, WHERE clause, and ORDER BY clause.</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * UserReq userRequest = new UserReq();
     * userRequest.setUsername("john");
     * QueryFragment queryFragment = QueryHelper.from(userRequest, List.of("username"), "a");
     * String sqlQuery = queryFragment.querySql();
     * }
     * </pre>
     * In this example, a UserReq object is created with a username filter, and a collection of keys to be excluded is defined.
     * The from method is then called to generate a QueryFragment, which can be used to execute a SQL from with filtering.
     *
     * @param object   The object to be converted into a map for from construction.
     * @param skipKeys A collection of keys to be excluded from the object map.
     * @param prefix   An optional prefix to be applied to column names.
     * @return A QueryFragment instance representing the constructed from.
     */
    public static QueryFragment query(Object object, Collection<String> skipKeys, String prefix) {
        return query(object, Pageable.ofSize(25), skipKeys, prefix);
    }

    /**
     * Constructs a QueryFragment instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create the QueryFragment.
     * This method is used to dynamically build SQL queries based on the given object's properties,
     * allowing for flexible and secure from construction.
     *
     * <p>The method's workflow is as follows:
     * <ul>
     *     <li>Converts the provided object into a map representation, excluding keys specified in
     *         {@link QueryHelper#SKIP_CRITERIA_KEYS} and any additional keys provided via the method parameters.</li>
     *     <li>Filters out the excluded keys from the map.</li>
     *     <li>Creates a QueryFragment instance with pagination information from the Pageable object.</li>
     *     <li>Applies sorting and WHERE conditions based on the object's properties.</li>
     *     <li>Generates the SQL from string by concatenating the columns, table name, WHERE clause, and ORDER BY clause.</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * UserReq userRequest = new UserReq();
     * userRequest.setUsername("john");
     * Pageable pageable = PageRequest.of(0, 10);
     * QueryFragment queryFragment = QueryHelper.from(userRequest, pageable, "a");
     * String sqlQuery = queryFragment.querySql();
     * }
     * </pre>
     * In this example, a UserReq object is created with a username filter, and a Pageable object is defined for pagination.
     * The from method is then called to generate a QueryFragment, which can be used to execute a SQL from with pagination and filtering.
     *
     * @param object   The object to be converted into a map for from construction.
     * @param pageable The Pageable object containing pagination information.
     * @param prefix   An optional prefix to be applied to column names.
     * @return A QueryFragment instance representing the constructed from.
     */
    public static QueryFragment query(Object object, Pageable pageable, String prefix) {
        return query(object, pageable, List.of(), prefix);
    }

    /**
     * Constructs a QueryFragment instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create the QueryFragment.
     * This method is used to dynamically build SQL queries based on the given object's properties,
     * allowing for flexible and secure from construction.
     *
     * <p>The method's workflow is as follows:
     * <ul>
     *     <li>Converts the provided object into a map representation, excluding keys specified in
     *         {@link QueryHelper#SKIP_CRITERIA_KEYS} and any additional keys provided via the method parameters.</li>
     *     <li>Filters out the excluded keys from the map.</li>
     *     <li>Creates a QueryFragment instance with pagination information from the Pageable object.</li>
     *     <li>Applies sorting and WHERE conditions based on the object's properties.</li>
     *     <li>Generates the SQL from string by concatenating the columns, table name, WHERE clause, and ORDER BY clause.</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * UserReq userRequest = new UserReq();
     * userRequest.setUsername("john");
     * Pageable pageable = PageRequest.of(0, 10);
     * QueryFragment queryFragment = QueryHelper.from(userRequest, pageable, List.of("username"), "a");
     * String sqlQuery = queryFragment.querySql();
     * }
     * </pre>
     * In this example, a UserReq object is created with a username filter, a Pageable object is defined for pagination,
     * and a collection of keys to be excluded is provided. The from method is then called to generate a QueryFragment,
     * which can be used to execute a SQL from with pagination and filtering.
     *
     * @param object   The object to be converted into a map for from construction.
     * @param pageable The Pageable object containing pagination information.
     * @param skipKeys A collection of keys to be excluded from the object map.
     * @param prefix   An optional prefix to be applied to column names.
     * @return A QueryFragment instance representing the constructed from.
     */
    public static QueryFragment query(Object object, Pageable pageable, Collection<String> skipKeys, String prefix) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        Map<String, Object> filterMap = ObjectUtils.isEmpty(objectMap) ? Map.of() :
                Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));

        QueryFragment queryFragment = QueryFragment.withMap(pageable.getPageSize(), pageable.getOffset(), filterMap);
        applySort(queryFragment, pageable.getSort(), prefix);
        applyWhere(queryFragment, prefix);
        applyQuerySql(queryFragment, object);

        if (!ObjectUtils.isEmpty(objectMap)) {
            processQueryKey(queryFragment, objectMap, prefix);
            processSecurityCodeKey(queryFragment, objectMap, skipKeys, prefix);
            processSearchKey(queryFragment, objectMap, prefix);
        }
        return queryFragment;
    }

    /**
     * Processes the 'query' key in the object map and merges the resulting from fragment.
     *
     * @param queryFragment The QueryFragment to which the query conditions will be added.
     * @param objectMap     The map representation of the object containing the 'from' key.
     * @param prefix        An optional prefix to be applied to column names.
     */
    @SuppressWarnings("unchecked")
    private static void processQueryKey(QueryFragment queryFragment, Map<String, Object> objectMap, String prefix) {
        if (objectMap.containsKey("query")) {
            var jsonMap = (Map<String, Object>) objectMap.get("query");
            var jsonQueryFragment = QueryJsonHelper.queryJson(jsonMap, prefix);
            queryFragment.getWhere().merge(jsonQueryFragment.getWhere());
            queryFragment.putAll(jsonQueryFragment);
        }
    }

    /**
     * Processes the 'securityCode' key in the object map and adds the corresponding condition to the from fragment.
     *
     * @param queryFragment The QueryFragment to which the security code condition will be added.
     * @param objectMap     The map representation of the object containing the 'securityCode' key.
     * @param skipKeys      A collection of keys to be excluded from the object map.
     * @param prefix        An optional prefix to be applied to column names.
     */
    private static void processSecurityCodeKey(QueryFragment queryFragment, Map<String, Object> objectMap, Collection<String> skipKeys, String prefix) {
        if (!skipKeys.contains("securityCode") && objectMap.containsKey("securityCode")) {
            var column = StringUtils.hasLength(prefix) ? prefix + ".tenant_code" : "tenant_code";
            queryFragment.where(column + " LIKE :securityCode");
            queryFragment.put("securityCode", objectMap.get("securityCode"));
        }
    }

    /**
     * Processes the 'search' key in the object map and adds the corresponding condition to the from fragment.
     *
     * @param queryFragment The QueryFragment to which the search condition will be added.
     * @param objectMap     The map representation of the object containing the 'search' key.
     * @param prefix        An optional prefix to be applied to column names.
     */
    private static void processSearchKey(QueryFragment queryFragment, Map<String, Object> objectMap, String prefix) {
        if (objectMap.containsKey("search") && !ObjectUtils.isEmpty(objectMap.get("search"))) {
            var column = StringUtils.hasLength(prefix) ? prefix + ".text_search" : "text_search";
            queryFragment.ts(column, objectMap.get("search"));
        }
    }

    /**
     * Applies sorting to the QueryFragment based on the provided Sort object.
     * This method transforms the Sort object to handle JSON fields correctly,
     * constructs the SQL ORDER BY clause, and appends it to the QueryFragment.
     *
     * <p>The method's workflow is as follows:
     * <ul>
     *     <li>Transforms the Sort object to handle JSON fields using {@link QueryJsonHelper#transformSortForJson(Sort)}.</li>
     *     <li>Iterates over each Sort.Order in the Sort object.</li>
     *     <li>For each Sort.Order, constructs the SQL ORDER BY clause, considering case sensitivity and optional prefix.</li>
     *     <li>Appends the constructed ORDER BY clause to the QueryFragment.</li>
     * </ul>
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * Sort sort = Sort.by(Sort.Order.asc("username").ignoreCase(), Sort.Order.desc("createdTime"));
     * QueryFragment queryFragment = new QueryFragment();
     * QueryHelper.applySort(queryFragment, sort, "u");
     * }
     * </pre>
     * In this example, a Sort object is created with ascending order for the username field (case insensitive)
     * and descending order for the createdTime field. The applySort method is then called to append the ORDER BY
     * clause to the QueryFragment with the prefix "u".
     *
     * @param queryFragment The QueryFragment to which the sorting will be applied.
     * @param sort          The Sort object containing sorting information.
     * @param prefix        An optional prefix to be applied to column names.
     */
    public static void applySort(QueryFragment queryFragment, Sort sort, String prefix) {
        sort = QueryJsonHelper.transformSortForJson(sort);
        for (Sort.Order order : sort) {
            String sortedPropertyName = order.getProperty();
            String sortedProperty = order.isIgnoreCase() ? "LOWER(" + sortedPropertyName + ")" : sortedPropertyName;
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            queryFragment.orderBy(sortedProperty + (order.isAscending() ? " ASC" : " DESC"));
        }
    }

    /**
     * Applies where conditions to the QueryFragment based on its current entries.
     *
     * @param queryFragment The QueryFragment to which the where conditions will be applied.
     * @param prefix        An optional prefix to be applied to column names.
     */
    public static void applyWhere(QueryFragment queryFragment, String prefix) {
        for (Map.Entry<String, Object> entry : queryFragment.entrySet()) {
            String conditionSql = buildConditionSql(entry, prefix);
            queryFragment.where(conditionSql);
        }
    }

    /**
     * Applies the from SQL based on the provided object's table annotation.
     * If the object does not have a table annotation, it throws a QueryException.
     *
     * @param queryFragment The QueryFragment to which the from SQL will be applied.
     * @param object        The object containing the table annotation and data for the from.
     * @throws QueryException If the object does not have a table annotation.
     */
    public static void applyQuerySql(QueryFragment queryFragment, Object object) {
        Class<?> objectClass = object.getClass();
        Table table = objectClass.getAnnotation(Table.class);

        if (ObjectUtils.isEmpty(table)) {
            throw QueryException.withMsg("Table annotation not found",
                    new IllegalArgumentException("This object does not have a table annotation"));
        }

        String tableName = StringUtils.hasLength(table.value()) ? table.value() : objectClass.getName();
        queryFragment.columns("*");
        queryFragment.from(tableName);
    }

    /**
     * Constructs a SQL condition string based on the provided map entry and prefix.
     *
     * @param entry  The map entry containing the column name and value.
     * @param prefix An optional prefix to be applied to column names.
     * @return The constructed SQL condition string.
     */
    public static String buildConditionSql(Map.Entry<String, Object> entry, String prefix) {
        String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
        if (StringUtils.hasLength(prefix)) {
            columnName = prefix + "." + columnName;
        }
        Object value = entry.getValue();
        String paramName = ":" + entry.getKey();
        if (value instanceof String) {
            columnName = columnName + " LIKE " + paramName;
        } else if (value instanceof Collection<?>) {
            columnName = columnName + " IN (" + paramName + ")";
        } else {
            columnName = columnName + " = " + paramName;
        }
        return columnName;
    }

    /**
     * Constructs a Criteria instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create
     * the Criteria object.
     *
     * @param object   The object to be converted into a map for criteria construction.
     * @param skipKeys A collection of keys to be excluded from the object map.
     * @return A Criteria instance representing the constructed criteria.
     */
    public static Criteria criteria(Object object, Collection<String> skipKeys) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        objectMap = Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));
        return criteria(objectMap);
    }

    /**
     * Constructs a Criteria instance from the provided map of criteria.
     *
     * @param objectMap The map containing criteria key-value pairs.
     * @return A Criteria instance representing the constructed criteria.
     */
    public static Criteria criteria(Map<String, Object> objectMap) {
        if (ObjectUtils.isEmpty(objectMap)) {
            return Criteria.empty();
        }
        List<Criteria> criteriaList = objectMap.entrySet().parallelStream().map(entry -> {
            if (entry.getValue() instanceof String value) {
                return Criteria.where(entry.getKey()).like(String.format("%s", value)).ignoreCase(true);
            } else if (entry.getValue() instanceof Collection<?> values) {
                return Criteria.where(entry.getKey()).in(values);
            } else {
                return Criteria.where(entry.getKey()).is(entry.getValue());
            }
        }).collect(Collectors.toList());
        return Criteria.from(criteriaList);
    }
}