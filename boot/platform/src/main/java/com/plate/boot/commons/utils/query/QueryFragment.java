package com.plate.boot.commons.utils.query;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment
 * and a map of parameters to be bound to a PreparedStatement.
 * This record facilitates the construction of dynamic SQL queries with placeholders
 * for improved performance and security against SQL injection.
 *
 * @param sql     A {@link StringJoiner} containing the dynamically built WHERE clause
 *                fragments of a SQL query, joined by 'and'.
 * @param params  A {@link Map} mapping parameter names to their respective values,
 *                which are intended to replace placeholders in the SQL query.
 */
public record QueryFragment(StringJoiner sql, Map<String, Object> params) implements Serializable {

    /**
     * Creates a new instance of {@link QueryFragment} with the provided conditional SQL
     * fragment and parameters map.
     *
     * @param sql    A {@link StringJoiner} object containing the dynamically
     *               constructed WHERE clause segments of a SQL query, concatenated by 'and'.
     * @param params A {@link Map} of parameter names to values, which will be
     *               substituted for placeholders within the SQL query to prevent SQL injection.
     * @return A new {@link QueryFragment} instance encapsulating the given SQL fragment
     * and parameters map, ready for use in preparing a parameterized SQL statement.
     */
    public static QueryFragment of(StringJoiner sql, Map<String, Object> params) {
        return new QueryFragment(sql, params);
    }

    /**
     * Constructs a WHERE clause segment for a SQL query based on the accumulated conditions.
     * If conditions have been added, it prepends the 'WHERE' keyword followed by the conditions;
     * otherwise, it returns an empty string.
     *
     * @return A String representing the WHERE clause with conditions or an empty string if no conditions exist.
     */
    public String whereSql() {
        if (this.sql.length() > 0) {
            return " where " + this.sql;
        }
        return "";
    }
}