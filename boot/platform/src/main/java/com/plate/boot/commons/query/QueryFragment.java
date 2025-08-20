package com.plate.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.DatabaseUtils;
import lombok.Getter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment and a map of parameters
 * to be bound to a PreparedStatement. This class facilitates the construction of dynamic SQL queries
 * with placeholders for improved performance and security against SQL injection.
 * <p>The QueryFragment class is designed to be flexible and modular, allowing users to build complex
 * SQL queries by chaining method calls. It manages the SQL from structure, including the SELECT
 * columns, FROM clause, WHERE conditions, ORDER BY clause, and LIMIT/OFFSET for pagination.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * QueryFragment queryFragment = QueryFragment.withNew()
 *     .columns("id", "name", "email")
 *     .from("users")
 *     .toSql("age > :age", 18)
 *     .orderBy("name ASC")
 *     .orderBy("email DESC");
 *
 * // Bind parameters
 * queryFragment.put("age", 18);
 *
 * // Generate SQL from
 * String sql = queryFragment.query();
 * System.out.println(sql);
 * }
 * </pre>
 * In this example, a QueryFragment instance is created and configured with columns, a table name,
 * a WHERE condition, and ORDER BY clauses. Parameters are added to the form fragment, and finally,
 * the SQL from string is generated using the query() method.
 *
 * @see QueryHelper for utility methods to construct QueryFragment instances from objects.
 */
@Getter
public class QueryFragment extends HashMap<String, Object> {

    /**
     * A StringJoiner to accumulate column names for the SELECT clause.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.columns("id", "name", "email");
     * }
     * </pre>
     */
    private final StringJoiner columns = new StringJoiner(",").setEmptyValue("");

    /**
     * A StringJoiner to accumulate the main SQL from parts (e.g., table names).
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.from("users");
     * }
     * </pre>
     */
    private final StringJoiner from = new StringJoiner(" ").setEmptyValue("");

    /**
     * A StringJoiner to accumulate WHERE conditions.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.toSql("age > :age");
     * }
     * </pre>
     */
    private final StringJoiner where = new StringJoiner(" AND ").setEmptyValue("");

    /**
     * A StringJoiner to accumulate ORDER BY clauses.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.orderBy("name ASC");
     * }
     * </pre>
     */
    private final StringJoiner orderBy = new StringJoiner(",").setEmptyValue("");

    /**
     * A StringJoiner to accumulate columns for the GROUP BY clause.
     *
     * <p>This field is used to build the GROUP BY clause of the SQL statement by
     * accumulating the specified columns.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.groupBy("category", "type");
     * }
     * </pre>
     */
    private final StringJoiner groupBy = new StringJoiner(",").setEmptyValue("");

    /**
     * The maximum number of rows to return (LIMIT clause).
     */
    private int size = 25;

    /**
     * The number of rows to skip before starting to return rows (OFFSET clause).
     */
    private long offset = 0;

    // 编译正则表达式以提高性能
    private static final Pattern COLUMN_REPLACE_PATTERN = java.util.regex.Pattern.compile("[.\\W]");

    /**
     * Creates a new QueryFragment instance with the specified parameters.
     *
     * @param params the parameters to initialize the QueryFragment with
     */
    private QueryFragment(Map<String, Object> params) {
        super(params);
    }

    /**
     * Private constructor to initialize QueryFragment with specified tables.
     *
     * @param tables the table names to be used in the query
     */
    private QueryFragment(String... tables) {
        for (String table : tables) {
            this.from.add(table);
        }
    }

    /**
     * Static factory method to create a new QueryFragment instance with specified tables.
     *
     * @param tables the table names to be used in the query
     * @return a new QueryFragment instance
     */
    public static QueryFragment from(String... tables) {
        return new QueryFragment(tables);
    }

    /**
     * Creates a new QueryFragment instance with the specified parameters.
     *
     * <p>This method initializes a new QueryFragment with the provided parameters. It is a convenient way
     * to create a QueryFragment instance with predefined parameters.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * Map<String, Object> params = Map.of("name", "John", "age", 30);
     * QueryFragment queryFragment = QueryFragment.withParams(params);
     * }
     * </pre>
     *
     * @param params the parameters to initialize the QueryFragment with
     * @return a new QueryFragment instance with the specified parameters
     */
    public static QueryFragment withParams(Map<String, Object> params) {
        var resultMap = new HashMap<String, Object>();
        for (var en : params.entrySet()) {
            var k = TypeInformation.of(en.getValue().getClass());
            var v = DatabaseUtils.R2DBC_CONVERTER.writeValue(en.getValue(), k);
            resultMap.put(en.getKey(), v);
        }
        return new QueryFragment(resultMap);
    }

