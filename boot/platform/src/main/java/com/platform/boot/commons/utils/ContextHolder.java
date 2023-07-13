package com.platform.boot.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.boot.commons.Snowflake;
import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.security.SecurityDetails;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.io.Serializable;


/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Component
public final class ContextHolder implements Serializable {
    public static final String CSRF_TOKEN_CONTEXT = "CSRF_TOKEN_CONTEXT";
    public static final String SECURITY_AUTH_TOKEN_HEADER = "X-Auth-Token";
    public static ObjectMapper OBJECT_MAPPER = null;
    public static Snowflake SNOW_FLAKE = null;
    public static CacheManager CACHE_MANAGER = null;

    ContextHolder(ObjectMapper objectMapper, CacheManager cacheManager) {
        ContextHolder.SNOW_FLAKE = new Snowflake(1, 1);
        ContextHolder.OBJECT_MAPPER = objectMapper;
        ContextHolder.CACHE_MANAGER = cacheManager;
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