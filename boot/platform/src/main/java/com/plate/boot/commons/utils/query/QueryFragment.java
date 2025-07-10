package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.DatabaseUtils;
import lombok.Getter;
import org.springframework.data.util.TypeInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment and a map of parameters
 * to be bound to a PreparedStatement. This class facilitates the construction of dynamic SQL queries
 * with placeholders for improved performance and security against SQL injection.
 *
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
 *     .where("age > :age", 18)
 *     .orderBy("name ASC")
 *     .orderBy("email DESC");
 *
 * // Bind parameters
 * queryFragment.put("age", 18);
 *
 * // Generate SQL from
 * String sql = queryFragment.querySql();
 * System.out.println(sql);
 * }
 * </pre>
 * In this example, a QueryFragment instance is created and configured with columns, a table name,
 * a WHERE condition, and ORDER BY clauses. Parameters are added to the form fragment, and finally,
 * the SQL from string is generated using the querySql() method.
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
    private final StringJoiner columns = new StringJoiner(",");

    /**
     * A StringJoiner to accumulate the main SQL from parts (e.g., table names).
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.from("users");
     * }
     * </pre>
     */
    private final StringJoiner from = new StringJoiner(" ");

    /**
     * A StringJoiner to accumulate WHERE conditions.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.where("age > :age");
     * }
     * </pre>
     */
    private final StringJoiner where = new StringJoiner(" AND ");

    /**
     * A StringJoiner to accumulate ORDER BY clauses.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.orderBy("name ASC");
     * }
     * </pre>
     */
    private final StringJoiner orderBy = new StringJoiner(",");

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
    private final StringJoiner groupBy = new StringJoiner(",");

    /**
     * The maximum number of rows to return (LIMIT clause).
     */
    private int size = 25;

    /**
     * The number of rows to skip before starting to return rows (OFFSET clause).
     */
    private long offset = 0;

    public QueryFragment(QueryFragment fragment) {
        super(fragment);
        this.size = fragment.size;
        this.offset = fragment.offset;
        this.columns.merge(fragment.getColumns());
        this.from.merge(fragment.getFrom());
        this.orderBy.merge(fragment.getOrderBy());
        this.where.merge(fragment.getWhere());
    }

    /**
     * Creates a new QueryFragment instance with the specified parameters.
     *
     * @param params the parameters to initialize the QueryFragment with
     */
    public QueryFragment(Map<String, Object> params) {
        super(params);
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
        return withMap(Map.of()).columns(columns);
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
        return withMap(Map.of());
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
     * QueryFragment queryFragment = QueryFragment.withMap(params);
     * }
     * </pre>
     *
     * @param params the parameters to initialize the QueryFragment with
     * @return a new QueryFragment instance with the specified parameters
     */
    public static QueryFragment withMap(Map<String, Object> params) {
        var resultMap = new HashMap<String, Object>();
        for (var en : params.entrySet()) {
            var t = TypeInformation.of(en.getValue().getClass());
            var c = DatabaseUtils.R2DBC_CONVERTER.writeValue(en.getValue(), t);
            resultMap.put(en.getKey(), c);
        }
        return new QueryFragment(params);
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
     * QueryFragment queryFragment = QueryFragment.withMap(10, 0, params);
     * }
     * </pre>
     *
     * @param size   the maximum number of rows to return (LIMIT clause)
     * @param offset the number of rows to skip before starting to return rows (OFFSET clause)
     * @param params the parameters to initialize the QueryFragment with
     * @return a new QueryFragment instance with the specified size, offset, and parameters
     */
    public static QueryFragment withMap(int size, long offset, Map<String, Object> params) {
        return withMap(params).limit(size, offset);
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
        this.from.setEmptyValue("");
        for (CharSequence query : queries) {
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
     * queryFragment.where("age > :age");
     * }
     * </pre>
     *
     * @param where the condition to be added to the WHERE clause
     * @return the QueryFragment instance with the added condition
     */
    public QueryFragment where(CharSequence where) {
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
            this.groupBy.add(column);
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
        String lowerCamelCol = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column);
        String queryTable = "ts_" + lowerCamelCol;
        columns("TS_RANK_CD(" + lowerCamelCol + ", " + queryTable + ") AS rank");
        from(",TO_TSQUERY('chinese',:" + column + ") AS " + queryTable);
        where(queryTable + " @@ " + lowerCamelCol);
        put(column, value);
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
        if (this.where.length() > 0) {
            return " WHERE " + this.where;
        }
        return "";
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
     * @throws QueryException if the querySql is null, indicating that the form structure is incomplete.
     */
    public String querySql() {
        if (this.from.length() > 0) {
            return String.format("SELECT %s FROM %s %s %s %s LIMIT %d OFFSET %d",
                    this.columns, this.from, whereSql(), orderSql(), groupSql(), this.size, this.offset);
        }
        throw QueryException.withError("This querySql is null, please use whereSql() method!",
                new IllegalArgumentException("This querySql is null, please use whereSql() method"));
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
        if (this.from.length() > 0) {
            return "SELECT COUNT(*) FROM (" + String.format("SELECT %s FROM %s", this.columns, this.from)
                    + whereSql() + ") t";
        }
        throw QueryException.withError("This countSql is null, please use whereSql() method!",
                new IllegalArgumentException("This countSql is null, please use whereSql() method"));
    }
}