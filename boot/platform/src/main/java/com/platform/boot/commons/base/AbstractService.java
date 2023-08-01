package com.platform.boot.commons.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Optional;

/**
 * This is an abstract class that provides a basic implementation of InitializingBean.
 * It also provides a method for initializing cache.
 * <p>
 * The cache is initialized using the cacheManager and cacheName provided.
 * If the cacheManager is null, a new ConcurrentMapCache is created.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 * @see InitializingBean
 * @see Cache
 * @see CacheManager
 * @see ConcurrentMapCache
 * @see Optional
 * @see ObjectMapper
 * @see Log4j2
 * @since 1.0.0
 */
public abstract class AbstractService implements InitializingBean {

    protected final Log log = LogFactory.getLog(AbstractService.class);

    protected CacheManager cacheManager;
    protected Cache cache;
    protected ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    public void setObjectMapper(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Initializes the cache using the cacheManager and cacheName provided.
     * If the cacheManager is null, a new ConcurrentMapCache is created.
     *
     * @param cacheName the name of the cache to be initialized
     */
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