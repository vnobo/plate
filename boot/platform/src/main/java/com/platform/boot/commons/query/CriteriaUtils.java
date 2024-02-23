package com.platform.boot.commons.query;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import com.platform.boot.commons.utils.BeanUtils;
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

    public static String applyPage(Pageable pageable) {
        return applyPage(pageable, null);
    }

    public static String applyPage(Pageable pageable, String prefix) {
        String orderSql = applySort(pageable.getSort(), prefix);
        return String.format(orderSql + " limit %d offset %d", pageable.getPageSize(), pageable.getOffset());
    }

    public static String applySort(Sort sort, String prefix) {
        if (sort == null || sort.isUnsorted()) {
            return " order by id ";
        }
        sort = QueryJson.sortJson(sort, prefix);
        StringJoiner sortSql = new StringJoiner(", ");
        for (Sort.Order order : sort) {
            String sortedPropertyName = order.getProperty();
            sortedPropertyName = sortedPropertyName.contains("->>") ? sortedPropertyName :
                    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, sortedPropertyName);
            String sortedProperty = order.isIgnoreCase() ? "lower(" + sortedPropertyName + ")" : sortedPropertyName;
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            sortSql.add(sortedProperty + (order.isAscending() ? " asc" : " desc"));
        }

        return " order by " + sortSql;
    }

    @SuppressWarnings("unchecked")
    public static ParamSql buildParamSql(Object object, Collection<String> skipKeys, String prefix) {

        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        if (ObjectUtils.isEmpty(objectMap)) {
            return ParamSql.of(new StringJoiner(" AND "), Maps.newHashMap());
        }
        ParamSql jsonParamSql = QueryJson.queryJson((Map<String, Object>) objectMap.get("query"), prefix);
        Map<String, Object> params = jsonParamSql.params();
        StringJoiner sql = jsonParamSql.sql();

        if (!skipKeys.contains("securityCode") && !ObjectUtils.isEmpty(objectMap.get("securityCode"))) {
            String key = "tenant_code";
            if (StringUtils.hasLength(prefix)) {
                key = prefix + "." + key;
            }
            sql.add(key + " like :securityCode");
            params.put("securityCode", objectMap.get("securityCode"));
        }

        Set<String> removeKeys = new HashSet<>(SKIP_CRITERIA_KEYS);
        removeKeys.add("query");
        removeKeys.add("securityCode");
        if (!ObjectUtils.isEmpty(skipKeys)) {
            removeKeys.addAll(skipKeys);
        }

        objectMap = Maps.filterKeys(objectMap, key -> !removeKeys.contains(key));
        ParamSql entityParamSql = buildParamSql(objectMap, prefix);
        params.putAll(entityParamSql.params());
        sql.merge(entityParamSql.sql());
        return ParamSql.of(sql, params);
    }

    public static ParamSql buildParamSql(Map<String, Object> objectMap, String prefix) {
        StringJoiner whereSql = new StringJoiner(" and ");
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String column = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            if (StringUtils.hasLength(prefix)) {
                column = prefix + "." + column;
            }

            Object value = entry.getValue();
            String paramName = ":" + entry.getKey();

            if (value instanceof String) {
                whereSql.add(column + " like " + paramName);
            } else if (value instanceof Collection<?>) {
                whereSql.add(column + " in :" + paramName);
            } else {
                whereSql.add(column + " = " + paramName);
            }
        }
        return ParamSql.of(whereSql, objectMap);
    }

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
}