    /**
     * Creates a new QueryFragment instance with the specified columns.
     *
     * <p>This method initializes a new QueryFragment with an empty map and sets the specified columns.
     * It is a convenient way to create a QueryFragment with predefined columns.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * QueryFragment queryFragment = QueryFragment.withColumns("id", "name", "email");
     * }
     * </pre>
     *
     * @param columns the columns to be included in the QueryFragment
     * @return a new QueryFragment instance with the specified columns
     */
    public static QueryFragment withColumns(CharSequence... columns) {
        QueryFragment fragment = new QueryFragment(Map.of());
        return fragment.columns(columns);
    }

    /**
     * Creates a new QueryFragment instance with an empty map.
     *
     * <p>This method initializes a new QueryFragment with an empty map. It is a convenient way to create
     * a new QueryFragment instance without any initial parameters.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * QueryFragment queryFragment = QueryFragment.withNew();
     * }
     * </pre>
     *
     * @return a new QueryFragment instance with an empty map
     */
    public static QueryFragment withNew() {
        return new QueryFragment(Map.of());
    }

    /**
     * Creates a new QueryFragment instance with the specified size, offset, and parameters.
     *
     * <p>This method initializes a new QueryFragment with the provided parameters and sets the limit
     * for pagination using the specified size and offset values.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * Map<String, Object> params = Map.of("name", "John", "age", 30);
     * QueryFragment queryFragment = QueryFragment.withParams(10, 0, params);
     * }
     * </pre>
     *
     * @param size   the maximum number of rows to return (LIMIT clause)
     * @param offset the number of rows to skip before starting to return rows (OFFSET clause)
     * @param params the parameters to initialize the QueryFragment with
     * @return a new QueryFragment instance with the specified size, offset, and parameters
     */
    public static QueryFragment withParams(int size, long offset, Map<String, Object> params) {
        return withParams(params).limit(size, offset);
    }

