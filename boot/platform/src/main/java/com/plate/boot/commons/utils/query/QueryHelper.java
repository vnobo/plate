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

    /**
     * Constructs a QueryFragment instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create the QueryFragment.
     *
     * @param object   The object to be converted into a map for query construction.
     * @param pageable The Pageable object containing pagination information.
     * @param skipKeys A collection of keys to be excluded from the object map.
     * @param prefix   An optional prefix to be applied to column names.
     * @return A QueryFragment instance representing the constructed query.
     */
    public static QueryFragment query(Object object, Pageable pageable, Collection<String> skipKeys, String prefix) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        Map<String, Object> filterMap = Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));

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
     * Processes the 'query' key in the object map and merges the resulting query fragment.
     *
     * @param queryFragment The QueryFragment to which the query conditions will be added.
     * @param objectMap     The map representation of the object containing the 'query' key.
     * @param prefix        An optional prefix to be applied to column names.
     */
    @SuppressWarnings("unchecked")
    private static void processQueryKey(QueryFragment queryFragment, Map<String, Object> objectMap, String prefix) {
        if (objectMap.containsKey("query")) {
            var jsonMap = (Map<String, Object>) objectMap.get("query");
            var jsonQueryFragment = QueryJsonHelper.queryJson(jsonMap, prefix);
            queryFragment.mergeWhere(jsonQueryFragment.getWhereSql());
            queryFragment.putAll(jsonQueryFragment);
        }
    }

    /**
     * Processes the 'securityCode' key in the object map and adds the corresponding condition to the query fragment.
     *
     * @param queryFragment The QueryFragment to which the security code condition will be added.
     * @param objectMap     The map representation of the object containing the 'securityCode' key.
     * @param skipKeys      A collection of keys to be excluded from the object map.
     * @param prefix        An optional prefix to be applied to column names.
     */
    private static void processSecurityCodeKey(QueryFragment queryFragment, Map<String, Object> objectMap, Collection<String> skipKeys, String prefix) {
        if (!skipKeys.contains("securityCode") && objectMap.containsKey("securityCode")) {
            var column = StringUtils.hasLength(prefix) ? prefix + ".tenant_code" : "tenant_code";
            queryFragment.addWhere(column + " LIKE :securityCode");
            queryFragment.put("securityCode", objectMap.get("securityCode"));
        }
    }

    /**
     * Processes the 'search' key in the object map and adds the corresponding condition to the query fragment.
     *
     * @param queryFragment The QueryFragment to which the search condition will be added.
     * @param objectMap     The map representation of the object containing the 'search' key.
     * @param prefix        An optional prefix to be applied to column names.
     */
    private static void processSearchKey(QueryFragment queryFragment, Map<String, Object> objectMap, String prefix) {
        if (objectMap.containsKey("search") && !ObjectUtils.isEmpty(objectMap.get("search"))) {
            var textSearch = (String) objectMap.get("search");
            var column = StringUtils.hasLength(prefix) ? prefix + ".text_search" : "text_search";
            queryFragment.addColumn("TS_RANK_CD(" + column + ", queryTextSearch) AS rank");
            queryFragment.addQuery(",TO_TSQUERY('chinese',:textSearch) queryTextSearch");
            queryFragment.addWhere(column + "@@TO_TSQUERY('chinese',:textSearch)");
            queryFragment.put("textSearch", textSearch);
        }
    }

    /**
     * Applies sorting to the QueryFragment based on the provided Sort object.
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
            queryFragment.addOrder(sortedProperty + (order.isAscending() ? " ASC" : " DESC"));
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
                    new IllegalArgumentException("This object does not have a table annotation"));
        }

        String tableName = StringUtils.hasLength(table.value()) ? table.value() : objectClass.getName();
        queryFragment.addColumn("*");
        queryFragment.addQuery(tableName);
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