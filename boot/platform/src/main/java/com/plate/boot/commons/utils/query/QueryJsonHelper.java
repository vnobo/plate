package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.exception.RestServerException;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Represents a query condition with its SQL fragment, operationSql),
 Collections.singletonMap associated parameters, and operation details.
 */
public final class QueryJsonHelper {

    private final static Map<String, String> SQL_OPERATION_MAPPING = Maps.newHashMap();

    static {
        SQL_OPERATION_MAPPING.put("EQ", "=");
        SQL_OPERATION_MAPPING.put("Equal", "=");
        SQL_OPERATION_MAPPING.put("After", ">");
        SQL_OPERATION_MAPPING.put("GreaterThanEqual", ">=");
        SQL_OPERATION_MAPPING.put("GTE", ">=");
        SQL_OPERATION_MAPPING.put("GreaterThan", ">");
        SQL_OPERATION_MAPPING.put("GT", ">");
        SQL_OPERATION_MAPPING.put("Before", "<");
        SQL_OPERATION_MAPPING.put("LessThanEqual", "<=");
        SQL_OPERATION_MAPPING.put("LTE", "<=");
        SQL_OPERATION_MAPPING.put("LessThan", "<");
        SQL_OPERATION_MAPPING.put("LT", "<");
        SQL_OPERATION_MAPPING.put("Between", "between");
        SQL_OPERATION_MAPPING.put("NotBetween", "not between");
        SQL_OPERATION_MAPPING.put("NotIn", "not in");
        SQL_OPERATION_MAPPING.put("In", "in");
        SQL_OPERATION_MAPPING.put("IsNotNull", "is not null");
        SQL_OPERATION_MAPPING.put("NotNull", "is not null");
        SQL_OPERATION_MAPPING.put("IsNull", "is null");
        SQL_OPERATION_MAPPING.put("Null", "is null");
        SQL_OPERATION_MAPPING.put("NotLike", "not like");
        SQL_OPERATION_MAPPING.put("Like", "like");
        SQL_OPERATION_MAPPING.put("StartingWith", "like");
        SQL_OPERATION_MAPPING.put("EndingWith", "like");
        SQL_OPERATION_MAPPING.put("IsNotLike", "not like");
        SQL_OPERATION_MAPPING.put("Containing", "like");
        SQL_OPERATION_MAPPING.put("NotContaining", "not like");
        SQL_OPERATION_MAPPING.put("Not", "!=");
        SQL_OPERATION_MAPPING.put("IsTrue", "is true");
        SQL_OPERATION_MAPPING.put("True", "is true");
        SQL_OPERATION_MAPPING.put("IsFalse", "is false");
        SQL_OPERATION_MAPPING.put("False", "is false");
    }

    /**
     * Transforms the given Sort object into a new Sort object with property names converted to camelCase format,
     * which is more suitable for JSON serialization. This method is particularly useful when preparing sorting criteria
     * to be used in APIs or databases that expect properties in camelCase notation.
     *
     * @param sort The Sort object whose properties are to be transformed. If null or empty, the method returns an unsorted Sort instance.
     * @return A new Sort object with property names converted to camelCase, preserving the original sorting directions (ascending/descending).
     */
    public static Sort transformSortForJson(Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = sort.stream().map(QueryJsonHelper::convertSortOrderToCamelCase)
                .collect(Collectors.toList());
        return Sort.by(orders);
    }

