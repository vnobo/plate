package com.plate.boot.commons.base;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


/**
 * AbstractDatabase is an abstract class that serves as a foundation for database interactions
 * in a reactive application context. It encapsulates the common functionality required for
 * executing database queries, managing transactions, and handling entity mappings, providing
 * a robust and flexible framework for database operations.
 *
 * <p>This class is designed to work with R2DBC (Reactive Relational Database Connectivity),
 * a non-blocking, asynchronous API for database access in reactive applications. It leverages
 * the R2dbcEntityTemplate for executing reactive database operations, the DatabaseClient for
 * direct database interactions, and the R2dbcConverter for converting between R2DBC data types
 * and domain-specific objects.
 *
 * <p>AbstractDatabase provides methods for executing queries with caching, counting entities
 * with caching, and setting up the R2dbcEntityTemplate. It also overrides the afterPropertiesSet
 * method to perform initialization tasks, such as setting up the databaseClient and r2dbcConverter.
 *
 * <p>Subclasses of AbstractDatabase can extend this functionality to provide specific database
 * interactions tailored to their application's needs.
 */
@Log4j2
public abstract class AbstractCache implements InitializingBean {
    /**
     * Cache instance used to store and retrieve data within the service.
     * Initialized through the  method with a cache name defaulting to the class name concatenated with ".cache".
     * This cache is cleared upon initialization and is essential for providing temporary storage that accelerates data access.
     */
    protected Cache cache;

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied all dependencies for this bean. This method allows the bean instance
     * to perform initialization only possible when all bean properties have been set
     * and to throw an exception in the event of misconfiguration.
     *
     * <p>This implementation initializes the cache associated with this component,
     * using the bean's class name concatenated with ".cache" as the cache identifier.
     */
    @Override
    public void afterPropertiesSet() {
        this.cache = initializingCache(this.getClass().getName().concat(".cache"));
    }

    /**
     * Initializes the cache with the specified name.
     * If the cache manager is set, it attempts to retrieve the cache from the manager;
     * otherwise, it creates a new ConcurrentMapCache with the given name.
     * After initialization, the cache is cleared and a debug log message is generated indicating
     * the cache's class name and name.
     *
     * @param cacheName The name of the cache to initialize.
     */
    public Cache initializingCache(String cacheName) {
        var cache = Optional.ofNullable(ContextUtils.CACHE_MANAGER).map(manager -> manager.getCache(cacheName))
                .orElse(new ConcurrentMapCache(cacheName));
        cache.clear();
        log.debug("Initializing provider [{}] cache names: {}",
                cache.getNativeCache().getClass().getSimpleName(), cache.getName());
        return cache;
    }

    /**
     * Executes a database from with caching functionality.
     * It takes a cache key, a from object, and an entity class type to perform the operation.
     * The results are cached for future queries with the same key.
     *
     * @param <T>         The type of entities expected as from results.
     * @param key         The unique identifier used as a cache key. Determines the cache entry for storing/retrieving results.
     * @param query       The form object defining the SQL from and its potential parameters.
     * @param entityClass The class of the entity that each row in the result set will be mapped to.
     * @return A {@link Flux} emitting the form results, potentially from cache if previously stored.
     */
    protected <T> Flux<T> queryWithCache(Object key, Query query, Class<T> entityClass) {
        return queryWithCache(key, DatabaseUtils.query(query, entityClass))
                .timeout(Duration.ofSeconds(10)).replay(3).refCount();
    }

    /**
     * Executes a SQL from with caching capability. It binds provided parameters to the SQL from,
     * maps the result set to entities of the specified class, applies user auditor serialization,
     * and utilizes a cache to store from results for subsequent identical queries.
     *
     * @param <T>         The type of entities the SQL from results will be mapped to.
     * @param key         The cache key used to identify the cached data. This should uniquely represent
     *                    the form and its parameters.
     * @param sql         The SQL from string to be executed.
     * @param bindParams  A map containing named parameter bindings for the SQL from.
     * @param entityClass The class of the entity that each row in the result set will be converted into.
     * @return A {@link Flux} emitting the entities resulting from the from, potentially from the cache.
     */
    protected <T> Flux<T> queryWithCache(Object key, String sql,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        return queryWithCache(key, DatabaseUtils.query(sql, bindParams, entityClass))
                .timeout(Duration.ofSeconds(10)).replay(3).refCount();
    }

