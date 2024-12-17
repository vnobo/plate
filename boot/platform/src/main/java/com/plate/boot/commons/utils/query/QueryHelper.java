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

    public static final Set<String> SKIP_CRITERIA_KEYS = Set.of("extend",
            "createdTime", "updatedTime", "securityCode", "query", "search");

    public static QueryFragment query(Object object, Pageable pageable) {
        return query(object, pageable, List.of(), null);
    }

    public static QueryFragment query(Object object, Collection<String> skipKeys) {
        return query(object, Pageable.ofSize(25), skipKeys, null);
    }

    public static QueryFragment query(Object object, Collection<String> skipKeys, String prefix) {
        return query(object, Pageable.ofSize(25), skipKeys, prefix);
    }

    public static QueryFragment query(Object object, Pageable pageable, String prefix) {
        return query(object, pageable, List.of(), prefix);
    }

    public static QueryFragment query(Object object, Pageable pageable, Collection<String> skipKeys, String prefix) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        Map<String, Object> filterMap = ObjectUtils.isEmpty(objectMap) ? Map.of() :
                Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));

        QueryFragment queryFragment = QueryFragment.withMap(pageable.getPageSize(), pageable.getOffset(), filterMap);
        QueryHelper.applySort(queryFragment, pageable.getSort(), prefix);
        QueryHelper.applyWhere(queryFragment, prefix);
        QueryHelper.applyQuerySql(queryFragment, object);

        if (ObjectUtils.isEmpty(objectMap)) {
            return queryFragment;
        }

        processQueryKey(queryFragment, objectMap, prefix);
        processSecurityCodeKey(queryFragment, objectMap, skipKeys, prefix);
        processSearchKey(queryFragment, objectMap, prefix);

        return queryFragment;
    }

    @SuppressWarnings("unchecked")
    private static void processQueryKey(QueryFragment queryFragment, Map<String, Object> objectMap, String prefix) {
        if (objectMap.containsKey("query")) {
            var jsonMap = (Map<String, Object>) objectMap.get("query");
            var jsonQueryFragment = QueryJsonHelper.queryJson(jsonMap, prefix);
            queryFragment.mergeWhere(jsonQueryFragment.getWhereSql());
            queryFragment.putAll(jsonQueryFragment);
        }
    }

    private static void processSecurityCodeKey(QueryFragment queryFragment, Map<String, Object> objectMap, Collection<String> skipKeys, String prefix) {
        if (!skipKeys.contains("securityCode") && objectMap.containsKey("securityCode")) {
            var key = StringUtils.hasLength(prefix) ? prefix + ".tenant_code" : "tenant_code";
            queryFragment.addWhere(key + " LIKE :securityCode");
            queryFragment.put("securityTypeCode", objectMap.get("securityCode"));
        }
    }

    private static void processSearchKey(QueryFragment queryFragment, Map<String, Object> objectMap, String prefix) {
        if (objectMap.containsKey("search") && !ObjectUtils.isEmpty(objectMap.get("search"))) {
            var textSearch = (String) objectMap.get("search");
            var column = StringUtils.hasLength(prefix) ? prefix + ".text_search" : "text_search";
            queryFragment.addColumn("TS_RANK_CD(" + column + ", queryTextSearch) AS rank");
            queryFragment.addQuery(",TO_TSQUERY('chinese',:textSearch) queryTextSearch");
            queryFragment.addWhere(column + " @@ TO_TSQUERY('chinese',:textSearch)");
            queryFragment.put("textSearch", textSearch);
        }
    }

    public static void applySort(QueryFragment queryFragment, Sort sort, String prefix) {
        sort = QueryJsonHelper.transformSortForJson(sort);
        for (Sort.Order order : sort) {
            String sortedPropertyName = order.getProperty();
            String sortedProperty = order.isIgnoreCase() ? "LOWER(" + sortedPropertyName + ")" : sortedPropertyName;
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            queryFragment.addOrder(sortedProperty + (order.isAscending() ? " ASC" : " DESC"));
        }
    }

    public static void applyWhere(QueryFragment queryFragment, String prefix) {
        for (Map.Entry<String, Object> entry : queryFragment.entrySet()) {
            String conditionSql = buildConditionSql(entry, prefix);
            queryFragment.addWhere(conditionSql);
        }
    }

    /**
     * Applies the query SQL based on the provided object's table annotation.
     * If the object does not have a table annotation, it throws a QueryException.
     *
     * @param queryFragment The QueryFragment to which the query SQL will be applied.
     * @param object        The object containing the table annotation and data for the query.
     * @throws QueryException If the object does not have a table annotation.
     */
    public static void applyQuerySql(QueryFragment queryFragment, Object object) {
        Class<?> objectClass = object.getClass();
        Table table = objectClass.getAnnotation(Table.class);

        if (ObjectUtils.isEmpty(table)) {
            throw QueryException.withMsg("Table annotation not found",
                    "This object does not have a table annotation");
        }

        String tableName = StringUtils.hasLength(table.value()) ? table.value() : objectClass.getName();
        queryFragment.addColumn("*");
        queryFragment.addQuery(tableName);
    }

    /**
     * Constructs a QueryCondition based on a map entry consisting of a column name and its value.
     * The method dynamically determines the SQL condition (LIKE, IN, or EQ) based on the value's type,
     * applies an optional prefix to the column name, and prepares the condition for use in a query.
     *
     * @param entry  A map entry where the key is the column name in camelCase format
     *               and the value is the filter criterion which can be a String, Collection, or other type.
     * @param prefix An optional prefix to prepend to the column name, useful for specifying table aliases or namespaces.
     * @return A QueryCondition object containing:
     * - The operation keyword and its associated SQL syntax as a Map.Entry<String, String>.
     * - The SQL fragment representing the condition with placeholders for parameters.
     * - A map of parameters mapping placeholders to the actual filter values.
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
     * the Criteria object. The method removes keys listed in the predefined
     * SKIP_CRITERIA_KEYS set as well as any additional keys specified in the
     * skipKes collection from the object map before constructing the Criteria.
     *
     * @param object   The Java object to convert into Criteria. Its properties will form the basis of the Criteria.
     * @param skipKeys A collection of strings representing keys to exclude from the object during conversion.
     *                 These are in addition to the default skipped keys predefined in SKIP_CRITERIA_KEYS.
     * @return A Criteria instance representing the processed object, excluding the specified keys.
     */
    public static Criteria criteria(Object object, Collection<String> skipKeys) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        objectMap = Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));
        return criteria(objectMap);
    }

    /**
     * Constructs a Criteria instance from a map of search criteria.
     * Each entry in the map represents a search criterion where the key is the field name
     * and the value is the criterion value. The method supports String patterns with 'like',
     * collections of values with 'in', and direct value matching for other types.
     *
     * @param objectMap A map mapping field names to their respective search criterion values.
     *                  String values will be treated for 'like' matching,
     *                  Collections will be used for 'in' clauses, and all other types for equality.
     * @return A Criteria instance representing the combined search criteria.
     * Returns an empty Criteria if the input map is null or empty.
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