    /**
     * Converts a single Sort.Order's property into camelCase format, preparing it for SQL queries
     * that involve JSON data. If the property denotes a nested JSON path, additional transformations
     * are applied to construct a valid SQL query fragment.
     *
     * @param order The Sort.Order object whose property needs to be converted to camelCase
     *              and potentially transformed for nested JSON field access.
     * @return A new Sort.Order instance with the property converted to camelCase and formatted
     *         appropriately for nested JSON paths, maintaining the original direction of sorting.
     */
    private static Sort.Order convertSortOrderToCamelCase(Sort.Order order) {
        String[] keys = StringUtils.delimitedListToStringArray(order.getProperty(), ".");
        String sortedProperty = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        int lastIndex = keys.length - 1;
        if (lastIndex > 0) {
            sortedProperty = sortedProperty + buildJsonQueryPath(Arrays.copyOfRange(keys, 1, lastIndex))
                    .append("->>'").append(keys[lastIndex]).append("'");
        }
        return Sort.Order.by(sortedProperty).with(order.getDirection());
    }

    /**
     * Constructs a QueryFragment representing a conditional SQL WHERE clause
     * based on a map of parameters and an optional prefix for nested JSON keys.
     * Each entry in the map is transformed into a condition that can query nested
     * JSON properties in a SQL environment, considering the provided prefix.
     *
     * @param params A map where keys represent JSON property paths and values are the criteria
     *               to match. If null or empty, the method returns a QueryFragment with no conditions.
     * @param prefix An optional string to prefix before each JSON property path in the SQL query,
     *               useful for namespacing or distinguishing JSON fields. Defaults to an empty string if not provided.
     * @return A QueryFragment containing the concatenated SQL conditions joined by 'and',
     * and a map of parameters for binding values to these conditions, ready to be used in a prepared statement.
     */
    public static QueryFragment queryJson(Map<String, Object> params, String prefix) {
        Map<String, Object> bindParams = Maps.newHashMap();
        StringJoiner whereSql = new StringJoiner(" and ");
        if (ObjectUtils.isEmpty(params)) {
            return QueryFragment.of(whereSql, bindParams);
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            QueryCondition exps = buildJsonCondition(entry, prefix);
            whereSql.add(exps.sql());
            bindParams.putAll(exps.params());
        }
        return QueryFragment.of(whereSql, bindParams);
    }

