package com.plate.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.exception.RestServerException;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
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
 * String sqlQuery = queryFragment.query();
 * System.out.println(sqlQuery);
 * }
 * </pre>
 * In this example, a JSON-based from is constructed using the QueryJsonHelper class. The form searches
 * for a user with a username like "Test User" and an email equal to "testuser@example.com". The form is
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

    private final static Map<String, BiFunction<String, Object, Criteria>> OPERATION_MAPPER = Maps.newHashMap();

    static {
        OPERATION_MAPPER.put("EQ", (column, value) -> Criteria.where(column).is(value));
        OPERATION_MAPPER.put("Equal", (column, value) -> Criteria.where(column).is(value));
        OPERATION_MAPPER.put("After", (column, value) -> Criteria.where(column).greaterThan(value));
        OPERATION_MAPPER.put("GreaterThanEqual", (column, value) -> Criteria.where(column).greaterThanOrEquals(value));
        OPERATION_MAPPER.put("GTE", (column, value) -> Criteria.where(column).greaterThanOrEquals(value));
        OPERATION_MAPPER.put("GreaterThan", (column, value) -> Criteria.where(column).greaterThan(value));
        OPERATION_MAPPER.put("GT", (column, value) -> Criteria.where(column).greaterThan(value));
        OPERATION_MAPPER.put("Before", (column, value) -> Criteria.where(column).lessThan(value));
        OPERATION_MAPPER.put("LessThanEqual", (column, value) -> Criteria.where(column).lessThanOrEquals(value));
        OPERATION_MAPPER.put("LTE", (column, value) -> Criteria.where(column).lessThanOrEquals(value));
        OPERATION_MAPPER.put("LessThan", (column, value) -> Criteria.where(column).lessThan(value));
        OPERATION_MAPPER.put("LT", (column, value) -> Criteria.where(column).lessThan(value));

        OPERATION_MAPPER.put("Between", (column, value) -> {
            String[] range = value.toString().split(",");
            return Criteria.where(column).between(range[0], range[1]);
        });

        OPERATION_MAPPER.put("NotBetween", (column, value) -> {
            String[] range = value.toString().split(",");
            return Criteria.where(column).notBetween(range[0], range[1]);
        });

        OPERATION_MAPPER.put("In", (column, value) -> {
            List<Object> values = Arrays.stream(value.toString().split(","))
                    .collect(Collectors.toList());
            return Criteria.where(column).in(values);
        });

        OPERATION_MAPPER.put("NotIn", (column, value) -> {
            List<Object> values = Arrays.stream(value.toString().split(","))
                    .collect(Collectors.toList());
            return Criteria.where(column).notIn(values);
        });

        OPERATION_MAPPER.put("IsNull", (column, value) -> Criteria.where(column).isNull());
        OPERATION_MAPPER.put("NotNull", (column, value) -> Criteria.where(column).isNotNull());
        OPERATION_MAPPER.put("IsNotNull", (column, value) -> Criteria.where(column).isNotNull());
        OPERATION_MAPPER.put("Null", (column, value) -> Criteria.where(column).isNull());

        OPERATION_MAPPER.put("Like", (column, value) -> Criteria.where(column).like(value.toString()));
        OPERATION_MAPPER.put("NotLike", (column, value) -> Criteria.where(column).notLike(value.toString()));
        OPERATION_MAPPER.put("StartingWith", (column, value) -> Criteria.where(column).like(value + "%"));
        OPERATION_MAPPER.put("EndingWith", (column, value) -> Criteria.where(column).like("%" + value));
        OPERATION_MAPPER.put("Containing", (column, value) -> Criteria.where(column).like("%" + value + "%"));
        OPERATION_MAPPER.put("NotContaining", (column, value) -> Criteria.where(column).notLike("%" + value + "%"));

        OPERATION_MAPPER.put("Not", (column, value) -> Criteria.where(column).not(value));
        OPERATION_MAPPER.put("IsTrue", (column, value) -> Criteria.where(column).is(true));
        OPERATION_MAPPER.put("True", (column, value) -> Criteria.where(column).is(true));
        OPERATION_MAPPER.put("IsFalse", (column, value) -> Criteria.where(column).is(false));
        OPERATION_MAPPER.put("False", (column, value) -> Criteria.where(column).is(false));
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
        if (keys.length == 0) {
            throw QueryException.withError("Delimited list to string property empty",
                    new IllegalArgumentException("Empty property name in sort order"));
        }

        String firstKey = validateColumnName(keys[0]);

        StringBuilder sortedProperty = new StringBuilder(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, firstKey));
        int lastIndex = keys.length - 1;
        if (lastIndex > 0) {
            String[] joinKeys = Arrays.copyOfRange(keys, 1, lastIndex);
            for (String path : joinKeys) {
                sortedProperty.append("->'").append(escapeJsonKey(path)).append("'");
            }
            sortedProperty.append("->>'").append(escapeJsonKey(keys[lastIndex])).append("'");
        }
        return Sort.Order.by(sortedProperty.toString()).with(order.getDirection());
    }

    private static String validateColumnName(String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            throw QueryException.withError("Column name is empty",
                    new IllegalArgumentException("Column name cannot be null or empty"));
        }

        if (!columnName.matches("[a-zA-Z0-9_]+")) {
            throw QueryException.withError("Invalid column name",
                    new IllegalArgumentException("Invalid column name: " + columnName));
        }
        return columnName;
    }

    /**
     * Constructs a QueryFragment representing a set of JSON-based from conditions.
     *
     * <p>This method takes a map of JSON parameters and a prefix, and constructs a QueryFragment that can be
     * used to build an SQL from targeting JSON data. The method iterates over the provided parameters,
     * converting each into a properly formatted SQL condition using the provided prefix to namespace JSON keys.
     *
     * @param params A map toSql each key represents a JSON path (possibly prefixed) and the value is the
     *               condition's value or further criteria for complex operations like 'Between', 'In', etc.
     * @param prefix A string prefix to prepend to each JSON key to form the full column name in SQL queries.
     * @return A QueryFragment containing the constructed SQL conditions for querying JSON data.
     * @throws IllegalArgumentException If any processing error occurs due to invalid input structure or content.
     */
    public static QueryFragment.Condition queryJson(Map<String, Object> params, String prefix) {
        Criteria criteria = Criteria.empty();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            criteria = criteria.and(buildJsonCondition(entry));
        }
        return QueryFragment.Condition.of(criteria, prefix);
    }

    /**
     * Constructs a {@link QueryFragment} based on the provided map entry and a prefix.
     * The entry's key represents a JSON path, and the value is the condition's operand.
     * The method supports nested JSON paths and translates them into equivalent SQL expressions
     * compatible with JSON column in SQL databases. It validates the path, processes the keys,
     * and delegates the construction of the final condition part to .
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
     * String sqlQuery = queryFragment.query();
     *
     * // Now you can execute the sqlQuery against your database using a JDBC template or similar
     * }
     * </pre>
     *
     * @param entry A map entry toSql the key is a dot-delimited string indicating a JSON path
     *              (e.g., "extend.usernameLike"), and the value is the target value for the condition.
     * @return A {@link QueryFragment} object representing the constructed SQL condition
     * for querying JSON data, including the SQL fragment, parameters, and the operation detail.
     * @throws RestServerException If the provided JSON path does not meet the minimum requirement of specifying
     *                             at least one level of nesting after the prefix (if provided).
     */
    private static Criteria buildJsonCondition(Map.Entry<String, Object> entry) {
        String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), ".");
        if (keys.length < 2) {
            throw QueryException.withError("Json from column path [query[" + entry.getKey() + "]] error",
                    new IllegalArgumentException("Json path example: extend.username," +
                            "Request from params:" +
                            "query[extend.usernameLike]=aa," +
                            "query[extend.age]=23," +
                            "query[extend.nameIn]=aa,bb,cc," +
                            "query[extend.codeEq]=123456"
                    ));
        }
        String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        StringBuilder columnBuilder = new StringBuilder(column);
        int lastIndex = keys.length - 1;
        if (lastIndex > 1) {
            String[] joinKeys = Arrays.copyOfRange(keys, 1, lastIndex);
            for (String path : joinKeys) {
                columnBuilder.append("->'").append(escapeJsonKey(path)).append("'");
            }
        }
        String lastKey = keys[lastIndex];
        Map.Entry<String, BiFunction<String, Object, Criteria>> comparator = queryKeywordMapper(lastKey);
        BiFunction<String, Object, Criteria> func = (col, value) -> Criteria.where(col).is(value);
        if (comparator != null) {
            lastKey = lastKey.substring(0, lastKey.length() - comparator.getKey().length());
            func = comparator.getValue();
        }
        columnBuilder.append("->>'").append(escapeJsonKey(lastKey)).append("'");
        return func.apply(columnBuilder.toString(), entry.getValue());
    }

    /**
     * Escapes special characters in JSON keys to prevent SQL injection.
     *
     * @param key The JSON key to escape
     * @return The escaped JSON key
     */
    private static String escapeJsonKey(String key) {
        if (key == null) {
            return null;
        }
        return key.replace("'", "''");
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
    private static Map.Entry<String, BiFunction<String, Object, Criteria>> queryKeywordMapper(String queryKeyword) {
        return OPERATION_MAPPER.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(queryKeyword, entry.getKey()))
                .max((entry1, entry2) -> {
                    int entry1Length = entry1.getKey().length();
                    int entry2Length = entry2.getKey().length();
                    return Integer.compare(entry1Length, entry2Length);
                }).orElse(null);
    }

}