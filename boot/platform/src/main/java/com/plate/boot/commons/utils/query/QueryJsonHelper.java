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
 * Utility class providing helper methods to facilitate querying and sorting JSON data within SQL statements.
 * This class maps operation keywords to their SQL equivalents and offers functionality to translate
 * query parameters and sorting instructions into SQL-compatible formats, especially handy when working
 * with JSON data stored in relational databases.
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
     * Transforms a Spring Sort object into a new Sort object suitable for sorting based on JSON properties,
     * considering nested structures denoted by dot-separated keys. Optionally prefixes property keys.
     * <p>
     * This method processes each order in the provided Sort object. If a property key represents a nested
     * JSON path (indicated by containing dots), it constructs a new sorting property that can be used
     * directly in SQL queries involving JSON data, using the '->>' operator to access nested JSON fields.
     * Non-nested properties or those not requiring transformation are preserved as is.
     *
     * @param sort The original Spring Sort object defining the sorting orders. If null or empty, returns an unsorted Sort.
     * @return A new Sort object with transformed sorting properties, ready for sorting queries that involve JSON columns.
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
     * Converts the property of a Sort.Order into camelCase format suitable for certain SQL operations,
     * especially when dealing with JSON data. If the property denotes a nested structure using dot-notation,
     * it transforms the first key to camelCase and appends the rest with appropriate JSON path syntax.
     *
     * @param order The Sort.Order whose property is to be converted to camelCase format, potentially
     *              handling nested keys for JSON compatibility.
     * @return A new Sort.Order instance with the property converted to camelCase format, ready
     * for use in sorting operations that require specific property formatting, like JSON field sorting.
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
     * Constructs a SQL parameter object based on a JSON-like map structure and an optional prefix for column names.
     * <p>
     * This method iterates through the provided map, treating keys as JSON paths to construct
     * WHERE clause conditions and bind parameters accordingly. Supports complex JSON paths
     * and keyword-based operations like 'Between' and 'NotBetween'.
     *
     * @param params A map where each key represents a JSON path to a value that should be used in query conditions.
     *               The value associated with each key is the target value for comparison or range (for Between/NotBetween).
     * @param prefix An optional prefix to prepend to column names, useful when querying nested or aliased tables/views.
     * @return A {@link QueryFragment} object containing a {@link StringJoiner} with concatenated WHERE clause conditions
     * and a map of bind parameters to be used in a prepared statement.
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