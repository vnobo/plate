package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.utils.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to hold both SQL string segment and its related parameters.
 * This is particularly useful for dynamically constructing SQL queries
 * with bind variables in a structured manner.
 */
public final class QueryHelper {

    public static final Set<String> SKIP_CRITERIA_KEYS = Set.of("extend",
            "createdTime", "updatedTime", "securityCode", "query");

    /**
     * Applies pagination to a SQL query string based on the provided {@link Pageable} object.
     * This is a convenience overload that delegates to {@link #applyPage(Pageable, String)}
     * with a null prefix, used primarily when no prefix is needed for generating the SQL LIMIT and OFFSET clauses.
     *
     * @param pageable The pagination information, including page size and offset.
     * @return A string representing the pagination part of the SQL query, i.e., "LIMIT {pageSize} OFFSET {offset}".
     */
    public static String applyPage(Pageable pageable) {
        return applyPage(pageable, null);
    }

    /**
     * Applies pagination to a SQL query string based on the provided {@link Pageable} object and an optional prefix.
     * Generates a LIMIT and OFFSET clause with the specified page size and offset from the {@link Pageable} instance,
     * optionally prefixed to align with a specific table alias or column name in the SQL query.
     *
     * @param pageable The pagination information containing the desired page size and offset for query pagination.
     * @param prefix   An optional prefix to be applied before the LIMIT and OFFSET placeholders in the SQL query.
     *                 This can be useful when addressing specific table aliases or column names.
     * @return A string appended with the pagination SQL segment, formatted as "LIMIT {pageSize} OFFSET {offset}",
     * with optional prefixing, ready to be integrated into a larger SQL query.
     */
    public static String applyPage(Pageable pageable, String prefix) {
        String orderSql = applySort(pageable.getSort(), prefix);
        return String.format(orderSql + " limit %d offset %d", pageable.getPageSize(), pageable.getOffset());
    }

    /**
     * Applies sorting to a SQL query string based on the provided {@link Sort} object, with an optional prefix for property names.
     * Transforms the sort orders into SQL-compatible sort clauses, considering case insensitivity and JSON field access notation.
     * If the sort is unsorted or null, defaults to sorting by 'id' in descending order.
     *
     * @param sort   The sorting criteria specifying the properties and directions for sorting.
     * @param prefix An optional prefix to prepend to each sorted property name, useful when dealing with table aliases or nested properties.
     * @return A string representing the sorting part of the SQL query, starting with "ORDER BY",
     * followed by comma-separated sort clauses, each in the format "property_name ASC/DESC".
     */
    public static String applySort(Sort sort, String prefix) {
        if (sort == null || sort.isUnsorted()) {
            return StringUtils.hasLength(prefix) ? " order by " + prefix + ".id desc" : " order by id desc";
        }
        sort = QueryJsonHelper.transformSortForJson(sort);
        StringJoiner sortSql = new StringJoiner(", ");
        for (Sort.Order order : sort) {
            String sortedPropertyName = order.getProperty();
            String sortedProperty = order.isIgnoreCase() ? "lower(" + sortedPropertyName + ")" : sortedPropertyName;
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            sortSql.add(sortedProperty + (order.isAscending() ? " asc" : " desc"));
        }
        return " order by " + sortSql;
    }

