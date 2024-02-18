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

    /**
     * 查询带有缓存的对象
     *
     * @param key         缓存键
     * @param query       查询对象
     * @param entityClass 实体类
     * @return 带有缓存的查询结果
     */
    protected <T> Flux<T> queryWithCache(Object key, Query query, Class<T> entityClass) {
        Flux<T> source = this.entityTemplate.select(query, entityClass);
        return queryWithCache(key, source).cache();
    }

    /**
     * 查询带有缓存的对象
     * @param key 缓存键
     * @param query 查询语句
     * @param bindParams 绑定参数
     * @param entityClass 实体类
     * @return 带有缓存的查询结果
     */
    protected <T> Flux<T> queryWithCache(Object key, String query,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        // 构建数据库执行规范
        DatabaseClient.GenericExecuteSpec executeSpec = this.databaseClient.sql(() -> query);
        for (Map.Entry<String, Object> e : bindParams.entrySet()) {
            executeSpec = executeSpec.bind(e.getKey(), e.getValue());
        }
        // 执行查询并映射结果
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> this.r2dbcConverter.read(entityClass, row, rowMetadata)).all();
        // 调用带有缓存的查询方法
        return queryWithCache(key, source);
    }

    /**
     * 使用缓存和源Flux查询数据。
     *
     * @param key   用于标识数据的键
     * @param sourceFlux 数据源Flux
     * @return 查询到的数据流
     */
    protected <T> Flux<T> queryWithCache(Object key, Flux<T> sourceFlux) {
        // 根据key生成缓存Key
        String cacheKey = key + ":data";
        // 从缓存中获取数据，若不存在则使用ArrayList创建新集合
        Collection<T> cacheData = this.cache.get(cacheKey, ArrayList::new);
        assert cacheData != null;

        // 将源Flux的数据添加到缓存数据集合，并在其完成时将更新后的数据放入缓存
        Flux<T> source = sourceFlux
                .doOnNext(cacheData::add)
                .doAfterTerminate(() -> this.cachePut(cacheKey, cacheData));

        // 如果缓存数据不为空，则直接返回缓存数据流；否则，当数据流为空时切换为从源Flux获取数据
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