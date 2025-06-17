package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.exception.RestServerException;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * QueryJsonHelper is a utility class designed to facilitate the transformation of from parameters into
 * SQL-compatible formats, particularly focusing on handling JSON-based queries. It provides methods to
 * convert sort orders into camelCase for JSON serialization, construct conditional SQL clauses for querying
 * nested JSON properties, and manage special operations like 'Between', 'In', and others within a SQL context.
 *
 * <p>This class is essential for applications that need to from JSON data stored in SQL databases, as it
 * helps in constructing SQL queries that can understand and manipulate JSON structures.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * Map<String, Object> jsonParams = new HashMap<>();
 * jsonParams.put("extend.usernameLike", "Test User");
 * jsonParams.put("extend.emailEq", "testuser@example.com");
 * QueryFragment queryFragment = QueryJsonHelper.queryJson(jsonParams, "a");
 * String sqlQuery = queryFragment.querySql();
 * System.out.println(sqlQuery);
 * }
 * </pre>
 * In this example, a JSON-based from is constructed using the QueryJsonHelper class. The from searches
 * for a user with a username like "Test User" and an email equal to "testuser@example.com". The from is
 * then converted into an SQL from string that can be executed against a database.
 *
 * <p>For a JSON object with nested fields, such as:
 * <pre>
 * {@code
 * {
 *   "extend": {
 *     "requestBody": {
 *       "bio": "This is a brief introduction of the user.",
 *       "name": "Test User",
 *       "email": "testuser@example.com",
 *       "phone": "123-456-7890",
 *       "avatar": "http://example.com/avatar.jpg"
 *     },
 *     "additionalField1": "value1",
 *     "additionalField2": "value2"
 *   },
 *   "disabled": false,
 *   "username": "17089118266"
 * }
 * }
 * </pre>
 * The QueryJsonHelper class can be used to construct SQL queries that target these nested fields, such as:
 * <pre>
 * {@code
 * QueryFragment queryFragment = QueryJsonHelper.queryJson(jsonParams, "a");
 * }
 * </pre>
 * This will generate an SQL from that can search for users based on the provided JSON parameters.
 *
 * @see QueryFragment for the class representing the SQL from structure.
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
     * Transforms a given Spring Framework Sort object into a new Sort object with properties
     * converted to camelCase format, which is beneficial when sorting involves JSON fields in SQL queries.
     * Nested JSON paths within sort properties are also adjusted to be compatible with SQL syntax.
     * If the input Sort is null or empty, the method returns an unsorted Sort instance.
     *
     * @param sort The Sort object to be transformed. Its properties may require conversion to camelCase
     *             and adjustment for SQL-JSON compatibility.
     * @return A new Sort object with properties converted into camelCase format and formatted
     * for proper handling of nested JSON paths in SQL queries. Returns an unsorted Sort if
     * the input is null or empty.
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
     * Converts the property of a Sort.Order into camelCase format and adjusts nested JSON paths
     * to be compatible with SQL syntax, particularly useful when sorting JSON fields within SQL queries.
     * The first part of the property is converted to lower_camel_case, and the remaining parts,
     * if any, are appended with appropriate '->>' operators to form a valid SQL-JSON path expression.
     *
     * @param order The Sort.Order whose property needs conversion and adjustment for SQL-JSON sorting.
     * @return A new Sort.Order instance with the property formatted in camelCase and SQL-JSON compatible
     * for sorting purposes, maintaining the original direction of sorting.
     */
    private static Sort.Order convertSortOrderToCamelCase(Sort.Order order) {
        String[] keys = StringUtils.delimitedListToStringArray(order.getProperty(), ".");
        String sortedProperty = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        int lastIndex = keys.length - 1;
        if (lastIndex > 0) {
            sortedProperty = sortedProperty + "->" + buildJsonQueryPath(Arrays.copyOfRange(keys, 1, lastIndex))
                    + "->>'" + keys[lastIndex] + "'";
        }
        return Sort.Order.by(sortedProperty).with(order.getDirection());
    }

    /**
     * Constructs a QueryFragment representing a set of JSON-based from conditions.
     *
     * <p>This method takes a map of JSON parameters and a prefix, and constructs a QueryFragment that can be
     * used to build an SQL from targeting JSON data. The method iterates over the provided parameters,
     * converting each into a properly formatted SQL condition using the provided prefix to namespace JSON keys.
     *
     * @param params A map where each key represents a JSON path (possibly prefixed) and the value is the
     *               condition's value or further criteria for complex operations like 'Between', 'In', etc.
     * @param prefix A string prefix to prepend to each JSON key to form the full column name in SQL queries.
     * @return A QueryFragment containing the constructed SQL conditions for querying JSON data.
     * @throws IllegalArgumentException If any processing error occurs due to invalid input structure or content.
     */
    public static QueryFragment queryJson(Map<String, Object> params, String prefix) {
        QueryFragment queryFragment = QueryFragment.withNew();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            var condition = buildJsonCondition(entry, prefix);
            queryFragment.getWhere().merge(condition.getWhere());
            queryFragment.putAll(condition);
        }
        return queryFragment;
    }

    /**
     * Constructs a {@link QueryFragment} based on the provided map entry and a prefix.
     * The entry's key represents a JSON path, and the value is the condition's operand.
     * The method supports nested JSON paths and translates them into equivalent SQL expressions
     * compatible with JSON columns in SQL databases. It validates the path, processes the keys,
     * and delegates the construction of the final condition part to {@link #buildLastCondition(String[], Object)}.
     * <pre>
     * {@code
     * // Create a map of JSON-based from parameters
     * Map<String, Object> jsonParams = new HashMap<>();
     * jsonParams.put("extend.requestBody.nameEq", "Test User"); // EQ stands for Equal
     * jsonParams.put("extend.additionalField1Eq", "value1"); // EQ stands for Equal
     *
     * // Use the QueryJsonHelper to construct the SQL from for the given JSON parameters
     * QueryFragment queryFragment = QueryJsonHelper.queryJson(jsonParams, "a");
     *
     * // Get the SQL from string
     * String sqlQuery = queryFragment.querySql();
     *
     * // Now you can execute the sqlQuery against your database using a JDBC template or similar
     * }
     * </pre>
     *
     * @param entry  A map entry where the key is a dot-delimited string indicating a JSON path
     *               (e.g., "extend.usernameLike"), and the value is the target value for the condition.
     * @param prefix A prefix to prepend to the first key of the JSON path to qualify column names
     *               in the SQL from, ensuring correct scoping or avoiding naming collisions.
     * @return A {@link QueryFragment} object representing the constructed SQL condition
     * for querying JSON data, including the SQL fragment, parameters, and the operation detail.
     * @throws RestServerException If the provided JSON path does not meet the minimum requirement of specifying
     *                             at least one level of nesting after the prefix (if provided).
     */
    private static QueryFragment buildJsonCondition(Map.Entry<String, Object> entry, String prefix) {
        String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), ".");
        if (keys.length < 2) {
            throw QueryException.withError("Json from column path [from[" + entry.getKey() + "]] error",
                    new Throwable("Json path example: extend.username," +
                            "Request from params:" +
                            "from[extend.usernameLike]=aa," +
                            "from[extend.age]=23," +
                            "from[extend.nameIn]=aa,bb,cc," +
                            "from[extend.codeEq]=123456"
                    ));
        }
        String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        if (StringUtils.hasLength(prefix)) {
            column = prefix + "." + keys[0];
        }
        StringBuilder conditionBuilder = new StringBuilder(column);
        int lastIndex = keys.length - 1;
        if (lastIndex > 1) {
            String[] joinKeys = Arrays.copyOfRange(keys, 1, lastIndex);
            for (String path : joinKeys) {
                conditionBuilder.append("->'").append(path).append("'");
            }
        }
        QueryFragment lastCondition = buildLastCondition(keys, entry.getValue());
        conditionBuilder.append(lastCondition.getWhere());
        return QueryFragment.withMap(lastCondition).where(conditionBuilder.toString());
    }

    /**
     * Constructs the final part of a from condition based on an array of keys and a value.
     * This method generates SQL fragments for different types of conditions such as equality,
     * between, in, not in, etc., depending on the detected keyword in the last key of the keys array.
     * It handles parameterization to prevent SQL injection and prepares the condition for dynamic
     * from execution.
     *
     * @param keys  An array of strings forming the path to the JSON attribute. The last element
     *              may contain a keyword suffix for special operations.
     * @param value The value to be compared against in the form condition. For 'In' and 'Between'
     *              operations, this should be a comma-separated string or an array respectively.
     * @return A QueryCondition object containing:
     * - The 'getWhereSql' field as a string of the SQL fragment representing the condition.
     * - The 'params' map holding named parameters and their values for the form.
     * - The 'operation' entry describing the operation type and its SQL syntax.
     */
    private static QueryFragment buildLastCondition(String[] keys, Object value) {
        StringBuilder conditionSql = new StringBuilder("->>'");

        String paramName = StringUtils.arrayToDelimitedString(keys, "_");
        String lastKey = keys[keys.length - 1];

        Map.Entry<String, String> exps = queryKeywordMapper(lastKey);
        if (exps == null) {
            conditionSql.append(lastKey).append("' = :").append(paramName);
            return QueryFragment.withMap(Map.of(paramName, value)).where(conditionSql.toString());
        }

        String key = lastKey.substring(0, lastKey.length() - exps.getKey().length());
        conditionSql.append(key).append("' ");
        Map<String, Object> params;

        value = switch (exps.getKey()) {
            case "StartingWith" -> value + "%";
            case "EndingWith" -> "%" + value;
            case "Containing" -> "%" + value + "%";
            default -> value;
        };

        if ("Between".equals(exps.getKey()) || "NotBetween".equals(exps.getKey())) {
            String startKey = paramName + "_start";
            String endKey = paramName + "_end";
            conditionSql.append(exps.getValue()).append(" :").append(startKey).append(" and :").append(endKey);
            var values = StringUtils.commaDelimitedListToStringArray(String.valueOf(value));
            params = Map.of(startKey, values[0], endKey, values[1]);
        } else if ("NotIn".equals(exps.getKey()) || "In".equals(exps.getKey())) {
            conditionSql.append(exps.getValue()).append(" (:").append(paramName).append(")");
            var values = StringUtils.commaDelimitedListToSet(String.valueOf(value));
            params = Map.of(paramName, values);
        } else {
            conditionSql.append(exps.getValue()).append(" :").append(paramName);
            params = Map.of(paramName, value);
        }
        return QueryFragment.withMap(params).where(conditionSql.toString());
    }

    /**
     * Constructs a JSON path from string from an array of keys meant to be used in SQL queries
     * targeting JSON data. Each key in the array is appended to the path with the appropriate
     * SQL-JSON operator, allowing for traversal of nested JSON objects.
     *
     * @param joinKeys An array of strings representing keys in a JSON object which will be combined
     *                 to form a JSON path from expression.
     * @return StringBuilder A StringBuilder object containing the concatenated JSON path
     * from expression, suitable for use in SQL queries with JSON columns.
     */
    private static StringJoiner buildJsonQueryPath(String[] joinKeys) {
        StringJoiner jsonPath = new StringJoiner("->");
        for (String path : joinKeys) {
            jsonPath.add("'" + path + "'");
        }
        return jsonPath;
    }

    /**
     * Retrieves the longest keyword mapping from a predefined map for a given input string.
     * The mapping is identified by checking if the input string ends with any of the keys
     * in the SQL_OPERATION_MAPPING map, and then selecting the one with the maximum length.
     * This is particularly useful for parsing or transforming strings based on known suffix patterns.
     *
     * @param queryKeyword The string for which the longest matching keyword mapping is to be retrieved.
     * @return An entry containing the matched keyword and its corresponding value from the SQL_OPERATION_MAPPING,
     * or null if no match is found.
     */
    private static Map.Entry<String, String> queryKeywordMapper(String queryKeyword) {
        return SQL_OPERATION_MAPPING.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(queryKeyword, entry.getKey()))
                .max((entry1, entry2) -> {
                    int entry1Length = entry1.getKey().length();
                    int entry2Length = entry2.getKey().length();
                    return Integer.compare(entry1Length, entry2Length);
                }).orElse(null);
    }


}