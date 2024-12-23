package com.plate.boot.commons.utils.query;

import com.google.common.base.CaseFormat;
import com.plate.boot.commons.exception.QueryException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment and a map of parameters
 * to be bound to a PreparedStatement. This class facilitates the construction of dynamic SQL queries
 * with placeholders for improved performance and security against SQL injection.
 *
 * <p>The QueryFragment class is designed to be flexible and modular, allowing users to build complex
 * SQL queries by chaining method calls. It manages the SQL query structure, including the SELECT
 * columns, FROM clause, WHERE conditions, ORDER BY clause, and LIMIT/OFFSET for pagination.
 *
 * <p>Example usage:
 * <pre>
 * {@code
 * QueryFragment queryFragment = QueryFragment.withNew()
 *     .columns("id", "name", "email")
 *     .query("users")
 *     .where("age > :age", 18)
 *     .orderBy("name ASC")
 *     .orderBy("email DESC");
 *
 * // Bind parameters
 * queryFragment.put("age", 18);
 *
 * // Generate SQL query
 * String sql = queryFragment.querySql();
 * System.out.println(sql);
 * }
 * </pre>
 * In this example, a QueryFragment instance is created and configured with columns, a table name,
 * a WHERE condition, and ORDER BY clauses. Parameters are added to the query fragment, and finally,
 * the SQL query string is generated using the querySql() method.
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
     * A StringJoiner to accumulate the main SQL query parts (e.g., table names).
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.query("users");
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

    public static QueryFragment withNew() {
        return withMap(Map.of());
    }

    public static QueryFragment withMap(Map<String, Object> params) {
        return new QueryFragment(params);
    }

    public static QueryFragment withMap(int size, long offset, Map<String, Object> params) {
        return withMap(params).limit(size, offset);
    }

    public static QueryFragment of(QueryFragment params) {
        return of(Integer.MAX_VALUE, 0, params);
    }

    public static QueryFragment of(int size, long offset, QueryFragment params) {
        return of(params).limit(size, offset);
    }

    public QueryFragment columns(CharSequence... columns) {
        for (CharSequence column : columns) {
            this.columns.add(column);
        }
        return this;
    }

    public QueryFragment query(CharSequence... queries) {
        this.from.setEmptyValue("");
        for (CharSequence query : queries) {
            this.from.add(query);
        }
        return this;
    }

    public QueryFragment where(CharSequence where) {
        this.where.add(where);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query.
     *
     * @param order the order to add
     * @return the QueryFragment instance
     */
    public QueryFragment orderBy(CharSequence order) {
        this.orderBy.add(order);
        return this;
    }

    /**
     * Adds a GROUP BY clause to the query.
     *
     * @param size   page size
     * @param offset page offset
     * @return the QueryFragment instance
     */
    public QueryFragment limit(int size, long offset) {
        this.size = size;
        this.offset = offset;
        return this;
    }

    public QueryFragment ts(String column, Object value) {
        String lowerCamelCol = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, column);
        String queryTable = "ts_" + lowerCamelCol;
        columns("TS_RANK_CD(" + lowerCamelCol + ", " + queryTable + ") AS rank");
        query(",TO_TSQUERY('chinese',:" + column + ") AS " + queryTable);
        where(queryTable + " @@ " + lowerCamelCol);
        put(column, value);
        return this;
    }

    public String whereSql() {
        if (this.where.length() > 0) {
            return " WHERE " + this.where;
        }
        return "";
    }

    public String orderSql() {
        if (this.orderBy.length() > 0) {
            return " ORDER BY " + this.orderBy;
        }
        return "";
    }

    /**
     * Generates the complete SQL query string based on the configured columns, table, conditions, and pagination.
     *
     * <p>The generated SQL query follows this structure:
     * <code>SELECT columns FROM table WHERE conditions ORDER BY order LIMIT size OFFSET offset</code>
     *
     * <p>If no table or columns are specified, an exception is thrown to prevent generating an invalid query.
     *
     * @return A String representing the complete SQL query.
     * @throws QueryException if the querySql is null, indicating that the query structure is incomplete.
     */
    public String querySql() {
        if (this.from.length() > 0) {
            return String.format("SELECT %s FROM %s %s %s LIMIT %d OFFSET %d",
                    this.columns, this.from, whereSql(), orderSql(), this.size, this.offset);
        }
        throw QueryException.withError("This querySql is null, please use whereSql() method!",
                new IllegalArgumentException("This querySql is null, please use whereSql() method"));
    }

    /**
     * Generates the COUNT SQL query string based on the configured conditions.
     *
     * <p>The generated COUNT SQL query follows this structure:
     * <code>SELECT COUNT(*) FROM (SELECT columns FROM table WHERE conditions) t</code>
     *
     * <p>If no table or columns are specified, an exception is thrown to prevent generating an invalid query.
     *
     * @return A String representing the COUNT SQL query.
     * @throws QueryException if the countSql is null, indicating that the query structure is incomplete.
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