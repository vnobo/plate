package com.plate.boot.commons.utils.query;

import com.plate.boot.commons.exception.QueryException;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents a SQL parameter structure consisting of a conditional SQL fragment
 * and a map of parameters to be bound to a PreparedStatement.
 * This record facilitates the construction of dynamic SQL queries with placeholders
 * for improved performance and security against SQL injection.
 */
@Getter
public class QueryFragment extends HashMap<String, Object> {

    private final StringJoiner querySql = new StringJoiner(",");

    private final StringJoiner whereSql = new StringJoiner(" AND ");

    private final StringJoiner orderSql = new StringJoiner(",");

    private final String pageSql;

    public QueryFragment(String pageSql, Map<String, Object> params) {
        super(16);
        this.pageSql = pageSql;
        this.putAll(params);
    }

    /**
     * Creates a new instance of {@link QueryFragment} with the provided conditional SQL
     * fragment and parameters map.
     *
     * @param pageSql A {@link StringJoiner} object containing the dynamically
     *                constructed WHERE clause segments of a SQL query, concatenated by 'and'.
     * @param params  A {@link Map} of parameter names to values, which will be
     *                substituted for placeholders within the SQL query to prevent SQL injection.
     * @return A new {@link QueryFragment} instance encapsulating the given SQL fragment
     * and parameters map, ready for use in preparing a parameterized SQL statement.
     */
    public static QueryFragment of(String pageSql, Map<String, Object> params) {
        return new QueryFragment(pageSql, params);
    }

    public QueryFragment addQuery(CharSequence query) {
        querySql.add(query);
        return this;
    }

    public QueryFragment addWhere(CharSequence where) {
        whereSql.add(where);
        return this;
    }

    public QueryFragment addOrder(CharSequence order) {
        orderSql.add(order);
        return this;
    }

    public QueryFragment mergeWhere(StringJoiner where) {
        whereSql.merge(where);
        return this;
    }

    public QueryFragment mergeOrder(StringJoiner order) {
        orderSql.merge(order);
        return this;
    }

    public QueryFragment merge(QueryFragment fragment) {
        this.putAll(fragment);
        this.querySql.merge(fragment.getQuerySql());
        this.whereSql.merge(fragment.getWhereSql());
        this.orderSql.merge(fragment.getOrderSql());
        return of(fragment.getPageSql(), this);
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

    public String orderSql() {
        if (this.orderSql.length() > 0) {
            return " ORDER BY " + this.orderSql;
        }
        return "";
    }

    public String querySql() {
        if (this.querySql.length() > 0) {
            return this.querySql + whereSql() + orderSql() + (StringUtils.hasLength(this.pageSql) ? this.pageSql : "");
        }
        throw QueryException.withError("This querySql is null, please use whereSql() method!",
                new IllegalArgumentException("This querySql is null, please use whereSql() method"));
    }

    public String countSql() {
        if (this.querySql.length() > 0) {
            return "SELECT COUNT(*) FROM (" + this.querySql + whereSql() + ") t";
        }
        throw QueryException.withError("This countSql is null, please use whereSql() method!",
                new IllegalArgumentException("This countSql is null, please use whereSql() method"));
    }
}