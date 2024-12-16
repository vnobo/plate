package com.plate.boot.commons.utils.query;

import com.plate.boot.commons.exception.QueryException;
import lombok.Getter;

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

    private final StringJoiner columns = new StringJoiner(",");

    private final StringJoiner querySql = new StringJoiner(" ");

    private final StringJoiner whereSql = new StringJoiner(" AND ");

    private final StringJoiner orderSql = new StringJoiner(",");

    private final int size;

    private final long offset;

    public QueryFragment(int size, long offset, QueryFragment params) {
        super(16);
        this.size = size;
        this.offset = offset;
        this.mergeWhere(params.getWhereSql());
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
        this.querySql.setEmptyValue("");
        for (CharSequence query : queries) {
            this.querySql.add(query);
        }
        return this;
    }

    public QueryFragment addWhere(CharSequence where) {
        whereSql.add(where);
        return this;
    }

    /**
     * Adds an ORDER BY clause to the query.
     *
     * @param order the order
     * @return this
     */
    public QueryFragment addOrder(CharSequence order) {
        orderSql.add(order);
        return this;
    }

    /**
     * Merges the given where clause with the existing one.
     *
     * @param where the where
     * @return this
     */
    public QueryFragment mergeWhere(StringJoiner where) {
        whereSql.merge(where);
        return this;
    }

    public QueryFragment mergeOrder(StringJoiner order) {
        orderSql.merge(order);
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
            return String.format("SELECT %s FROM %s %s %s LIMIT %d OFFSET %d",
                    this.columns, this.querySql, whereSql(), orderSql(), this.size, this.offset);
        }
        throw QueryException.withError("This querySql is null, please use whereSql() method!",
                new IllegalArgumentException("This querySql is null, please use whereSql() method"));
    }

    public String countSql() {
        if (this.querySql.length() > 0) {
            return "SELECT COUNT(*) FROM (" + String.format("SELECT %s FROM %s", this.columns, this.querySql)
                    + whereSql() + ") t";
        }
        throw QueryException.withError("This countSql is null, please use whereSql() method!",
                new IllegalArgumentException("This countSql is null, please use whereSql() method"));
    }
}