    /**
     * Generates a JSON path key along with the corresponding parameter name for constructing SQL queries.
     * It supports handling special keywords in the last key segment for operations like 'Between' and 'NotBetween'.
     *
     * @param entry  An array of strings representing keys in a JSON path, typically derived from a dot-separated string.
     * @param prefix An optional prefix to be prepended to the first key, used to namespace column names in SQL queries.
     * @return A Map.Entry containing:
     * - Key: A string representing the constructed JSON path expression suitable for SQL query with placeholders.
     * - Value: A list of strings representing the parameter names to bind values to in the SQL prepared statement.
     * @throws IllegalArgumentException If the keys array is null or empty.
     */
    private static QueryCondition buildJsonCondition(Map.Entry<String, Object> entry, String prefix) {
        String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), ".");
        if (keys.length < 2) {
            throw RestServerException.withMsg("Json query column path [query[" + entry.getKey() + "]] error",
                    List.of("Json path example: extend.username",
                            "Request query params:",
                            "query[extend.usernameLike]=aa",
                            "query[extend.age]=23",
                            "query[extend.nameIn]=aa,bb,cc",
                            "query[extend.codeEq]=123456"
                    ));
        }
        // 处理第一个键
        String sql = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        if (StringUtils.hasLength(prefix)) {
            sql = prefix + "." + keys[0];
        }
        // 构建 JSON 路径
        StringBuilder jsonPath = new StringBuilder("(" + sql);
        int lastIndex = keys.length - 1;
        // 处理中间键
        if (lastIndex > 1) {
            String[] joinKeys = Arrays.copyOfRange(keys, 1, lastIndex);
            jsonPath.append(buildJsonQueryPath(joinKeys));
        }
        //处理最后键
        QueryCondition lastCondition = buildLastCondition(keys, entry.getValue());
        sql = jsonPath.append(lastCondition.sql()).toString();
        return new QueryCondition(sql, lastCondition.params(), lastCondition.operation());
    }

    /**
     * Constructs the final part of a query condition based on the provided keys and a value.
     * This method handles special keywords for operations such as 'Between', 'NotBetween', 'In', and 'NotIn',
     * formatting the SQL condition accordingly and preparing the necessary bind parameters.
     *
     * @param keys  An array of strings forming the path to the JSON attribute. The last key may contain
     *              a keyword indicating a special operation.
     * @param value The value or values to be used in the query condition. For 'In' and 'NotIn'
     *              operations, this should be a comma-separated string or collection of values.
     * @return A {@link QueryCondition} object containing:
     * - The SQL fragment representing the condition with placeholders for parameters.
     * - A map of parameter names to their bound values.
     * - The operation details including the keyword and its SQL representation.
     */
    private static QueryCondition buildLastCondition(String[] keys, Object value) {
        StringBuilder conditionSql = new StringBuilder("->>'");

        String paramName = StringUtils.arrayToDelimitedString(keys, "_");
        String lastKey = keys[keys.length - 1];

        Map.Entry<String, String> exps = retrieveKeywordMapping(lastKey);
        if (exps == null) {
            conditionSql.append(lastKey).append("' = :").append(paramName);
            return new QueryCondition(conditionSql.append(")").toString(), Map.of(paramName, value),
                    SQL_OPERATION_MAPPING.entrySet().iterator().next());
        }

        String key = lastKey.substring(0, lastKey.length() - exps.getKey().length());
        conditionSql.append(key).append("' ");

        if ("Between".equals(exps.getKey()) || "NotBetween".equals(exps.getKey())) {
            String startKey = paramName + "_start";
            String endKey = paramName + "_end";
            conditionSql.append(exps.getValue()).append(" :").append(startKey).append(" and :").append(endKey);
            var values = StringUtils.commaDelimitedListToStringArray(String.valueOf(value));
            return new QueryCondition(conditionSql.append(")").toString(),
                    Map.of(startKey, values[0], endKey, values[1]), exps);
        } else if ("NotIn".equals(exps.getKey()) || "In".equals(exps.getKey())) {
            conditionSql.append(exps.getValue()).append(" (:").append(paramName).append(")");
            var values = StringUtils.commaDelimitedListToSet(String.valueOf(value));
            return new QueryCondition(conditionSql.append(")").toString(), Map.of(paramName, values), exps);
        } else {
            conditionSql.append(exps.getValue()).append(" :").append(paramName);
            return new QueryCondition(conditionSql.append(")").toString(), Map.of(paramName, value), exps);
        }
    }

    /**
     * Appends intermediate keys from a given array into a StringBuilder to form a part of a JSON path expression.
     * Each key is surrounded by '->' to denote nested elements in a JSON structure when used within SQL queries.
     *
     * @param joinKeys An array of strings representing intermediate keys in a JSON path.
     * @return StringBuilder containing the concatenated intermediate keys formatted for a JSON path expression.
     */
    private static StringBuilder buildJsonQueryPath(String[] joinKeys) {
        StringBuilder jsonPath = new StringBuilder();
        for (String path : joinKeys) {
            jsonPath.append("->'").append(path).append("'");
        }
        return jsonPath;
    }

    /**
     * Searches for a keyword within a predefined map of keywords and returns the matching entry.
     * The search prioritizes longer keywords and is case-insensitive, considering the end of the input string.
     *
     * @param inputStr The string to search for a matching keyword suffix.
     * @return An entry containing the matched keyword and its associated value,
     * or null if no match is found.
     */
    private static Map.Entry<String, String> retrieveKeywordMapping(String inputStr) {
        return SQL_OPERATION_MAPPING.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(inputStr, entry.getKey()))
                .max((entry1, entry2) -> {
                    int entry1Length = entry1.getKey().length();
                    int entry2Length = entry2.getKey().length();
                    return Integer.compare(entry1Length, entry2Length);
                }).orElse(null);
    }


}