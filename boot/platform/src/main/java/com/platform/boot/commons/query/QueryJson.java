package com.platform.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
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
        KEYWORDS.put("EQ", "=");
        KEYWORDS.put("Equal", "=");
        KEYWORDS.put("After", ">");
        KEYWORDS.put("GreaterThanEqual", ">=");
        KEYWORDS.put("GTE", ">=");
        KEYWORDS.put("GreaterThan", ">");
        KEYWORDS.put("GT", ">");
        KEYWORDS.put("Before", "<");
        KEYWORDS.put("LessThanEqual", "<=");
        KEYWORDS.put("LTE", "<=");
        KEYWORDS.put("LessThan", "<");
        KEYWORDS.put("LT", "<");
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
    public static ParamSql queryJson(Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params)) {
            return ParamSql.EMPTY;
        }
        Map<String, Object> bindParams = Maps.newHashMap();
        StringJoiner whereSql = new StringJoiner(" and ");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), ".");
            Map.Entry<String, String> exps = jsonPathKeyAndParamName(keys);
            whereSql.add(exps.getValue());
            bindParams.put(exps.getKey(), entry.getValue());
        }
        return ParamSql.of(whereSql, bindParams);
    }

    private static Map.Entry<String, String> jsonPathKeyAndParamName(String[] keys) {
        String lastKey = keys[keys.length - 1];
        String colum = keys[0];

        Map.Entry<String, String> exps = exitsKeyWords(lastKey);

        StringBuilder jsonPath = new StringBuilder(colum);

        String[] joinKeys = Arrays.copyOfRange(keys, 1, keys.length - 1);
        for (String path : joinKeys) {
            var capath = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, path);
            jsonPath.append("->'").append(capath).append("'");
        }

        String key = lastKey.substring(0, lastKey.length() - exps.getKey().length());
        jsonPath.append("->>'").append(key).append("' ");
        String paramName = StringUtils.arrayToDelimitedString(keys, "_");
        if (!ObjectUtils.isEmpty(exps)) {
            jsonPath.append(exps.getValue()).append(" :").append(paramName);
        } else {
            jsonPath.append("=").append(" :").append(paramName);
        }
        return Map.entry(paramName, jsonPath.toString());
    }

    private static Map.Entry<String, String> exitsKeyWords(String inputStr) {
        Set<Map.Entry<String, String>> entries = KEYWORDS.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(inputStr, entry.getKey()))
                .collect(Collectors.toSet());
        Assert.notNull(entries, "Not support key words: " + inputStr);
        return entries.stream().max((entry1, entry2) -> {
            int entry1Length = entry1.getKey().length();
            int entry2Length = entry2.getKey().length();
            return Integer.compare(entry1Length, entry2Length);
        }).orElseThrow();
    }
}