package com.platform.boot.commons.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is an abstract class that provides a base implementation for database services.
 * It sets up the R2dbcEntityTemplate, DatabaseClient, and R2dbcConverter for use in the service.
 * Extend this class to create a new database service.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public abstract class AbstractDatabase extends AbstractService {

    protected R2dbcEntityTemplate entityTemplate;
    protected DatabaseClient databaseClient;
    protected R2dbcConverter r2dbcConverter;

    /**
     * Query entities that meet the conditions with cache.
     *
     * @param key         cache key
     * @param entityClass entity class
     * @param query       query condition
     * @param <T>         generic type, representing entity class
     * @return a Flux containing entity class objects
     */
    protected <T> Flux<T> queryWithCache(Object key, Query query, Class<T> entityClass) {
        // Construct the query request and sort the results
        Flux<T> source = this.entityTemplate.select(query, entityClass);
        // If there is no data in the cache, return the query result; otherwise, return the cache data
        return queryWithCache(key, source).cache();
    }

    /**
     * Query entities that meet the conditions with cache.
     *
     * @param key         cache key
     * @param entityClass entity class
     * @param query       query condition
     * @param <T>         generic type, representing entity class
     * @return a Flux containing entity class objects
     */
    protected <T> Flux<T> queryWithCache(Object key, String query,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        // Create a GenericExecuteSpec object from the given query
        var executeSpec = this.databaseClient.sql(() -> query);
        // Bind the given parameters to the query
        for (var e : bindParams.entrySet()) {
            executeSpec = executeSpec.bind(e.getKey(), e.getValue());
        }
        // Read the results of the query into an entity class
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> this.r2dbcConverter.read(entityClass, row, rowMetadata)).all();
        // If there is no data in the cache, return the query result; otherwise, return the cache data
        return queryWithCache(key, source);
    }

    /**
     * Query data from cache.
     *
     * @param key        cache key
     * @param sourceMono data source
     * @return query result
     */
    protected <T> Flux<T> queryWithCache(Object key, Flux<T> sourceMono) {
        String cacheKey = key + ":data";
        // Get data from cache
        List<T> cacheData = this.cache.get(cacheKey, ArrayList::new);
        // Note: the returned list will not be null
        assert cacheData != null;

        // Construct the query request and sort the results
        Flux<T> source = sourceMono
                // Add the query result to the cache
                .doOnNext(cacheData::add)
                // When the query is complete, store the data in the cache
                .doAfterTerminate(() -> this.cache.put(cacheKey, cacheData));
        // If there is no data in the cache, return the query result; otherwise, return the cache data
        return Flux.fromIterable(ObjectUtils.isEmpty(cacheData) ? Collections.emptyList() : cacheData)
                .switchIfEmpty(source);
    }

    /**
     * 分页并缓存查询结果。
     *
     * @param key         缓存键值
     * @param entityClass 实体类
     * @param query       查询条件
     * @param <T>         实体类型
     * @return 查询结果元组（包括实体列表和总数）
     */
    protected <T> Mono<Long> countWithCache(Object key, Query query, Class<T> entityClass) {
        // 将查询结果添加到缓存中
        Mono<Long> source = this.entityTemplate.count(query, entityClass);
        // 如果缓存中没有数据，返回查询结果；否则返回缓存数据
        return countWithCache(key, source);
    }

    /**
     * 分页并缓存查询结果。
     *
     * @param key   缓存键值
     * @param query 查询语句
     * @return 查询结果元组（包括实体列表和总数）
     */
    protected Mono<Long> countWithCache(Object key, String query, Map<String, Object> bindParams) {
        // Create a GenericExecuteSpec object from the given query
        var executeSpec = this.databaseClient.sql(() -> query);
        // Bind the given parameters to the query
        for (var e : bindParams.entrySet()) {
            executeSpec = executeSpec.bind(e.getKey(), e.getValue());
        }
        // Read the results of the query into an entity class
        Mono<Long> source = executeSpec
                .map(readable -> readable.get(0, Long.class)).one();
        // 如果缓存中没有数据，返回查询结果；否则返回缓存数据
        return countWithCache(key, source);
    }

    /**
     * 从缓存中查询数据
     *
     * @param key        缓存的 key
     * @param sourceMono 数据源
     * @return 查询结果
     */
    protected Mono<Long> countWithCache(Object key, Mono<Long> sourceMono) {
        String cacheKey = key + ":count";
        // 从缓存中获取数据
        Long cacheCount = this.cache.get(cacheKey, () -> null);
        // 将查询结果添加到缓存中
        Mono<Long> source = sourceMono.doOnNext(count -> this.cache.put(cacheKey, count));
        // 如果缓存中没有数据，返回查询结果；否则返回缓存数据
        return Mono.justOrEmpty(cacheCount).switchIfEmpty(source);
    }


    /**
     * Set the R2dbcEntityTemplate for the service.
     *
     * @param entityTemplate the R2dbcEntityTemplate to set
     */
    @Autowired
    public void setEntityTemplate(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    /**
     * Set up the DatabaseClient and R2dbcConverter for the service after properties are set.
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.databaseClient = this.entityTemplate.getDatabaseClient();
        this.r2dbcConverter = this.entityTemplate.getConverter();
    }
}