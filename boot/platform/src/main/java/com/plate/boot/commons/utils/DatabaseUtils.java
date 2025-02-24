package com.plate.boot.commons.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Component
public class DatabaseUtils implements InitializingBean {

    /**
     * Represents the maximum size of data that can be held in memory.
     * This threshold is utilized to determine when data should be processed differently,
     * such as being written to disk, to avoid exceeding memory constraints.
     */
    public static DataSize MAX_IN_MEMORY_SIZE;

    /**
     * The R2dbcEntityTemplate instance used for executing reactive database operations.
     * This template facilitates interaction with the database, including from execution,
     * entity conversion, and transaction management specifically tailored for R2DBC (Reactive Relational Database Connectivity).
     */
    public static R2dbcEntityTemplate ENTITY_TEMPLATE;

    /**
     * Represents a client for interacting with the database, facilitating operations such as
     * querying, updating, and managing data within the database. This field is initialized
     * and configured typically during the setup or initialization phase of the hosting class,
     * enabling seamless database access throughout the application.
     */
    public static DatabaseClient DATABASE_CLIENT;

    /**
     * The R2DBC Converter instance used for converting between R2DBC Data types and domain-specific objects.
     * This converter plays a crucial role in mapping from results to entity classes and vice versa,
     * facilitating seamless interaction with the R2DBC database through the R2dbcEntityTemplate.
     */
    public static R2dbcConverter R2DBC_CONVERTER;

    DatabaseUtils(R2dbcEntityTemplate entityTemplate) {
        DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
        DatabaseUtils.DATABASE_CLIENT = entityTemplate.getDatabaseClient();
        DatabaseUtils.R2DBC_CONVERTER = entityTemplate.getConverter();
    }

    /**
     * Executes a database from with caching functionality.
     * It takes a cache key, a from object, and an entity class type to perform the operation.
     * The results are cached for future queries with the same key.
     *
     * @param <T>         The type of entities expected as from results.
     * @param query       The form object defining the SQL from and its potential parameters.
     * @param entityClass The class of the entity that each row in the result set will be mapped to.
     * @return A {@link Flux} emitting the form results, potentially from cache if previously stored.
     */
    public static <T> Flux<T> query(Query query, Class<T> entityClass) {
        Flux<T> source = ENTITY_TEMPLATE.select(query, entityClass);
        return source.flatMapSequential(BeanUtils::serializeUserAuditor).cache();
    }

    /**
     * Executes a SQL from with caching capability. It binds provided parameters to the SQL from,
     * maps the result set to entities of the specified class, applies user auditor serialization,
     * and utilizes a cache to store from results for subsequent identical queries.
     *
     * @param <T>         The type of entities the SQL from results will be mapped to.
     * @param sql         The SQL from string to be executed.
     * @param bindParams  A map containing named parameter bindings for the SQL from.
     * @param entityClass The class of the entity that each row in the result set will be converted into.
     * @return A {@link Flux} emitting the entities resulting from the from, potentially from the cache.
     */
    public static <T> Flux<T> query(String sql,
                                    Map<String, Object> bindParams, Class<T> entityClass) {
        var executeSpec = DatabaseUtils.DATABASE_CLIENT.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> DatabaseUtils.R2DBC_CONVERTER.read(entityClass, row, rowMetadata))
                .all();
        return source.flatMapSequential(BeanUtils::serializeUserAuditor).cache();
    }

    /**
     * Counts entities with caching support based on the provided key, from, and entity class.
     * This method enhances entity counting by storing the count result in a cache,
     * allowing subsequent calls with the same key to retrieve the count directly from the cache
     * rather than executing the form again.
     *
     * @param <T>         The type of entities for which the count is to be performed.
     * @param query       The form object defining the criteria for counting entities.
     * @param entityClass The class of the entities being counted.
     * @return A {@link Mono} emitting the count of entities as a {@link Long}, potentially from cache.
     */
    public static <T> Mono<Long> count(Query query, Class<T> entityClass) {
        return DatabaseUtils.ENTITY_TEMPLATE.count(query, entityClass).cache();
    }

    /**
     * Executes a SQL count from with caching capabilities. It prepares the SQL from with provided bind parameters,
     * creates a Mono source to fetch the count, and then delegates to another method to handle caching logic.
     *
     * @param sql        The SQL count from string to be executed.
     * @param bindParams A map containing named parameter placeholders and their respective values for the SQL from.
     * @return A Mono emitting the count result, potentially fetched from cache or computed from the database.
     */
    public static Mono<Long> count(String sql, Map<String, Object> bindParams) {
        var executeSpec = DatabaseUtils.DATABASE_CLIENT.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        return executeSpec.mapValue(Long.class).first().cache();
    }

    /**
     * Calculates the size of the given Java bean by serializing it into a byte array and measuring its length.
     * This method provides an estimate of the space the object occupies when serialized.
     *
     * @param obj The Java bean object whose size is to be calculated. Must not be null.
     * @return The size of the bean as a {@link DataSize} object, representing the size in a human-readable format.
     * If the object is empty or serialization fails, returns a DataSize of 0 bytes.
     * @throws IllegalArgumentException If the provided object is null, since null cannot be sized.
     */
    public static DataSize getBeanSize(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            log.warn("Object is empty,This object not null.");
            return DataSize.ofBytes(0);
        }
        try {
            int size = BeanUtils.objectToBytes(obj).length;
            return DataSize.ofBytes(size);
        } catch (Exception e) {
            log.error("Bean Size IO exception! msg: {}", e.getLocalizedMessage());
            return DataSize.ofBytes(0);
        }
    }

    /**
     * Sets the maximum size of data that can be stored in memory before being written to disk.
     * This method allows configuration of the maximum in-memory size limit, which is particularly
     * useful for managing memory usage when handling large amounts of data, such as file uploads.
     *
     * @param dataSize The maximum in-memory size limit defined as a {@link DataSize}. Defaults to 256 kilobytes if not explicitly set.
     */
    @Value("${spring.codec.max-in-memory-size:256kb}")
    public void setMaxInMemorySize(DataSize dataSize) {
        MAX_IN_MEMORY_SIZE = dataSize;
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
        log.info("Initializing utils [DatabaseUtils]");
    }
}
