package com.plate.boot.commons.base;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * AbstractDatabase serves as a foundation for implementing database operations with caching capabilities.
 * It extends the AbstractService to inherit common service layer functionalities and integrates
 * R2DBC for reactive database access.
 * This class provides generic methods to execute queries and counts with caching support.
 * <p>
 * Key Features:
 * - Utilizes R2dbcEntityTemplate for entity-based operations.
 * - Leverages DatabaseClient for SQL execution.
 * - Implements caching mechanisms to enhance query performance.
 * - Supports reactive programming model using Flux and Mono.
 * <p>
 * Usage Note:
 * Subclasses should implement specific database interaction logic while leveraging
 * the provided caching wrappers around query and count operations.
 * <p>
 * Dependencies:
 * Requires an R2dbcEntityTemplate, DatabaseClient, and Cache instance to be functional,
 * typically wired through dependency injection.
 */
public abstract class AbstractDatabase extends AbstractService {

    /**
     * The R2dbcEntityTemplate instance used for executing reactive database operations.
     * This template facilitates interaction with the database, including query execution,
     * entity conversion, and transaction management specifically tailored for R2DBC (Reactive Relational Database Connectivity).
     */
    protected R2dbcEntityTemplate entityTemplate;

    /**
     * Represents a client for interacting with the database, facilitating operations such as
     * querying, updating, and managing data within the database. This field is initialized
     * and configured typically during the setup or initialization phase of the hosting class,
     * enabling seamless database access throughout the application.
     */
    protected DatabaseClient databaseClient;

    /**
     * The R2DBC Converter instance used for converting between R2DBC Data types and domain-specific objects.
     * This converter plays a crucial role in mapping query results to entity classes and vice versa,
     * facilitating seamless interaction with the R2DBC database through the R2dbcEntityTemplate.
     */
    protected R2dbcConverter r2dbcConverter;

    /**
     * Executes a database query with caching functionality.
     * It takes a cache key, a query object, and an entity class type to perform the operation.
     * The results are cached for future queries with the same key.
     *
     * @param <T>         The type of entities expected as query results.
     * @param key         The unique identifier used as a cache key. Determines the cache entry for storing/retrieving results.
     * @param query       The query object defining the SQL query and its potential parameters.
     * @param entityClass The class of the entity that each row in the result set will be mapped to.
     * @return A {@link Flux} emitting the query results, potentially from cache if previously stored.
     */
    protected <T> Flux<T> queryWithCache(Object key, Query query, Class<T> entityClass) {
        Flux<T> source = this.entityTemplate.select(query, entityClass);
        return queryWithCache(key, source).cache();
    }

    /**
     * Executes a SQL query with caching capability. It binds provided parameters to the SQL query,
     * maps the result set to entities of the specified class, applies user auditor serialization,
     * and utilizes a cache to store query results for subsequent identical queries.
     *
     * @param <T>         The type of entities the SQL query results will be mapped to.
     * @param key         The cache key used to identify the cached data. This should uniquely represent
     *                    the query and its parameters.
     * @param sql         The SQL query string to be executed.
     * @param bindParams  A map containing named parameter bindings for the SQL query.
     * @param entityClass The class of the entity that each row in the result set will be converted into.
     * @return A {@link Flux} emitting the entities resulting from the query, potentially from the cache.
     */
    protected <T> Flux<T> queryWithCache(Object key, String sql,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        var executeSpec = this.databaseClient.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> this.r2dbcConverter.read(entityClass, row, rowMetadata))
                .all().flatMapSequential(ContextUtils::serializeUserAuditor);
        return queryWithCache(key, source);
    }

    /**
     * Executes a Flux-based database query with caching functionality.
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
        Collection<T> cacheData = this.cache.get(cacheKey, ArrayList::new);
        assert cacheData != null;
        Flux<T> source = sourceFlux.doOnNext(cacheData::add)
                .doAfterTerminate(() -> BeanUtils.cachePut(this.cache, cacheKey, cacheData));
        return Flux.fromIterable(ObjectUtils.isEmpty(cacheData) ? Collections.emptyList() : cacheData)
                .switchIfEmpty(Flux.defer(() -> source));
    }

    /**
     * Counts entities with caching support based on the provided key, query, and entity class.
     * This method enhances entity counting by storing the count result in a cache,
     * allowing subsequent calls with the same key to retrieve the count directly from the cache
     * rather than executing the query again.
     *
     * @param <T>         The type of entities for which the count is to be performed.
     * @param key         A unique identifier used as a cache key. Determines the cached count's retrieval.
     * @param query       The query object defining the criteria for counting entities.
     * @param entityClass The class of the entities being counted.
     * @return A {@link Mono} emitting the count of entities as a {@link Long}, potentially from cache.
     */
    protected <T> Mono<Long> countWithCache(Object key, Query query, Class<T> entityClass) {
        Mono<Long> source = this.entityTemplate.count(query, entityClass);
        return countWithCache(key, source).cache();
    }

    /**
     * Executes a SQL count query with caching capabilities. It prepares the SQL query with provided bind parameters,
     * creates a Mono source to fetch the count, and then delegates to another method to handle caching logic.
     *
     * @param key        The unique cache key associated with the count query. Used for caching and retrieving results.
     * @param sql        The SQL count query string to be executed.
     * @param bindParams A map containing named parameter placeholders and their respective values for the SQL query.
     * @return A Mono emitting the count result, potentially fetched from cache or computed from the database.
     */
    protected Mono<Long> countWithCache(Object key, String sql, Map<String, Object> bindParams) {
        var executeSpec = this.databaseClient.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        Mono<Long> source = executeSpec.mapValue(Long.class).first();
        return countWithCache(key, source);
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
        Mono<Long> source = sourceMono.doOnNext(count -> BeanUtils.cachePut(this.cache, cacheKey, count));
        return Mono.justOrEmpty(cacheCount).switchIfEmpty(Mono.defer(() -> source));
    }

    /**
     * Sets the R2dbcEntityTemplate instance to be used by this class for database operations.
     *
     * @param entityTemplate The R2dbcEntityTemplate instance to inject.
     */
    @Autowired
    public void setEntityTemplate(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied all dependencies for this bean. This method allows the bean instance
     * to perform initialization only possible when all bean properties have been set
     * and to throw an exception in the event of misconfiguration.
     *
     * <p>In this implementation, the method sets up the {@code databaseClient} and
     * {@code r2dbcConverter} by extracting them from the injected {@code entityTemplate}.
     * It calls the superclass's {@code afterPropertiesSet} first to ensure any
     * necessary setup in the parent class is also executed.
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.databaseClient = this.entityTemplate.getDatabaseClient();
        this.r2dbcConverter = this.entityTemplate.getConverter();
    }
}