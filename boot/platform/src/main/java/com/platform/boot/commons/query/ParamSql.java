package com.platform.boot.commons.query;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.StringJoiner;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record ParamSql(StringJoiner sql, Map<String, Object> params) {
    public final static ParamSql EMPTY = ParamSql.of(new StringJoiner(" AND "), Maps.newHashMap());

    public static ParamSql of(StringJoiner sql, Map<String, Object> params) {
        return new ParamSql(sql, params);
    }

    public String whereSql() {
        if (this.sql.length() > 0) {
            return "Where " + this.sql;
        }
        return "";
    }
}