    public QueryFragment in(String column, Iterable<?> values) {
        Assert.notNull(values, "In values not null!");
        Assert.hasText(column, "Column name must not be empty");

        StringJoiner joiner = new StringJoiner(",");
        AtomicInteger index = new AtomicInteger(0);
        values.forEach(item -> {
            var key = COLUMN_REPLACE_PATTERN.matcher(column).replaceAll("_") + index.getAndIncrement();
            joiner.add(":" + key);
            this.put(key, item);
        });

        String inClause = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column)
                + " IN (" + joiner + ")";
        this.where.add(inClause);
        return this;
    }

    public QueryFragment notIn(String column, Iterable<?> values) {
        Assert.notNull(values, "Not in values not null!");
        Assert.hasText(column, "Column name must not be empty");

        StringJoiner joiner = new StringJoiner(",");
        AtomicInteger index = new AtomicInteger(0);
        values.forEach(item -> {
            var key = COLUMN_REPLACE_PATTERN.matcher(column).replaceAll("_") + index.getAndIncrement();
            joiner.add(":" + key);
            this.put(key, item);
        });

        String inClause = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column)
                + " NOT IN (" + joiner + ")";
        this.where.add(inClause);
        return this;
    }

    /**
     * Add condition collections to the query.
     *
     * @param conditions the condition collections to be added to the query
     * @return the current QueryFragment instance for method chaining
     */
    public QueryFragment condition(Condition... conditions) {
        for (Condition condition : conditions) {
            this.putAll(condition);
            this.where.add(condition.toSql());
        }
        return this;
    }

    /**
     * Creates a new QueryFragment instance with the specified parameters.
     *
     * <p>This method initializes a new QueryFragment with the provided parameters and sets the limit
     * for pagination using the specified size and offset values.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * QueryFragment queryFragment = QueryFragment.of(existingQueryFragment);
     * }
     * </pre>
     *
     * @param params the parameters to initialize the QueryFragment with
     * @return a new QueryFragment instance with the specified parameters
     */
    public static QueryFragment of(QueryFragment params) {
        return of(Integer.MAX_VALUE, 0, params);
    }

    /**
     * Creates a new QueryFragment instance with the specified size, offset, and parameters.
     *
     * <p>This method initializes a new QueryFragment with the provided parameters and sets the limit
     * for pagination using the specified size and offset values.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * QueryFragment queryFragment = QueryFragment.of(10, 0, existingQueryFragment);
     * }
     * </pre>
     *
     * @param size   the maximum number of rows to return (LIMIT clause)
     * @param offset the number of rows to skip before starting to return rows (OFFSET clause)
     * @param params the parameters to initialize the QueryFragment with
     * @return a new QueryFragment instance with the specified size, offset, and parameters
     */
    public static QueryFragment of(int size, long offset, QueryFragment params) {
        return of(params).limit(size, offset);
    }

    /**
     * Adds the specified columns to the QueryFragment.
     *
     * <p>This method allows adding multiple columns to the QueryFragment's SELECT clause.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.columns("id", "name", "email");
     * }
     * </pre>
     *
     * @param columns the columns to be added to the QueryFragment
     * @return the QueryFragment instance with the added columns
     */
    public QueryFragment columns(CharSequence... columns) {
        for (CharSequence column : columns) {
            Assert.hasText(column.toString(), "Column name must not be empty");
            this.columns.add(column);
        }
        return this;
    }

    /**
     * Adds the specified queries to the FROM clause.
     *
     * <p>This method allows adding multiple queries to the FROM clause of the SQL statement.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.from("users", "orders");
     * }
     * </pre>
     *
     * @param queries the queries to be added to the FROM clause
     * @return the QueryFragment instance with the added queries
     */
    public QueryFragment from(CharSequence... queries) {
        for (CharSequence query : queries) {
            Assert.hasText(query.toString(), "Query must not be empty");
            this.from.add(query);
        }
        return this;
    }

    /**
     * Adds the specified condition to the WHERE clause.
     *
     * <p>This method allows adding a condition to the WHERE clause of the SQL statement.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.toSql("age > :age");
     * }
     * </pre>
     *
     * @param where the condition to be added to the WHERE clause
     * @return the QueryFragment instance with the added condition
     */
    public QueryFragment where(CharSequence where) {
        Assert.hasText(where.toString(), "WHERE condition must not be empty");
        this.where.add(where);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query.
     *
     * <p>This method allows adding an ORDER BY clause to the SQL statement.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.orderBy("name ASC");
     * }
     * </pre>
     *
     * @param order the order to add
     * @return the QueryFragment instance with the added ORDER BY clause
     */
    public QueryFragment orderBy(CharSequence order) {
        Assert.hasText(order.toString(), "Order must not be empty");
        this.orderBy.add(order);
        return this;
    }

    /**
     * Adds the specified columns to the GROUP BY clause.
     *
     * <p>This method allows adding multiple columns to the GROUP BY clause of the SQL statement.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.groupBy("category", "type");
     * }
     * </pre>
     *
     * @param columns the columns to be added to the GROUP BY clause
     * @return the QueryFragment instance with the added GROUP BY columns
     */
    public QueryFragment groupBy(CharSequence... columns) {
        for (CharSequence column : columns) {
            Assert.hasText(column.toString(), "GroupBy column must not be empty");
            this.groupBy.add(column);
        }
        return this;
    }

    /**
     * Configures the QueryFragment with pagination and sorting information from a Pageable object.
     *
     * <p>This method sets the LIMIT and OFFSET clauses based on the Pageable's page size and offset,
     * and applies sorting orders by transforming them to SQL ORDER BY clauses. Each sort property
     * can be converted to lowercase for case-insensitive sorting if specified in the Sort.Order.
     *
     * @param pageable the Pageable object containing pagination and sorting information
     * @return the QueryFragment instance with applied pagination and sorting
     */
    public QueryFragment pageable(Pageable pageable) {
        this.limit(pageable.getPageSize(), pageable.getOffset());
        var sort = QueryJsonHelper.transformSortForJson(pageable.getSort());
        for (Sort.Order order : sort) {
            String sortedPropertyName = order.getProperty();
            String sortedProperty = order.isIgnoreCase() ? "LOWER(" + sortedPropertyName + ")" : sortedPropertyName;
            this.orderBy(sortedProperty + (order.isAscending() ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * Adds a LIMIT and OFFSET clause to the query.
     *
     * <p>This method allows setting the LIMIT and OFFSET clauses for pagination in the SQL statement.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.limit(10, 0);
     * }
     * </pre>
     *
     * @param size   the maximum number of rows to return (LIMIT clause)
     * @param offset the number of rows to skip before starting to return rows (OFFSET clause)
     * @return the QueryFragment instance with the added LIMIT and OFFSET clauses
     */
    public QueryFragment limit(int size, long offset) {
        Assert.isTrue(size >= 0, "Size must be non-negative");
        Assert.isTrue(offset >= 0, "Offset must be non-negative");
        this.size = size;
        this.offset = offset;
        return this;
    }

    /**
     * Adds a full-text search condition to the query.
     *
     * <p>This method allows adding a full-text search condition to the SQL statement using the specified column and value.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * queryFragment.ts("description", "search term");
     * }
     * </pre>
     *
     * @param column the column to search
     * @param value  the value to search for
     * @return the QueryFragment instance with the added full-text search condition
     */
    public QueryFragment ts(String column, Object value) {
        Assert.hasText(column, "Column name must not be empty");

        String lowerCamelCol = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column);
        String queryTable = "ts_" + lowerCamelCol;

        // 使用更安全的列选择
        columns("TS_RANK_CD(" + lowerCamelCol + ", " + queryTable + ") AS rank");
        from(",TO_TSQUERY('chinese',:" + column + ") AS " + queryTable);
        where(queryTable + " @@ " + lowerCamelCol);
        put(column, value);
        return this;
    }

    /**
     * Adds a LIKE condition to the WHERE clause.
     *
     * @param column  the column to apply the LIKE condition to
     * @param pattern the pattern to match
     * @return the QueryFragment instance with the added LIKE condition
     */
    public QueryFragment like(String column, String pattern) {
        return addCondition(column, pattern, "LIKE");
    }

    /**
     * Adds a LIKE condition to the WHERE clause for values starting with the specified pattern.
     *
     * @param column  the column to apply the LIKE condition to
     * @param pattern the starting pattern to match
     * @return the QueryFragment instance with the added LIKE condition
     */
    public QueryFragment startingWith(String column, String pattern) {
        return like(column, pattern + "%");
    }

    /**
     * Adds a LIKE condition to the WHERE clause for values ending with the specified pattern.
     *
     * @param column  the column to apply the LIKE condition to
     * @param pattern the ending pattern to match
     * @return the QueryFragment instance with the added LIKE condition
     */
    public QueryFragment endingWith(String column, String pattern) {
        return like(column, "%" + pattern);
    }

    /**
     * Adds a NOT LIKE condition to the WHERE clause.
     *
     * @param column  the column to apply the NOT LIKE condition to
     * @param pattern the pattern to match
     * @return the QueryFragment instance with the added NOT LIKE condition
     */
    public QueryFragment notLike(String column, String pattern) {
        return addCondition(column, pattern, "NOT LIKE");
    }

    /**
     * Adds a BETWEEN condition to the WHERE clause.
     *
     * @param column the column to apply the BETWEEN condition to
     * @param value1 the lower bound of the range
     * @param value2 the upper bound of the range
     * @return the QueryFragment instance with the added BETWEEN condition
     */
    public QueryFragment between(String column, Object value1, Object value2) {
        String key1 = COLUMN_REPLACE_PATTERN.matcher(column).replaceAll("_") + "1";
        String key2 = COLUMN_REPLACE_PATTERN.matcher(column).replaceAll("_") + "2";
        this.put(key1, value1);
        this.put(key2, value2);

        String condition = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column)
                + " BETWEEN :" + key1 + " AND :" + key2;
        this.where.add(condition);
        return this;
    }

    /**
     * Adds an IS NULL condition to the WHERE clause.
     *
     * @param column the column to check for NULL
     * @return the QueryFragment instance with the added IS NULL condition
     */
    public QueryFragment isNull(String column) {
        return addCondition(column, null, "IS NULL");
    }

    /**
     * Adds an IS NOT NULL condition to the WHERE clause.
     *
     * @param column the column to check for NOT NULL
     * @return the QueryFragment instance with the added IS NOT NULL condition
     */
    public QueryFragment isNotNull(String column) {
        return addCondition(column, null, "IS NOT NULL");
    }

    /**
     * Adds a > condition to the WHERE clause.
     *
     * @param column the column to compare
     * @param value  the value to compare against
     * @return the QueryFragment instance with the added > condition
     */
    public QueryFragment after(String column, Object value) {
        return addCondition(column, value, ">");
    }

    /**
     * Adds a >= condition to the WHERE clause.
     *
     * @param column the column to compare
     * @param value  the value to compare against
     * @return the QueryFragment instance with the added >= condition
     */
    public QueryFragment greaterThanOrEqual(String column, Object value) {
        return addCondition(column, value, ">=");
    }

    /**
     * Adds a < condition to the WHERE clause.
     *
     * @param column the column to compare
     * @param value  the value to compare against
     * @return the QueryFragment instance with the added < condition
     */
    public QueryFragment before(String column, Object value) {
        return addCondition(column, value, "<");
    }

    /**
     * Adds a <= condition to the WHERE clause.
     *
     * @param column the column to compare
     * @param value  the value to compare against
     * @return the QueryFragment instance with the added <= condition
     */
    public QueryFragment lessThanOrEqual(String column, Object value) {
        return addCondition(column, value, "<=");
    }

    /**
     * Adds a != condition to the WHERE clause.
     *
     * @param column the column to compare
     * @param value  the value to compare against
     * @return the QueryFragment instance with the added != condition
     */
    public QueryFragment not(String column, Object value) {
        return addCondition(column, value, "!=");
    }

    private QueryFragment addCondition(String column, Object value, String operator) {
        Assert.hasText(column, "Column name must not be empty");

        if (value != null) {
            String key = COLUMN_REPLACE_PATTERN.matcher(column).replaceAll("_");
            this.put(key, value);
            String condition = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column)
                    + " " + operator + " :" + key;
            this.where.add(condition);
        } else if (operator.equals("IS NULL") || operator.equals("IS NOT NULL")) {
            String condition = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column)
                    + " " + operator;
            this.where.add(condition);
        }
        return this;
    }

    /**
     * Adds an IS TRUE condition to the WHERE clause.
     *
     * @param column the column to check for TRUE
     * @return the QueryFragment instance with the added IS TRUE condition
     */
    public QueryFragment isTrue(String column) {
        this.where.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column) + " IS TRUE");
        return this;
    }

    /**
     * Adds an IS FALSE condition to the WHERE clause.
     *
     * @param column the column to check for FALSE
     * @return the QueryFragment instance with the added IS FALSE condition
     */
    public QueryFragment isFalse(String column) {
        this.where.add(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column) + " IS FALSE");
        return this;
    }

    /**
     * Generates the WHERE clause of the SQL statement.
     *
     * <p>This method generates the WHERE clause based on the conditions added to the QueryFragment.
     *
     * @return the WHERE clause as a String
     */
    public String whereSql() {
        if (this.where.length() == 0) {
            return "";
        }
        return " WHERE " + this.where;
    }

    /**
     * Generates the ORDER BY clause of the SQL statement.
     *
     * <p>This method generates the ORDER BY clause based on the orders added to the QueryFragment.
     *
     * @return the ORDER BY clause as a String
     */
    public String orderSql() {
        if (this.orderBy.length() > 0) {
            return " ORDER BY " + this.orderBy;
        }
        return "";
    }

    /**
     * Generates the GROUP BY clause of the SQL statement.
     *
     * <p>This method generates the GROUP BY clause based on the columns added to the QueryFragment.
     *
     * <p>Example usage:
     * <pre>
     * {@code
     * String groupByClause = queryFragment.groupSql();
     * }
     * </pre>
     *
     * @return the GROUP BY clause as a String, or an empty String if no columns are added
     */
    public String groupSql() {
        if (this.groupBy.length() > 0) {
            return " GROUP BY " + this.groupBy;
        }
        return "";
    }

    /**
     * Generates the complete SQL from string based on the configured columns, table, conditions, and pagination.
     *
     * <p>The generated SQL from follows this structure:
     * <code>SELECT columns FROM table WHERE conditions ORDER BY order LIMIT size OFFSET offset</code>
     *
     * <p>If no table or columns are specified, an exception is thrown to prevent generating an invalid from.
     *
     * @return A String representing the complete SQL from.
     * @throws QueryException if the query is null, indicating that the form structure is incomplete.
     */
    public String querySql() {
        if (this.from.length() == 0) {
            throw QueryException.withError("This query is null, please use whereSql() method!",
                    new IllegalArgumentException("This query is null, please use whereSql() method"));
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(columns.length() > 0 ? columns : "*");
        sql.append(" FROM ").append(from);

        if (where.length() > 0) {
            sql.append(" WHERE ").append(where);
        }

        if (groupBy.length() > 0) {
            sql.append(" GROUP BY ").append(groupBy);
        }

        if (orderBy.length() > 0) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        sql.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);
        return sql.toString();
    }

    /**
     * Generates the COUNT SQL from string based on the configured conditions.
     *
     * <p>The generated COUNT SQL from follows this structure:
     * <code>SELECT COUNT(*) FROM (SELECT columns FROM table WHERE conditions) t</code>
     *
     * <p>If no table or columns are specified, an exception is thrown to prevent generating an invalid from.
     *
     * @return A String representing the COUNT SQL from.
     * @throws QueryException if the countSql is null, indicating that the form structure is incomplete.
     */
    public String countSql() {
        if (this.from.length() == 0) {
            throw QueryException.withError("This countSql is null, please use whereSql() method!",
                    new IllegalArgumentException("This countSql is null, please use whereSql() method"));
        }

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM (SELECT 1 FROM ")
                .append(from);

        if (where.length() > 0) {
            sql.append(" WHERE ").append(where);
        }

        return sql.append(") t").toString();
    }
}