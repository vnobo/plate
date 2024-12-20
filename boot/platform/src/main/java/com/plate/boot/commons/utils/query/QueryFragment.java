package com.plate.boot.commons.utils.query;

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
 *     .addColumn("id", "name", "email")
 *     .addQuery("users")
 *     .addWhere("age > :age", 18)
 *     .addOrder("name ASC")
 *     .addOrder("email DESC");
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
     * queryFragment.addColumn("id", "name", "email");
     * }
     * </pre>
     */
    private final StringJoiner columns = new StringJoiner(",");

    /**
     * A StringJoiner to accumulate the main SQL query parts (e.g., table names).
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.addQuery("users");
     * }
     * </pre>
     */
    private final StringJoiner select = new StringJoiner(" ");

    /**
     * A StringJoiner to accumulate WHERE conditions.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.addWhere("age > :age");
     * }
     * </pre>
     */
    private final StringJoiner where = new StringJoiner(" AND ");

    /**
     * A StringJoiner to accumulate ORDER BY clauses.
     * Example usage:
     * <pre>
     * {@code
     * queryFragment.addOrder("name ASC");
     * }
     * </pre>
     */
    private final StringJoiner orderBy = new StringJoiner(",");

    private final StringJoiner groupBy = new StringJoiner(",");

    /**
     * The maximum number of rows to return (LIMIT clause).
     */
    private final int size;

    /**
     * The number of rows to skip before starting to return rows (OFFSET clause).
     */
    private final long offset;

    public QueryFragment(int size, long offset, QueryFragment params) {
        super(16);
        this.size = size;
        this.offset = offset;
        this.mergeWhere(params.getWhere());
        this.putAll(params);
    }

    public QueryFragment(int size, long offset, Map<String, Object> params) {
        super(16);
        this.size = size;
        this.offset = offset;
        this.putAll(params);
    }

    public static QueryFragment withNew() {
        return withMap(Map.of());
    }

    public static QueryFragment withMap(Map<String, Object> params) {
        return new QueryFragment(Integer.MAX_VALUE, 0, params);
    }

    public static QueryFragment withMap(int size, long offset, Map<String, Object> params) {
        return new QueryFragment(size, offset, params);
    }

    public static QueryFragment of(QueryFragment params) {
        return of(Integer.MAX_VALUE, 0, params);
    }

    public static QueryFragment of(int size, long offset, QueryFragment params) {
        return new QueryFragment(size, offset, params);
    }

    public QueryFragment addColumn(CharSequence... columns) {
        for (CharSequence column : columns) {
            this.columns.add(column);
        }
        return this;
    }

    public QueryFragment addQuery(CharSequence... queries) {
        this.select.setEmptyValue("");
        for (CharSequence query : queries) {
            this.select.add(query);
        }
        return this;
    }

    public QueryFragment addWhere(CharSequence where) {
        this.where.add(where);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query.
     *
     * @param order the order
     * @return this
     */
    public QueryFragment addOrder(CharSequence order) {
        this.orderBy.add(order);
        return this;
    }

    /**
     * Merges the given where clause with the existing one.
     *
     * @param where the where
     * @return this
     */
    public QueryFragment mergeWhere(StringJoiner where) {
        this.where.merge(where);
        return this;
    }

    /**
     * Merges the given order clause with the existing one.
     *
     * @param order the order
     * @return this
     */
    public QueryFragment mergeOrder(StringJoiner order) {
        this.orderBy.merge(order);
        return this;
    }

    /**
     * Generates the WHERE clause part of a SQL query based on the stored conditions.
     * If conditions have been accumulated, it prefixes the conditions with the 'WHERE' keyword;
     * otherwise, it returns an empty string to indicate no conditions.
     *
     * @return A String forming the WHERE clause of the SQL query, or an empty string if no conditions are present.
     */
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
        if (this.select.length() > 0) {
            return String.format("SELECT %s FROM %s %s %s LIMIT %d OFFSET %d",
                    this.columns, this.select, whereSql(), orderSql(), this.size, this.offset);
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
        if (this.select.length() > 0) {
            return "SELECT COUNT(*) FROM (" + String.format("SELECT %s FROM %s", this.columns, this.select)
                    + whereSql() + ") t";
        }
        throw QueryException.withError("This countSql is null, please use whereSql() method!",
                new IllegalArgumentException("This countSql is null, please use whereSql() method"));
    }
}