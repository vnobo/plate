package com.platform.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.platform.boot.commons.exception.RestServerException;
import org.springframework.data.domain.Sort;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

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
        KEYWORDS.put("Between", "between");
        KEYWORDS.put("NotBetween", "not between");
        KEYWORDS.put("NotIn", "not in");
        KEYWORDS.put("In", "in");
        KEYWORDS.put("IsNotNull", "is not null");
        KEYWORDS.put("NotNull", "is not null");
        KEYWORDS.put("IsNull", "is null");
        KEYWORDS.put("Null", "is null");
        KEYWORDS.put("NotLike", "not like");
        KEYWORDS.put("Like", "like");
        KEYWORDS.put("StartingWith", "like");
        KEYWORDS.put("EndingWith", "like");
        KEYWORDS.put("IsNotLike", "not like");
        KEYWORDS.put("Containing", "like");
        KEYWORDS.put("NotContaining", "not like");
        KEYWORDS.put("Not", "!=");
        KEYWORDS.put("IsTrue", "is true");
        KEYWORDS.put("True", "is true");
        KEYWORDS.put("IsFalse", "is false");
        KEYWORDS.put("False", "is false");
    }

    public static Sort sortJson(Sort sort, String prefix) {
        if (sort == null || sort.isEmpty()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = Lists.newArrayList();
        for (Sort.Order order : sort) {
            String[] keys = StringUtils.delimitedListToStringArray(order.getProperty(), ".");
            if (keys.length > 1) {
                int lastIndex = keys.length - 1;
                var sortReplaceArray = Arrays.copyOfRange(keys, 1, lastIndex);
                String sortedProperty = keys[0];
                if (StringUtils.hasLength(prefix)) {
                    sortedProperty = prefix + "." + sortedProperty;
                }
                String sortReplace = sortedProperty + appendIntermediateKeys(sortReplaceArray).append("->>'")
                        .append(keys[lastIndex]).append("'");
                orders.add(Sort.Order.by(sortReplace).with(order.getDirection()));
            } else {
                orders.add(order);
            }
        }
        return Sort.by(orders);
    }

    public static ParamSql queryJson(Map<String, Object> params, String prefix) {
        if (ObjectUtils.isEmpty(params)) {
            return ParamSql.of(new StringJoiner(" and "), Maps.newHashMap());
        }
        Map<String, Object> bindParams = Maps.newHashMap();
        StringJoiner whereSql = new StringJoiner(" and ");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String[] keys = StringUtils.delimitedListToStringArray(entry.getKey(), ".");
            Map.Entry<String, List<String>> exps = jsonPathKeyAndParamName(keys, prefix);
            whereSql.add(exps.getKey());
            if (exps.getValue().size() > 1) {
                String[] values = StringUtils.commaDelimitedListToStringArray(String.valueOf(entry.getValue()));
                bindParams.put(exps.getValue().get(0), values[0]);
                bindParams.put(exps.getValue().get(1), values[1]);
            } else {
                bindParams.put(exps.getValue().getFirst(), entry.getValue());
            }
        }
        return ParamSql.of(whereSql, bindParams);
    }

    private static Map.Entry<String, List<String>> jsonPathKeyAndParamName(String[] keys, String prefix) {
        int lastIndex = keys.length - 1;
        String lastKey = keys[lastIndex];
        Map.Entry<String, String> exps = findKeyWord(lastKey);
        String key = lastKey.substring(0, lastKey.length() - exps.getKey().length());

        String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, keys[0]);
        if (StringUtils.hasLength(prefix)) {
            column = prefix + "." + keys[0];
        }
        StringBuilder jsonPath = new StringBuilder("(" + column);
        String[] joinKeys = Arrays.copyOfRange(keys, 1, lastIndex);
        jsonPath.append(appendIntermediateKeys(joinKeys));

        List<String> paramNames = new ArrayList<>();
        String paramName = StringUtils.arrayToDelimitedString(keys, "_");
        if (!ObjectUtils.isEmpty(exps)) {
            if ("Between".equals(exps.getKey()) || "NotBetween".equals(exps.getKey())) {
                jsonPath.append("->>'").append(key).append("' ");
                String startKey = paramName + "_start";
                String endKey = paramName + "_end";
                jsonPath.append(exps.getValue()).append(" :").append(startKey).append(" and :").append(endKey);
                paramNames.add(startKey);
                paramNames.add(endKey);
            } else {
                jsonPath.append("->>'").append(key).append("' ");
                jsonPath.append(exps.getValue()).append(" :").append(paramName);
                paramNames.add(paramName);
            }
        } else {
            jsonPath.append("=").append(" :").append(paramName);
            paramNames.add(paramName);
        }
        return Map.entry(jsonPath.append(")").toString(), paramNames);
    }

    private static StringBuilder appendIntermediateKeys(String[] joinKeys) {
        StringBuilder jsonPath = new StringBuilder();
        for (String path : joinKeys) {
            jsonPath.append("->'").append(path).append("'");
        }
        return jsonPath;
    }

    private static Map.Entry<String, String> findKeyWord(String inputStr) {
        return KEYWORDS.entrySet().stream()
                .filter(entry -> StringUtils.endsWithIgnoreCase(inputStr, entry.getKey()))
                .max((entry1, entry2) -> {
                    int entry1Length = entry1.getKey().length();
                    int entry2Length = entry2.getKey().length();
                    return Integer.compare(entry1Length, entry2Length);
                }).orElseThrow(() -> RestServerException.withMsg("Not support key words!",
                        "Not support key words: " + inputStr));
    }
}