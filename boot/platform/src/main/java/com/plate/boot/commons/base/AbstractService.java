package com.plate.boot.commons.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Optional;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
public abstract class AbstractService implements InitializingBean {

    protected Cache cache;

    protected CacheManager cacheManager;

    protected ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected void initializingCache(String cacheName) {
        this.cache = Optional.ofNullable(this.cacheManager).map(manager -> manager.getCache(cacheName))
                .orElse(new ConcurrentMapCache(cacheName));
        this.cache.clear();
        log.debug("Initializing provider [%s] cache names: %s".formatted(
                this.cache.getNativeCache().getClass().getSimpleName(), this.cache.getName()));
    }

    @Override
    public void afterPropertiesSet() {
        initializingCache(this.getClass().getName().concat(".cache"));
    }
}