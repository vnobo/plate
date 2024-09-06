package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

    public static final Set<String> SKIP_CRITERIA_KEYS = Set.of("extend", "createdTime", "updatedTime");

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
     * Constructs a ParamSql instance representing a part of an SQL WHERE clause
     * and its corresponding parameters extracted from a given Java object, while
     * providing options to skip certain keys and apply a prefix to the keys.
     * The method first converts the object into a map and processes a special
     * "query" key using QueryJson to generate SQL and parameters. It then handles
     * a "securityCode" key if present and not skipped. Afterward, it filters out
     * keys that should be skipped, including default ones defined in SKIP_CRITERIA_KEYS
     * and combines these with additional parameters generated from the remaining object map.
     *
     * @param object   The source object from which SQL conditions and parameters are derived.
     * @param skipKeys A collection of keys to be excluded from processing, can be null.
     * @param prefix   A prefix to prepend to the keys in the generated SQL, useful for nested queries.
     * @return A ParamSql object containing a StringJoiner with concatenated SQL conditions
     * (joined by 'and') and a map of parameters for prepared statement binding.
     */
    @SuppressWarnings("unchecked")
    public static QueryFragment query(Object object, Collection<String> skipKeys, String prefix) {

        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        if (ObjectUtils.isEmpty(objectMap)) {
            return QueryFragment.of(new StringJoiner(" and "), Maps.newHashMap());
        }
        QueryFragment jsonQueryFragment = QueryJsonHelper.queryJson((Map<String, Object>) objectMap.get("query"), prefix);
        Map<String, Object> params = jsonQueryFragment.params();
        StringJoiner sql = jsonQueryFragment.sql();
        String securityCodeKey = "securityCode";
        if (!skipKeys.contains(securityCodeKey) && !ObjectUtils.isEmpty(objectMap.get(securityCodeKey))) {
            String key = "tenant_code";
            if (StringUtils.hasLength(prefix)) {
                key = prefix + "." + key;
            }
            sql.add(key + " like :securityCode");
            params.put(securityCodeKey, objectMap.get(securityCodeKey));
        }

        Set<String> removeKeys = new HashSet<>(SKIP_CRITERIA_KEYS);
        removeKeys.add("query");
        removeKeys.add(securityCodeKey);
        if (!ObjectUtils.isEmpty(skipKeys)) {
            removeKeys.addAll(skipKeys);
        }

        objectMap = Maps.filterKeys(objectMap, key -> !removeKeys.contains(key));
        QueryFragment entityQueryFragment = query(objectMap, prefix);
        params.putAll(entityQueryFragment.params());
        sql.merge(entityQueryFragment.sql());
        return QueryFragment.of(sql, params);
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
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            if (StringUtils.hasLength(prefix)) {
                column = prefix + "." + column;
            }

            Object value = entry.getValue();
            String paramName = ":" + entry.getKey();

            if (value instanceof String) {
                whereSql.add(column + " like " + paramName);
            } else if (value instanceof Collection<?>) {
                whereSql.add(column + " in (" + paramName + ")");
            } else {
                whereSql.add(column + " = " + paramName);
            }
        }
        return QueryFragment.of(whereSql, objectMap);
    }

    /**
     * Constructs a Criteria instance by converting the provided object into a map,
     * excluding specified keys, and then further processing this map to create
     * the Criteria object. The method removes keys listed in the predefined
     * SKIP_CRITERIA_KEYS set as well as any additional keys specified in the
     * skipKes collection from the object map before constructing the Criteria.
     *
     * @param object  The Java object to convert into Criteria. Its properties will form the basis of the Criteria.
     * @param skipKes A collection of strings representing keys to exclude from the object during conversion.
     *                These are in addition to the default skipped keys predefined in SKIP_CRITERIA_KEYS.
     * @return A Criteria instance representing the processed object, excluding the specified keys.
     */
    public static Criteria criteria(Object object, Collection<String> skipKes) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        if (!ObjectUtils.isEmpty(objectMap)) {
            Set<String> mergeSet = Sets.newHashSet(SKIP_CRITERIA_KEYS);
            if (!ObjectUtils.isEmpty(skipKes)) {
                mergeSet.addAll(skipKes);
            }
            mergeSet.forEach(objectMap::remove);
        }
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
                return Criteria.where(entry.getKey()).like(String.format("%s", value) + "%").ignoreCase(true);
            } else if (entry.getValue() instanceof Collection<?> values) {
                return Criteria.where(entry.getKey()).in(values);
            } else {
                return Criteria.where(entry.getKey()).is(entry.getValue());
            }
        }).collect(Collectors.toList());
        return Criteria.from(criteriaList);
    }
}