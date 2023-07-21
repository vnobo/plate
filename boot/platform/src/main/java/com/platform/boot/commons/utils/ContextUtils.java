package com.platform.boot.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.boot.commons.Snowflake;
import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.security.SecurityDetails;
import org.springframework.cache.CacheManager;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.io.Serializable;


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
    public static R2dbcEntityTemplate ENTITY_TEMPLATE;

    ContextUtils(ObjectMapper objectMapper, CacheManager cacheManager, R2dbcEntityTemplate entityTemplate) {
        ContextUtils.SNOW_FLAKE = new Snowflake(1, 1);
        ContextUtils.OBJECT_MAPPER = objectMapper;
        ContextUtils.CACHE_MANAGER = cacheManager;
        ContextUtils.ENTITY_TEMPLATE = entityTemplate;
    }

    public static Mono<SecurityDetails> securityDetails() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getPrincipal())
                .cast(SecurityDetails.class);
    }

    public static String nextId() {
        if (ObjectUtils.isEmpty(SNOW_FLAKE)) {
            throw RestServerException.withMsg("Snowflake 未初始化!请先初始化");
        }
        return SNOW_FLAKE.nextIdStr();
    }
}