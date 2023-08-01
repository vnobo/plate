package com.platform.boot.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.boot.commons.Snowflake;
import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.security.SecurityDetails;
import com.platform.boot.security.UserAuditor;
import com.platform.boot.security.user.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Component
public final class ContextUtils implements Serializable {
    public static final String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";
    public static final String SECURITY_AUTH_TOKEN_HEADER = "X-Auth-Token";
    public static ObjectMapper OBJECT_MAPPER;
    public static Snowflake SNOW_FLAKE;
    public static CacheManager CACHE_MANAGER;
    public static UsersService USERS_SERVICE;

    ContextUtils(ObjectMapper objectMapper, CacheManager cacheManager, UsersService usersService) {
        ContextUtils.SNOW_FLAKE = new Snowflake(1, 1);
        ContextUtils.OBJECT_MAPPER = objectMapper;
        ContextUtils.CACHE_MANAGER = cacheManager;
        ContextUtils.USERS_SERVICE = usersService;
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
                return USERS_SERVICE.loadByUsername(userAuditor.getUsername()).flatMap(user -> {
                    try {
                        propertyDescriptor.getWriteMethod().invoke(obejct, userAuditor.withUser(user));
                        return Mono.just(obejct);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        return Mono.error(RestServerException.withMsg(e.getMessage()));
                    }
                });
            } catch (IllegalAccessException | InvocationTargetException e) {
                return Mono.error(RestServerException.withMsg(e.getMessage()));
            }
        });
        return propertyFlux.then(Mono.just(obejct));
    }

    public static String nextId() {
        if (ObjectUtils.isEmpty(SNOW_FLAKE)) {
            throw RestServerException.withMsg("Snowflake 未初始化!请先初始化");
        }
        return SNOW_FLAKE.nextIdStr();
    }
}