    /**
     * Constructs a QueryFragment for dynamic SQL WHERE clause generation based on an object's properties.
     * Excludes specified keys and allows for an optional prefix to be applied to column names.
     * This method also processes a special "query" property within the object, which contains
     * a nested map of conditions, and applies additional security-related conditions if present.
     *
     * @param object   The source object whose properties will be used to construct the query conditions.
     *                 Properties should map to filter values. A special property "query" can be used
     *                 to pass a nested map of conditions.
     * @param skipKeys A collection of strings representing property names to exclude from the query conditions.
     *                 These properties will not be included in the generated WHERE clause.
     * @param prefix   An optional prefix to prepend to each property name in the SQL query,
     *                 useful for specifying table aliases or namespaces.
     * @return A QueryFragment containing the concatenated SQL WHERE conditions and a map of parameters.
     * The SQL conditions are joined by 'and', and the parameters map binds placeholders to actual values.
     */
    @SuppressWarnings("unchecked")
    public static QueryFragment query(Object object, Collection<String> skipKeys, String prefix) {
        StringJoiner whereSql = new StringJoiner(" and ");
        Map<String, Object> bindParams = Maps.newHashMap();

        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        if (ObjectUtils.isEmpty(objectMap)) {
            return QueryFragment.of(whereSql, bindParams);
        }

        if (objectMap.containsKey("query")) {
            var jsonMap = (Map<String, Object>) objectMap.get("query");
            QueryFragment jsonQueryFragment = QueryJsonHelper.queryJson(jsonMap, prefix);
            whereSql.merge(jsonQueryFragment.sql());
            bindParams.putAll(jsonQueryFragment.params());
        }


        String securityCodeKey = "securityCode";
        if (!skipKeys.contains(securityCodeKey) && objectMap.containsKey(securityCodeKey)) {
            var condition = securityCondition(objectMap.get(securityCodeKey), prefix);
            whereSql.add(condition.sql());
            bindParams.putAll(condition.params());
        }

        objectMap = Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && !skipKeys.contains(key));
        if (!ObjectUtils.isEmpty(objectMap)) {
            QueryFragment entityQueryFragment = query(objectMap, prefix);
            whereSql.merge(entityQueryFragment.sql());
            bindParams.putAll(entityQueryFragment.params());
        }

        return QueryFragment.of(whereSql, bindParams);
    }

    private static QueryCondition securityCondition(Object value, String prefix) {
        String key = "tenant_code";
        if (StringUtils.hasLength(prefix)) {
            key = prefix + "." + key;
        }
        return new QueryCondition(key + " like :securityCode",
                Map.of("securityCode", value), null);
    }

    /**
     * Constructs a ParamSql instance for dynamic SQL WHERE clause generation
     * based on a provided map of column-value pairs. Supports optional prefixing
     * for column names to handle table aliases or nested properties. Determines
     * the SQL condition type (equality, 'like', or 'in') dynamically based on
     * the value's type, enabling flexible query construction.
     *
     * @param objectMap A map where keys represent column names (in camelCase)
     *                  and values are the criteria for filtering. Values can be
     *                  Strings, Collections, or other types supporting equality checks.
     * @param prefix    An optional string prefix to prepend to each column name,
     *                  typically used to reference specific tables or entities in a query.
     * @return A ParamSql object encapsulating the constructed WHERE clause
     * conditions joined by 'and', and a map of parameters for prepared
     * statement binding, where keys correspond to named parameters
     * and values are the user-provided filter values.
     */
    public static QueryFragment query(Map<String, Object> objectMap, String prefix) {
        StringJoiner whereSql = new StringJoiner(" and ");
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            QueryCondition condition = buildCondition(entry, prefix);
            whereSql.add(condition.sql());
        }
        return QueryFragment.of(whereSql, objectMap);
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
    public static QueryCondition buildCondition(Map.Entry<String, Object> entry, String prefix) {
        String sql = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
        if (StringUtils.hasLength(prefix)) {
            sql = prefix + "." + sql;
        }
        Object value = entry.getValue();
        String paramName = ":" + entry.getKey();
        if (value instanceof String) {
            sql = sql + " like " + paramName;
        } else if (value instanceof Collection<?>) {
            sql = sql + " in (" + paramName + ")";
        } else {
            sql = sql + " = " + paramName;
        }
        return new QueryCondition(sql, Map.of(paramName, value), null);
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
        objectMap = Maps.filterKeys(objectMap, key -> !SKIP_CRITERIA_KEYS.contains(key) && skipKeys.contains(key));
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