package com.plate.boot.commons.utils.query;

import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment
 * and a map of parameters to be bound to a PreparedStatement.
 * This record facilitates the construction of dynamic SQL queries with placeholders
 * for improved performance and security against SQL injection.
 */
public class QueryFragment extends HashMap<String, Object> {

    private final String querySql;

    private final StringJoiner whereSql;

    private final String orderSql;

    public QueryFragment(String querySql, StringJoiner whereSql, String orderSql, Map<String, Object> params) {
        super(16);
        Assert.notNull(whereSql, "whereSqlJoiner must not be null!");
        Assert.notNull(params, "params must not be null!");
        this.querySql = querySql;
        this.whereSql = whereSql;
        this.orderSql = orderSql;
        this.putAll(params);
    }

    /**
     * Creates a new instance of {@link QueryFragment} with the provided conditional SQL
     * fragment and parameters map.
     *
     * @param whereSql A {@link StringJoiner} object containing the dynamically
     *                 constructed WHERE clause segments of a SQL query, concatenated by 'and'.
     * @param params   A {@link Map} of parameter names to values, which will be
     *                 substituted for placeholders within the SQL query to prevent SQL injection.
     * @return A new {@link QueryFragment} instance encapsulating the given SQL fragment
     * and parameters map, ready for use in preparing a parameterized SQL statement.
     */
    public static QueryFragment of(StringJoiner whereSql, Map<String, Object> params) {
        return of(null, whereSql, "", params);
    }

    public static QueryFragment of(String querySql, StringJoiner whereSql, Map<String, Object> params) {
        return of(querySql, whereSql, "", params);
    }

    public static QueryFragment of(String querySql, StringJoiner whereSql, String orderSql, Map<String, Object> params) {
        return new QueryFragment(querySql, whereSql, orderSql, params);
    }

    public static QueryFragment query(Object object, Pageable pageable) {
        return query(object, pageable, List.of(), null);
    }

    public static QueryFragment query(Object object, Pageable pageable, String prefix) {
        return query(object, pageable, List.of(), prefix);
    }

    public static QueryFragment query(Object object, Pageable pageable, Collection<String> skipKeys, String prefix) {
        QueryFragment queryFragment = QueryHelper.query(object, skipKeys, prefix);
        String orderSql = "";
        if (!ObjectUtils.isEmpty(pageable)) {
            orderSql = QueryHelper.applyPage(pageable, prefix);
        }
        return QueryFragment.of(queryFragment.querySqlJoiner(), queryFragment.whereSqlJoiner(), orderSql, queryFragment);
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
    public StringJoiner whereSqlJoiner() {
        return this.whereSql;
    }

    public String querySqlJoiner() {
        return this.querySql;
    }

    /**
     * Generates the WHERE clause part of a SQL query based on the stored conditions.
     * If conditions have been accumulated, it prefixes the conditions with the 'WHERE' keyword;
     * otherwise, it returns an empty string to indicate no conditions.
     *
     * @return A String forming the WHERE clause of the SQL query, or an empty string if no conditions are present.
     */
    public String whereSql() {
        if (this.whereSql.length() > 0) {
            return " WHERE " + this.whereSql;
        }
        return "";
    }

    public String querySql() {
        if (StringUtils.hasLength(this.querySql)) {
            return this.querySql + whereSql() + this.orderSql;
        }
        throw new NullPointerException("This querySql is null, please use whereSql() method!");
    }

    public String countSql() {
        if (StringUtils.hasLength(this.querySql)) {
            return "SELECT COUNT(*) FROM (" + this.querySql + whereSql() + ") t";
        }
        throw new NullPointerException("This querySql is null, please use whereSql() method!");
    }
}