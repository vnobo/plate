package com.plate.boot.commons.utils.query;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment
 * and a map of parameters to be bound to a PreparedStatement.
 * This record facilitates the construction of dynamic SQL queries with placeholders
 * for improved performance and security against SQL injection.
 */
public class QueryFragment extends HashMap<String, Object> {

    private final StringJoiner sql;

    public QueryFragment(StringJoiner sql, Map<String, Object> params) {
        super(16);
        Assert.notNull(sql, "sql must not be null!");
        Assert.notNull(params, "params must not be null!");
        this.sql = sql;
        this.putAll(params);
    }

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
    public Map<String, Object> params() {
        return this;
    }

    /**
     * Retrieves the {@link StringJoiner} instance containing the dynamically
     * constructed SQL fragments of this {@link QueryFragment} object.
     * <p>
     * This method is useful for accessing the SQL fragment that represents
     * a part of a query, such as a WHERE clause, which can be further
     * incorporated into a complete SQL statement.
     *
     * @return The {@link StringJoiner} object holding the concatenated SQL segments.
     */
    public StringJoiner sql() {
        return this.sql;
    }

    /**
     * Generates the WHERE clause part of a SQL query based on the stored conditions.
     * If conditions have been accumulated, it prefixes the conditions with the 'WHERE' keyword;
     * otherwise, it returns an empty string to indicate no conditions.
     *
     * @return A String forming the WHERE clause of the SQL query, or an empty string if no conditions are present.
     */
    public String whereSql() {
        if (this.sql.length() > 0) {
            return " where " + this.sql;
        }
        return "";
    }
}