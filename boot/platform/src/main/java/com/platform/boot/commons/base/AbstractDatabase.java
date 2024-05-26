package com.platform.boot.commons.base;

import com.platform.boot.commons.utils.BeanUtils;
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
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public abstract class AbstractDatabase extends AbstractService {

    protected R2dbcEntityTemplate entityTemplate;

    protected DatabaseClient databaseClient;

    protected R2dbcConverter r2dbcConverter;

    protected <T> Flux<T> queryWithCache(Object key, Query query, Class<T> entityClass) {
        Flux<T> source = this.entityTemplate.select(query, entityClass);
        return queryWithCache(key, source).cache();
    }

    protected <T> Flux<T> queryWithCache(Object key, String sql,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        var executeSpec = this.databaseClient.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> this.r2dbcConverter.read(entityClass, row, rowMetadata))
                .all();

        return queryWithCache(key, source);
    }

    protected <T> Flux<T> queryWithCache(Object key, Flux<T> sourceFlux) {
        String cacheKey = key + ":data";
        Collection<T> cacheData = this.cache.get(cacheKey, ArrayList::new);
        assert cacheData != null;

        Flux<T> source = sourceFlux
                .doOnNext(cacheData::add)
                .doAfterTerminate(() -> BeanUtils.cachePut(this.cache, cacheKey, cacheData));

        return Flux.fromIterable(ObjectUtils.isEmpty(cacheData) ? Collections.emptyList() : cacheData)
                .switchIfEmpty(Flux.defer(() -> source));
    }

    protected <T> Mono<Long> countWithCache(Object key, Query query, Class<T> entityClass) {
        Mono<Long> source = this.entityTemplate.count(query, entityClass);
        return countWithCache(key, source);

    }

    protected Mono<Long> countWithCache(Object key, String sql, Map<String, Object> bindParams) {
        var executeSpec = this.databaseClient.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);

        Mono<Long> source = executeSpec.mapValue(Long.class).first();

        return countWithCache(key, source);
    }


    protected Mono<Long> countWithCache(Object key, Mono<Long> sourceMono) {
        String cacheKey = key + ":count";
        Long cacheCount = this.cache.get(cacheKey, () -> null);
        Mono<Long> source = sourceMono.doOnNext(count -> BeanUtils.cachePut(this.cache, cacheKey, count));
        return Mono.justOrEmpty(cacheCount).switchIfEmpty(Mono.defer(() -> source));
    }

    @Autowired
    public void setEntityTemplate(R2dbcEntityTemplate entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        this.databaseClient = this.entityTemplate.getDatabaseClient();
        this.r2dbcConverter = this.entityTemplate.getConverter();
    }
}