package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

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
     * @param sort   The original Spring Sort object defining the sorting orders. If null or empty, returns an unsorted Sort.
     * @param prefix An optional prefix to prepend to each sorting property, useful for aliasing in SQL queries.
     * @return A new Sort object with transformed sorting properties, ready for sorting queries that involve JSON columns.
     */
    public static Sort transformSortForJson(Sort sort, String prefix) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = Lists.newArrayList();
        for (Sort.Order order : sort) {
            String[] keys = StringUtils.delimitedListToStringArray(order.getProperty(), ".");
            if (keys.length > 1) {
                int lastIndex = keys.length - 1;
                var sortReplaceArray = Arrays.copyOfRange(keys, 1, lastIndex);
                String sortedProperty = keys[0];
                if (StringUtils.hasLength(prefix)) {
                    sortedProperty = prefix + "." + sortedProperty;
                }
                String sortReplace = sortedProperty + appendIntermediateKeys(sortReplaceArray).append("->>'")
                        .append(keys[lastIndex]).append("'");
                orders.add(Sort.Order.by(sortReplace).with(order.getDirection()));
            } else {
                orders.add(order);
            }
        }
        return Sort.by(orders);
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
            String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), ".");
            Map.Entry<String, List<String>> exps = jsonPathKeyAndParamName(keys, prefix);
            whereSql.add(exps.getKey());
            if (exps.getValue().size() > 1) {
                String[] values = StringUtils.commaDelimitedListToStringArray(String.valueOf(entry.getValue()));
                bindParams.put(exps.getValue().get(0), values[0]);
                bindParams.put(exps.getValue().get(1), values[1]);
            } else {
                bindParams.put(exps.getValue().getFirst(), entry.getValue());
            }
        }
        return QueryFragment.of(whereSql, bindParams);
    }

    /**
     * Generates a JSON path key along with the corresponding parameter name for constructing SQL queries.
     * It supports handling special keywords in the last key segment for operations like 'Between' and 'NotBetween'.
     *
     * @param keys   An array of strings representing keys in a JSON path, typically derived from a dot-separated string.
     * @param prefix An optional prefix to be prepended to the first key, used to namespace column names in SQL queries.
     * @return A Map.Entry containing:
     * - Key: A string representing the constructed JSON path expression suitable for SQL query with placeholders.
     * - Value: A list of strings representing the parameter names to bind values to in the SQL prepared statement.
     * @throws IllegalArgumentException If the keys array is null or empty.
     */
    private static Map.Entry<String, List<String>> jsonPathKeyAndParamName(String[] keys, String prefix) {
        if (keys == null || keys.length < 1) {
            throw new IllegalArgumentException("Keys array cannot be null or empty.");
        }

        int lastIndex = keys.length - 1;
        String lastKey = keys[lastIndex];

        // 处理第一个键
        String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        if (StringUtils.hasLength(prefix)) {
            column = prefix + "." + keys[0];
        }

        // 构建 JSON 路径
        StringBuilder jsonPath = new StringBuilder("(" + column);

        // 处理中间键
        if (lastIndex > 1) {
            String[] joinKeys = Arrays.copyOfRange(keys, 1, lastIndex);
            jsonPath.append(appendIntermediateKeys(joinKeys));
        }

        List<String> paramNames = new ArrayList<>();
        String paramName = StringUtils.arrayToDelimitedString(keys, "_");

        Map.Entry<String, String> exps = findKeyWord(lastKey);
        if (exps != null && !exps.getKey().isEmpty()) {
            String key = lastKey.substring(0, lastKey.length() - exps.getKey().length());
            jsonPath.append("->>'").append(key).append("' ");

            if ("Between".equals(exps.getKey()) || "NotBetween".equals(exps.getKey())) {
                String startKey = paramName + "_start";
                String endKey = paramName + "_end";
                jsonPath.append(exps.getValue()).append(" :").append(startKey).append(" and :").append(endKey);
                paramNames.add(startKey);
                paramNames.add(endKey);
            } else {
                jsonPath.append(exps.getValue()).append(" :").append(paramName);
                paramNames.add(paramName);
            }
        } else {
            jsonPath.append("->>'").append(lastKey).append("' ").append("=").append(" :").append(paramName);
            paramNames.add(paramName);
        }
        return Map.entry(jsonPath.append(")").toString(), paramNames);
    }


    /**
     * Appends intermediate keys from a given array into a StringBuilder to form a part of a JSON path expression.
     * Each key is surrounded by '->' to denote nested elements in a JSON structure when used within SQL queries.
     *
     * @param joinKeys An array of strings representing intermediate keys in a JSON path.
     * @return StringBuilder containing the concatenated intermediate keys formatted for a JSON path expression.
     */
    private static StringBuilder appendIntermediateKeys(String[] joinKeys) {
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
    private static Map.Entry<String, String> findKeyWord(String inputStr) {
        return SQL_OPERATION_MAPPING.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(inputStr, entry.getKey()))
                .max((entry1, entry2) -> {
                    int entry1Length = entry1.getKey().length();
                    int entry2Length = entry2.getKey().length();
                    return Integer.compare(entry1Length, entry2Length);
                }).orElse(null);
    }
}