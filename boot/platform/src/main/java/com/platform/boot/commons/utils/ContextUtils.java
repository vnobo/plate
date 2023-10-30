package com.platform.boot.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.boot.commons.Snowflake;
import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.security.SecurityDetails;
import com.platform.boot.security.UserAuditor;
import com.platform.boot.security.user.UsersService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Log4j2
@Component
public final class ContextUtils implements Serializable {
    public final static String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";
    public final static String SECURITY_AUTH_TOKEN_HEADER = "X-Auth-Token";
    public static ObjectMapper OBJECT_MAPPER;
    public static Snowflake SNOW_FLAKE;
    public static UsersService USERS_SERVICE;

    ContextUtils(ObjectMapper objectMapper, UsersService usersService) {
        ContextUtils.SNOW_FLAKE = new Snowflake(1, 1);
        ContextUtils.OBJECT_MAPPER = objectMapper;
        ContextUtils.USERS_SERVICE = usersService;
    }

    public static StringJoiner cacheKey(Object... objects) {
        // Convert objects to a map using ObjectMapper
        StringJoiner keyBuilder = new StringJoiner("&");
        for (Object object : objects) {
            if (object instanceof Pageable pageable) {
                keyBuilder.merge(applySort(pageable.getSort()));
                keyBuilder.add("page=" + pageable.getPageNumber());
                keyBuilder.add("size=" + pageable.getPageSize());
                keyBuilder.add("offset=" + pageable.getOffset());
                continue;
            }
            // Convert object to a map using ObjectMapper
            Map<String, Object> objectMap = com.platform.boot.commons.utils.BeanUtils
                    .beanToMap(object, true);
            // Check if the object map is empty
            if (ObjectUtils.isEmpty(objectMap)) {
                // Append the class name of the object to the key builder
                keyBuilder.add(object.getClass().getName());
                continue;
            }
            // Append each key-value pair from the object map to the key builder
            objectMap.forEach((k, v) -> keyBuilder.add(k + "=" + v));
        }
        // Return the final cache key as a string
        return keyBuilder;
    }

    private static StringJoiner applySort(Sort sort) {
        StringJoiner sortKey = new StringJoiner("&");
        if (sort == null || sort.isUnsorted()) {
            return sortKey;
        }
        for (Sort.Order order : sort) {
            String sortedPropertyName = order.getProperty();
            String sortedProperty = order.isIgnoreCase() ? "lower(" + sortedPropertyName + ")" : sortedPropertyName;
            sortKey.add(sortedProperty + "=" + (order.isAscending() ? "asc" : "desc"));
        }
        return sortKey;
    }

    public static Mono<SecurityDetails> securityDetails() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getPrincipal())
                .cast(SecurityDetails.class);
    }

    public static <T> Mono<T> userAuditorSerializable(T obejct) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(obejct.getClass());
        var setPropertyStream = Arrays.stream(propertyDescriptors)
                .filter(propertyDescriptor -> propertyDescriptor.getPropertyType() == UserAuditor.class);
        var propertyFlux = Flux.fromStream(setPropertyStream).flatMap(propertyDescriptor -> {
            try {
                UserAuditor userAuditor = (UserAuditor) propertyDescriptor.getReadMethod().invoke(obejct);
                if (ObjectUtils.isEmpty(userAuditor)) {
                    return Mono.just(obejct);
                }
                return USERS_SERVICE.loadByCode(userAuditor.code()).flatMap(user -> {
                    try {
                        propertyDescriptor.getWriteMethod().invoke(obejct, UserAuditor.withUser(user));
                        return Mono.just(obejct);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return Mono.error(RestServerException.withMsg(
                                "User auditor serialization getWriteMethod invoke error!", e));
                    }
                });
            } catch (IllegalAccessException | InvocationTargetException e) {
                return Mono.error(RestServerException.withMsg(
                        "User auditor serialization getReadMethod invoke error!", e));
            }
        });
        return propertyFlux.then(Mono.just(obejct));
    }

    public static String nextId() {
        if (ObjectUtils.isEmpty(SNOW_FLAKE)) {
            throw RestServerException.withMsg(
                    "Snowflake not found!", "Snowflake server is not found, init snowflake first.");
        }
        return SNOW_FLAKE.nextIdStr();
    }
}