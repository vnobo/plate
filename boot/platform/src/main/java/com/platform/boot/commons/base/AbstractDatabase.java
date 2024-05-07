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

    /**
     * 保护类型的R2dbcEntityTemplate实例，用于数据操作。
     */
    protected R2dbcEntityTemplate entityTemplate;

    /**
     * 保护类型的DatabaseClient实例，提供数据库客户端功能。
     */
    protected DatabaseClient databaseClient;

    /**
     * 保护类型的R2dbcConverter实例，用于R2DBC的转换功能。
     */
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
     *
     * @param key         缓存键
     * @param sql         查询语句
     * @param bindParams  绑定参数
     * @param entityClass 实体类
     * @return 带有缓存的查询结果
     */
    protected <T> Flux<T> queryWithCache(Object key, String sql,
                                         Map<String, Object> bindParams, Class<T> entityClass) {
        // 构建数据库执行规范
        var executeSpec = this.databaseClient.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        // 执行查询并映射结果
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> this.r2dbcConverter.read(entityClass, row, rowMetadata))
                .all();
        // 调用带有缓存的查询方法
        return queryWithCache(key, source);
    }

    /**
     * 使用缓存和源Flux查询数据。
     *
     * @param key        用于标识数据的键
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
                .doAfterTerminate(() -> BeanUtils.cachePut(cacheKey, cacheData, this.cache));

        // 如果缓存数据不为空，则直接返回缓存数据流；否则，当数据流为空时切换为从源Flux获取数据
        return Flux.fromIterable(ObjectUtils.isEmpty(cacheData) ? Collections.emptyList() : cacheData)
                .switchIfEmpty(Flux.defer(() -> source));
    }


    protected <T> Mono<Long> countWithCache(Object key, Query query, Class<T> entityClass) {
        Mono<Long> source = this.entityTemplate.count(query, entityClass);
        return countWithCache(key, source);
    }

    /**
     * 使用缓存计算给定SQL和参数的计数结果。
     *
     * @param key        用于缓存的键，确保相同的查询使用相同的缓存键。
     * @param sql        要执行的SQL查询语句。
     * @param bindParams SQL查询中要绑定的参数。
     * @return 返回查询结果的Mono对象，包含查询到的行数。
     */
    protected Mono<Long> countWithCache(Object key, String sql, Map<String, Object> bindParams) {
        // 构建执行规范，设置SQL语句并绑定参数
        var executeSpec = this.databaseClient.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);

        // 执行查询并仅获取第一个结果的行数
        Mono<Long> source = executeSpec.mapValue(Long.class).first();

        // 使用给定的键和查询结果源对结果进行缓存
        return countWithCache(key, source);
    }


    protected Mono<Long> countWithCache(Object key, Mono<Long> sourceMono) {
        String cacheKey = key + ":count";
        Long cacheCount = this.cache.get(cacheKey, () -> null);
        Mono<Long> source = sourceMono.doOnNext(count -> BeanUtils.cachePut(cacheKey, count, this.cache));
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