    /**
     * Executes a Flux-based database from with caching functionality.
     * It enhances the source Flux by caching its emissions under a specified key.
     * If the cache contains data for the key, it returns the cached data immediately.
     * Otherwise, it executes the source Flux, caches the results, and then emits them.
     *
     * @param <T>        The type of elements emitted by the Flux.
     * @param key        The unique identifier used as the cache key.
     * @param sourceFlux The original Flux to be executed for querying data.
     * @return A Flux that emits the cached data if available, otherwise executes the source Flux and caches the result.
     */
    protected <T> Flux<T> queryWithCache(Object key, Flux<T> sourceFlux) {
        String cacheKey = key + ":data";
        Collection<T> cacheData = this.cache.get(cacheKey, () -> null);
        if (ObjectUtils.isEmpty(cacheData)) {
            var sourceData = new ArrayList<T>();
            return sourceFlux.doOnNext(sourceData::add)
                    .doAfterTerminate(() -> this.cachePut(cacheKey, sourceData));
        }
        return Flux.fromIterable(cacheData);
    }

    /**
     * Counts entities with caching support based on the provided key, from, and entity class.
     * This method enhances entity counting by storing the count result in a cache,
     * allowing subsequent calls with the same key to retrieve the count directly from the cache
     * rather than executing the form again.
     *
     * @param <T>         The type of entities for which the count is to be performed.
     * @param key         A unique identifier used as a cache key. Determines the cached count's retrieval.
     * @param query       The form object defining the criteria for counting entities.
     * @param entityClass The class of the entities being counted.
     * @return A {@link Mono} emitting the count of entities as a {@link Long}, potentially from cache.
     */
    protected <T> Mono<Long> countWithCache(Object key, Query query, Class<T> entityClass) {
        return countWithCache(key, DatabaseUtils.count(query, entityClass)).timeout(Duration.ofSeconds(10));
    }

    /**
     * Executes a SQL count from with caching capabilities. It prepares the SQL from with provided bind parameters,
     * creates a Mono source to fetch the count, and then delegates to another method to handle caching logic.
     *
     * @param key        The unique cache key associated with the count from. Used for caching and retrieving results.
     * @param sql        The SQL count from string to be executed.
     * @param bindParams A map containing named parameter placeholders and their respective values for the SQL from.
     * @return A Mono emitting the count result, potentially fetched from cache or computed from the database.
     */
    protected Mono<Long> countWithCache(Object key, String sql, Map<String, Object> bindParams) {
        return countWithCache(key, DatabaseUtils.count(sql, bindParams)).timeout(Duration.ofSeconds(10));
    }

    /**
     * Counts entities using a cached value if present, otherwise executes the provided Mono source
     * and caches the result for future use. This method enhances performance by retrieving counts
     * from a cache, thereby reducing the need for repetitive count queries to the underlying database.
     *
     * @param key        The unique identifier used as the cache key. This key is instrumental in
     *                   both retrieving and storing count values within the cache.
     * @param sourceMono A Mono publisher that, when subscribed to, executes a count operation.
     *                   If no cached value is found for the specified key, this Mono will be
     *                   subscribed to retrieve the count, which is subsequently cached.
     * @return A Mono emitting the count of entities. If a cached value is present, it is emitted
     * immediately. Otherwise, the Mono returned by `sourceMono` is executed, its emission is
     * cached, and then emitted.
     */
    protected Mono<Long> countWithCache(Object key, Mono<Long> sourceMono) {
        String cacheKey = key + ":count";
        Long cacheCount = this.cache.get(cacheKey, () -> null);
        Mono<Long> source = sourceMono.doOnNext(count -> this.cachePut(cacheKey, count));
        return Mono.justOrEmpty(cacheCount).switchIfEmpty(Mono.defer(() -> source));
    }

    /**
     * Inserts an object into the specified cache if its size does not exceed the maximum allowed in-memory size.
     *
     * @param cacheKey The key under which the object will be stored in the cache.
     * @param obj      The object to be cached. Its size will be evaluated against the maximum allowed size.
     *                 If the object exceeds the limit, it will not be cached and a warning will be logged.
     */
    protected void cachePut(String cacheKey, Object obj) {
        DataSize objectSize = DatabaseUtils.getBeanSize(obj);
        if (objectSize.toBytes() > DatabaseUtils.MAX_IN_MEMORY_SIZE.toBytes()) {
            log.warn("Object size is too large, Max memory size is {}, Object size is {}.",
                    DatabaseUtils.MAX_IN_MEMORY_SIZE, objectSize);
            return;
        }
        this.cache.put(cacheKey, obj);
    }
}