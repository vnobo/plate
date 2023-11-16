package com.platform.boot.commons.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;
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

    @Value("${spring.codec.max-in-memory-size:256kb}")
    private DataSize maxInMemorySize;
    protected R2dbcEntityTemplate entityTemplate;
    protected DatabaseClient databaseClient;
    protected R2dbcConverter r2dbcConverter;

    protected <T> Flux<T> queryWithCache(Object key, Query query, Class<T> entityClass) {
        Flux<T> source = this.entityTemplate.select(query, entityClass);
        return queryWithCache(key, source).cache();
    }

    protected <T> Flux<T> queryWithCache(Object key, String query,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.sql(() -> query);
        for (Map.Entry<String, Object> e : bindParams.entrySet()) {
            executeSpec = executeSpec.bind(e.getKey(), e.getValue());
        }
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> this.r2dbcConverter.read(entityClass, row, rowMetadata)).all();
        return queryWithCache(key, source);
    }

    protected <T> Flux<T> queryWithCache(Object key, Flux<T> sourceFlux) {
        String cacheKey = key + ":data";
        Collection<T> cacheData = this.cache.get(cacheKey, ArrayList::new);
        assert cacheData != null;

        Flux<T> source = sourceFlux
                .doOnNext(cacheData::add)
                .doAfterTerminate(() -> this.cachePut(cacheKey, cacheData));
        return Flux.fromIterable(ObjectUtils.isEmpty(cacheData) ? Collections.emptyList() : cacheData)
                .switchIfEmpty(Flux.defer(() -> source));
    }

    protected <T> Mono<Long> countWithCache(Object key, Query query, Class<T> entityClass) {
        Mono<Long> source = this.entityTemplate.count(query, entityClass);
        return countWithCache(key, source);
    }

    protected Mono<Long> countWithCache(Object key, String query, Map<String, Object> bindParams) {
        DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.sql(() -> query);
        for (Map.Entry<String, Object> e : bindParams.entrySet()) {
            executeSpec = executeSpec.bind(e.getKey(), e.getValue());
        }
        Mono<Long> source = executeSpec
                .map(readable -> readable.get(0, Long.class)).one();
        return countWithCache(key, source);
    }

    protected Mono<Long> countWithCache(Object key, Mono<Long> sourceMono) {
        String cacheKey = key + ":count";
        Long cacheCount = this.cache.get(cacheKey, () -> null);
        Mono<Long> source = sourceMono.doOnNext(count -> this.cachePut(cacheKey, count));
        return Mono.justOrEmpty(cacheCount).switchIfEmpty(Mono.defer(() -> source));
    }

    private void cachePut(String cacheKey, Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return;
        }
        DataSize objectSize = com.platform.boot.commons.utils.BeanUtils.getBeanSize(obj);
        if (objectSize.toBytes() > this.maxInMemorySize.toBytes()) {
            log.warn("Object size is too large,Max memory size is " + this.maxInMemorySize + "," +
                    " Object size is " + objectSize + ".");
        }
        this.cache.put(cacheKey, obj);
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