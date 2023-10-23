package com.platform.boot.commons.query;

import java.util.Map;
import java.util.StringJoiner;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record BindSql(StringJoiner sql, Map<String, Object> params) {

    public static BindSql of(StringJoiner sql, Map<String, Object> params) {
        return new BindSql(sql, params);
    }
}