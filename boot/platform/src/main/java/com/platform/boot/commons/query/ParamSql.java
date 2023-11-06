package com.platform.boot.commons.query;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record ParamSql(StringJoiner sql, Map<String, Object> params) implements Serializable {
    public final static ParamSql EMPTY = ParamSql.of(new StringJoiner(" AND "), Maps.newHashMap());

    /**
     * Creates a new ParamSql instance with the given SQL string and parameters.
     *
     * @param sql    The SQL string.
     * @param params The map of parameter values.
     * @return A new ParamSql instance.
     */
    public static ParamSql of(StringJoiner sql, Map<String, Object> params) {
        return new ParamSql(sql, params);
    }

    /**
     * Returns the WHERE clause of the SQL query.
     *
     * @return The WHERE clause.
     */
    public String whereSql() {
        if (this.sql.length() > 0) {
            return " Where " + this.sql;
        }
        return "";
    }
}