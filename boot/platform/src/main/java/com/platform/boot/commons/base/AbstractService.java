package com.platform.boot.commons.base;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 */
public abstract class AbstractService implements InitializingBean {

    protected final Log log = LogFactory.getLog(AbstractService.class);

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