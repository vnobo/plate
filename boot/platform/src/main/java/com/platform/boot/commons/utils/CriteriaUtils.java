package com.platform.boot.commons.utils;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public final class CriteriaUtils {
    public static final Set<String> SKIP_CRITERIA_KEYS = Set.of("extend", "createdTime", "updatedTime");

    public static Map<StringJoiner, Map<String, Object>> queryJson(Map<String, Object> params) {
        if (ObjectUtils.isEmpty(params)) {
            return Map.of(new StringJoiner(" and "), Maps.newHashMap());
        }
        return Map.of(queryJsonSql(params), queryJsonParams(params));
    }

    public static StringJoiner queryJsonSql(Map<String, Object> params) {
        StringJoiner whereSql = new StringJoiner(" and ");
        if (ObjectUtils.isEmpty(params)) {
            return whereSql;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StringUtils.startsWithIgnoreCase(key, "query.")) {
                key = key.replace("query.", "");
                String replaceStr = StringUtils.replace(key, ".", "_");
                String bindKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, replaceStr);
                if (!ObjectUtils.isEmpty(value)) {
                    whereSql.add("JSON_VALUE(data,'$." + key + "') = :" + bindKey);
                }
            } else if (StringUtils.startsWithIgnoreCase(key, "like.")) {
                key = key.replace("like.", "");
                String replaceStr = StringUtils.replace(key, ".", "_");
                String bindKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, replaceStr);
                if (!ObjectUtils.isEmpty(value)) {
                    whereSql.add("JSON_VALUE(data,'$." + key + "') like :" + bindKey);
                }


            }
        }
        return whereSql;
    }

    public static Map<String, Object> queryJsonParams(Map<String, Object> params) {
        Map<String, Object> whereParams = Maps.newHashMap();
        if (ObjectUtils.isEmpty(params)) {
            return whereParams;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StringUtils.startsWithIgnoreCase(key, "query.")) {
                key = key.replace("query.", "");
                String replaceStr = StringUtils.replace(key, ".", "_");
                String bindKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, replaceStr);
                if (!ObjectUtils.isEmpty(value)) {
                    whereParams.put(bindKey, value);
                }
            } else if (StringUtils.startsWithIgnoreCase(key, "like.")) {
                key = key.replace("like.", "");
                String replaceStr = StringUtils.replace(key, ".", "_");
                String bindKey = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, replaceStr);
                if (!ObjectUtils.isEmpty(value)) {
                    whereParams.put(bindKey, value);
                }
            }
        }
        return whereParams;
    }

    public static String applyPage(Pageable pageable) {
        String orderSql = applySort(pageable.getSort(), null);
        return String.format(orderSql + " limit %d offset %d", pageable.getPageSize(), pageable.getOffset());
    }

    public static String applyPage(Pageable pageable, String prefix) {
        String orderSql = applySort(pageable.getSort(), prefix);
        return String.format(orderSql + " limit %d offset %d", pageable.getPageSize(), pageable.getOffset());
    }

    public static String applySort(Sort sort, String prefix) {
        if (sort == null || sort.isUnsorted()) {
            return "";
        }
        StringJoiner sortSql = new StringJoiner(" , ");
        sort.iterator().forEachRemaining((o) -> {
            String sortedPropertyName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, o.getProperty());
            String sortedProperty = o.isIgnoreCase() ? "lower(" + sortedPropertyName + ")" : sortedPropertyName;
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            sortSql.add(sortedProperty + (o.isAscending() ? " asc" : " desc"));
        });
        return " order by " + sortSql;
    }

    public static String whereSql(Object object, Collection<String> skipKeys, String prefix) {

        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        if (ObjectUtils.isEmpty(objectMap)) {
            return "";
        }

        Set<String> removeKeys = new HashSet<>(SKIP_CRITERIA_KEYS);
        if (!ObjectUtils.isEmpty(skipKeys)) {
            removeKeys.addAll(skipKeys);
        }

        objectMap = Maps.filterKeys(objectMap, key -> !removeKeys.contains(key));
        return whereSql(objectMap, prefix);
    }

    public static String whereSql(Map<String, Object> objectMap, String prefix) {

        if (ObjectUtils.isEmpty(objectMap)) {
            return "";
        }

        StringJoiner whereSql = new StringJoiner(" and ");
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            if (StringUtils.hasLength(prefix)) {
                key = prefix + "." + key;
            }
            if (entry.getValue() instanceof String) {
                whereSql.add(key + " like :" + entry.getKey());
            } else if (entry.getValue() instanceof Collection<?>) {
                whereSql.add(key + " in (:" + entry.getKey() + ")");
            } else {
                whereSql.add(key + " = :" + entry.getKey());
            }
        }

        return "Where " + whereSql;
    }

    /**
     * Builds a Criteria object from the given object excluding the given keys.
     * The static skip keys such as {@link CriteriaUtils} are also excluded.
     *
     * @param object  the object from which to build the Criteria
     * @param skipKes the keys to skip
     * @return the built Criteria
     */
    public static Criteria build(Object object, Collection<String> skipKes) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        if (!ObjectUtils.isEmpty(objectMap)) {
            Set<String> mergeSet = new HashSet<>(SKIP_CRITERIA_KEYS);
            if (!ObjectUtils.isEmpty(skipKes)) {
                mergeSet.addAll(skipKes);
            }
            mergeSet.forEach(objectMap::remove);
        }
        return build(objectMap);
    }

    /**
     * A utility class to build a {@link Criteria} from a {@link Map} of key value pairs.
     *
     * <p>The keys are used as the field names and the values can be either {@link String}, {@link Collection},
     * or any other object type. For a string value, the criteria performs a LIKE operation
     * with a wildcard added to the end of the string. For a collection type, the criteria performs
     * an IN operation. For all other object types, the criteria performs an IS operation.
     * </p>
     *
     * @param objectMap The {@link Map} of key value pairs
     * @return A {@link Criteria} built from the input.
     */
    public static Criteria build(Map<String, Object> objectMap) {
        if (ObjectUtils.isEmpty(objectMap)) {
            return Criteria.empty();
        }
        List<Criteria> criteriaList = objectMap.entrySet().parallelStream().map(entry -> {
            if (entry.getValue() instanceof String value) {
                return Criteria.where(entry.getKey()).like(String.format("%s", value) + "%").ignoreCase(true);
            } else if (entry.getValue() instanceof Collection<?> values) {
                return Criteria.where(entry.getKey()).in(values);
            } else {
                return Criteria.where(entry.getKey()).is(entry.getValue());
            }
        }).collect(Collectors.toList());
        return Criteria.from(criteriaList);
    }

    @Data(staticConstructor = "of")
    public static class Parameter {
        private final String sql;
        private final Map<String, Object> params;
    }
}