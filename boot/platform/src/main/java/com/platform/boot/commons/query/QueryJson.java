package com.platform.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public class QueryJson {

    private final static Map<String, String> KEYWORDS = Maps.newHashMap();

    static {
        KEYWORDS.put("After", ">");
        KEYWORDS.put("GreaterThanEqual", ">=");
        KEYWORDS.put("GreaterThan", ">");
        KEYWORDS.put("Before", "<");
        KEYWORDS.put("LessThanEqual", "<=");
        KEYWORDS.put("LessThan", "<");
        KEYWORDS.put("Between", "BETWEEN");
        KEYWORDS.put("NotBetween", "NOT BETWEEN");
        KEYWORDS.put("NotIn", "NOT IN");
        KEYWORDS.put("In", "IN");
        KEYWORDS.put("IsNotNull", "IS NOT NULL");
        KEYWORDS.put("NotNull", "IS NOT NULL");
        KEYWORDS.put("IsNull", "IS NULL");
        KEYWORDS.put("Null", "IS NULL");
        KEYWORDS.put("NotLike", "NOT LIKE");
        KEYWORDS.put("Like", "LIKE");
        KEYWORDS.put("StartingWith", "LIKE");
        KEYWORDS.put("EndingWith", "LIKE");
        KEYWORDS.put("IsNotLike", "NOT LIKE");
        KEYWORDS.put("Containing", "LIKE");
        KEYWORDS.put("NotContaining", "NOT LIKE");
        KEYWORDS.put("Not", "!=");
        KEYWORDS.put("IsTrue", "IS TRUE");
        KEYWORDS.put("True", "IS TRUE");
        KEYWORDS.put("IsFalse", "IS FALSE");
        KEYWORDS.put("False", "IS FALSE");
    }

    /**
     * Generates a JSON query based on the provided parameters and returns a map
     * of the generated SQL query and its corresponding parameters.
     *
     * @param params a map of key-value pairs representing the parameters for the query
     * @return a map containing the generated SQL query and its parameters
     */
    public static BindSql queryJson(Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params)) {
            return BindSql.of(new StringJoiner(""), Maps.newHashMap());
        }
        Map<String, Object> bindParams = Maps.newHashMap();
        StringJoiner whereSql = new StringJoiner(" and ");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), "Query");
            String column = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, keys[0]);
            String key = keys[1];
            Map.Entry<String, String> exps = exitsKeyWords(key);
            String jsonPath = ObjectUtils.isEmpty(exps) ? key : StringUtils.replace(key, exps.getKey(), "");
            if (!ObjectUtils.isEmpty(value)) {
                whereSql.add(column + "->>'" + jsonPath + "' = :" + jsonPath);
                bindParams.put(jsonPath, value);
            }
        }
        return BindSql.of(whereSql, bindParams);
    }

    private static Map.Entry<String, String> exitsKeyWords(String inputStr) {
        Set<Map.Entry<String, String>> entries = KEYWORDS.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(inputStr, entry.getKey()))
                .collect(Collectors.toSet());
        if (entries.isEmpty()) {
            return null;
        }
        return entries.stream().max((entry1, entry2) -> {
            int entry1Length = entry1.getKey().length();
            int entry2Length = entry2.getKey().length();
            return Integer.compare(entry1Length, entry2Length);
        }).orElseThrow();
    }
}