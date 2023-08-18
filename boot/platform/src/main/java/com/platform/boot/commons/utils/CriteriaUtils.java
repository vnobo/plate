package com.platform.boot.commons.utils;

import com.google.common.base.CaseFormat;
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

    /**
     * Method to construct page query by pageable
     *
     * @param pageable pageable to be used as query
     * @return page query based on pageable
     */
    public static String applyPage(Pageable pageable) {
        String orderSql = applySort(pageable.getSort(), null);
        return String.format(orderSql + " LIMIT %d OFFSET %d", pageable.getPageSize(), pageable.getOffset());
    }

    public static String applyPage(Pageable pageable, String prefix) {
        String orderSql = applySort(pageable.getSort(), prefix);
        return String.format(orderSql + " LIMIT %d OFFSET %d", pageable.getPageSize(), pageable.getOffset());
    }

    /**
     * Generates an ORDER BY clause for the given {@link Sort} object,
     * taking into account whether the sorting order is ascending or descending,
     * and whether the sorting has to be case-sensitive or not.
     *
     * @param sort The Sort object used to generate the ORDER By clause.
     * @return An ORDER BY clause string.
     */
    public static String applySort(Sort sort, String prefix) {
        if (sort == null || sort.isUnsorted()) {
            return "";
        }
        StringJoiner sortSql = new StringJoiner(" , ");
        sort.iterator().forEachRemaining((o) -> {
            String sortedPropertyName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, o.getProperty());
            String sortedProperty = o.isIgnoreCase() ? "LOWER(" + sortedPropertyName + ")" : sortedPropertyName;
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            sortSql.add(sortedProperty + (o.isAscending() ? " ASC" : " DESC"));
        });
        return " ORDER BY " + sortSql;
    }

    /**
     * 使用 applyWhere 方法将一个对象转化成查询条件 where 语句
     *
     * @param object   待转化的对象
     * @param skipKeys 跳过的字段名列表
     * @return 返回 where 语句字符串
     */
    public static Parameter whereParameterSql(Object object, List<String> skipKeys) {
        return whereParameterSql(object, skipKeys, null);
    }

    /**
     * 使用 applyWhere 方法将一个对象转化成查询条件 where 语句
     *
     * @param object   待转化的对象
     * @param skipKeys 跳过的字段名列表
     * @param prefix   拼接条件前缀
     * @return 返回 where 语句字符串
     */
    public static Parameter whereParameterSql(Object object, List<String> skipKeys, String prefix) {
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        Set<String> mergeSet = new HashSet<>(SKIP_CRITERIA_KEYS);
        if (!ObjectUtils.isEmpty(skipKeys)) {
            mergeSet.addAll(skipKeys);
        }
        mergeSet.forEach(objectMap::remove);
        return whereParameterSql(objectMap, prefix);
    }

    public static Parameter whereParameterSql(Map<String, Object> objectMap, String prefix) {
        StringBuilder whereSql = new StringBuilder();
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, entry.getKey());
            if (StringUtils.hasLength(prefix)) {
                key = prefix + "." + key;
            }
            if (entry.getValue() instanceof String) {
                whereSql.append(" AND ").append(key).append(" LIKE ").append(":").append(entry.getKey());
            } else if (entry.getValue() instanceof Collection<?>) {
                whereSql.append(" AND ").append(key).append(" IN (:").append(entry.getKey()).append(")");
            } else {
                whereSql.append(" AND ").append(key).append(" = :").append(entry.getKey());
            }
        }
        if (!whereSql.isEmpty()) {
            whereSql.insert(0, " WHERE 1=1 ");
        }
        return Parameter.of(whereSql.toString(), objectMap);
    }

    /**
     * Builds a Criteria object from the given object excluding the given keys.
     * The static skip keys such as {@link CriteriaUtils} are also excluded.
     *
     * @param object  the object from which to build the Criteria
     * @param skipKes the keys to skip
     * @return the built Criteria
     */
    public static Criteria build(Object object, Set<String> skipKes) {
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