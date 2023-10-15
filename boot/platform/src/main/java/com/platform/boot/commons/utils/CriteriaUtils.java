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

    public static String applyPage(Pageable pageable) {
        String orderSql = applySort(pageable.getSort(), null);
        return String.format(orderSql + " limit %d offset %d", pageable.getPageSize(), pageable.getOffset());
    }

    public static String applyPage(Pageable pageable, String prefix) {
        //Apply the sort to the pageable object
        String orderSql = applySort(pageable.getSort(), prefix);
        //Format the orderSql string with the pageSize and offset from the pageable object
        return String.format(orderSql + " limit %d offset %d", pageable.getPageSize(), pageable.getOffset());
    }

    public static String applySort(Sort sort, String prefix) {
        // Check if the sort is null or unsorted
        if (sort == null || sort.isUnsorted()) {
            return "";
        }
        // Create a StringJoiner to store the SQL for the sort
        StringJoiner sortSql = new StringJoiner(" , ");
        // Iterate through the sort and add the sorted property name and order to the StringJoiner
        sort.iterator().forEachRemaining((o) -> {
            String sortedPropertyName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, o.getProperty());
            // Check if the property should be ignored
            String sortedProperty = o.isIgnoreCase() ? "lower(" + sortedPropertyName + ")" : sortedPropertyName;
            // Check if a prefix is provided
            if (StringUtils.hasLength(prefix)) {
                sortedProperty = prefix + "." + sortedProperty;
            }
            // Add the sorted property and order to the StringJoiner
            sortSql.add(sortedProperty + (o.isAscending() ? " asc" : " desc"));
        });
        // Return the SQL for the sort
        return " order by " + sortSql;
    }

    public static String whereSql(Object object, Collection<String> skipKeys, String prefix) {

        //Create a map of the object's properties
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, false, true);
        //If the objectMap is empty, return an empty string
        if (ObjectUtils.isEmpty(objectMap)) {
            return "";
        }

        //Create a set of the keys to be removed
        Set<String> removeKeys = new HashSet<>(SKIP_CRITERIA_KEYS);
        //If the skipKeys is not empty, add it to the set
        if (!ObjectUtils.isEmpty(skipKeys)) {
            removeKeys.addAll(skipKeys);
        }

        //Filter the objectMap, removing any keys that are in the set
        objectMap = Maps.filterKeys(objectMap, key -> !removeKeys.contains(key));
        //Return the whereSql with the filtered objectMap
        return whereSql(objectMap, prefix);
    }

    public static String whereSql(Map<String, Object> objectMap, String prefix) {

        // Check if the objectMap is empty
        if (ObjectUtils.isEmpty(objectMap)) {
            return "";
        }

        // Create a StringJoiner object to store the SQL
        StringJoiner whereSql = new StringJoiner(" and ");
        // Loop through the objectMap
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            // Convert the key to lower camel case
            String key = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, entry.getKey());
            // Check if the prefix is not empty
            if (StringUtils.hasLength(prefix)) {
                // Add the prefix to the key
                key = prefix + "." + key;
            }
            // Check if the value is a String
            if (entry.getValue() instanceof String) {
                // Add the key and value to the StringJoiner
                whereSql.add(key + " like :" + entry.getKey());
                // Check if the value is a Collection
            } else if (entry.getValue() instanceof Collection<?>) {
                // Add the key and value to the StringJoiner
                whereSql.add(key + " in (:" + entry.getKey() + ")");
                // Otherwise
            } else {
                // Add the key and value to the StringJoiner
                whereSql.add(key + " = :" + entry.getKey());
            }
        }

        // Return the SQL
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
        //Create a map of the object's properties
        Map<String, Object> objectMap = BeanUtils.beanToMap(object, true);
        //If the map is not empty
        if (!ObjectUtils.isEmpty(objectMap)) {
            //Create a set of the criteria keys to skip
            Set<String> mergeSet = new HashSet<>(SKIP_CRITERIA_KEYS);
            //If the skipKes parameter is not empty
            if (!ObjectUtils.isEmpty(skipKes)) {
                //Add the skipKes to the set
                mergeSet.addAll(skipKes);
            }
            //Remove the criteria keys from the map
            mergeSet.forEach(objectMap::remove);
        }
        //Return the criteria built from the map
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
        if (objectMap == null || objectMap.isEmpty()) {
            return Criteria.empty();
        }
        List<Criteria> criteriaList = objectMap.entrySet().stream().map(entry -> {
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