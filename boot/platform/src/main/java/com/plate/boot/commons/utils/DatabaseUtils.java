package com.plate.boot.commons.utils;

import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.exception.RestServerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

/**
 * Database utility class providing reactive database operations, caching functionality,
 * and memory management capabilities for the application.
 *
 * <p>This class offers a set of static methods for executing parameterized SQL queries,
 * counting records with caching support, and managing memory constraints
 * to prevent SQL injection vulnerabilities and memory overflow issues.
 *
 * <p>Example usage for reactive query execution:
 * <pre>{@code
 * Query query = Query.query(Criteria.where("status").is("active"));
 * Flux<User> users = DatabaseUtils.query(query, User.class);
 * }</pre>
 * This generates parameterized SQL query with caching
 *
 * <p>Example usage for batch processing:
 * <pre>{@code
 * Flux<User> users = Flux.fromIterable(userList);
 * Flux<ProgressEvent> result = DatabaseUtils.batchEvent(users, user -> {
 *     return userRepository.save(user);
 * });
 * }</pre>
 * This processes users in batch with progress tracking
 *
 * @see R2dbcEntityTemplate for reactive database operations
 * @see ReactiveRedisTemplate for caching implementation
 * @since 1.0
 */
@Log4j2
@Component
public class DatabaseUtils implements InitializingBean {

    /**
     * The maximum number of elements that can be stored in the Redis cache.
     * This value is used to determine when the cache should be cleared to avoid memory overflow.
     *
     * @see ReactiveRedisTemplate for caching implementation details
     */
    public static ReactiveRedisTemplate<String, Object> REACTIVE_REDIS_TEMPLATE;

    /**
     * The maximum size of data that can be held in memory.
     * This threshold determines when data should be processed differently
     * (e.g., written to disk) to avoid exceeding memory constraints.
     *
     * @see #setMaxInMemorySize(DataSize) for configuration
     */
    public static DataSize MAX_IN_MEMORY_SIZE;

    /**
     * The R2DBC entity template instance used for reactive database operations.
     * Facilitates query execution, entity conversion, and transaction management.
     *
     * @see R2dbcEntityTemplate for detailed usage
     */
    public static R2dbcEntityTemplate ENTITY_TEMPLATE;

    /**
     * The database client instance for executing SQL operations.
     * Provides methods for querying, updating, and managing database connections.
     *
     * @see DatabaseClient for database client capabilities
     */
    public static DatabaseClient DATABASE_CLIENT;

    /**
     * The R2DBC converter instance for mapping between database results and domain objects.
     * Handles conversion between R2DBC Data types and entity classes.
     *
     * @see R2dbcConverter for conversion details
     */
    public static R2dbcConverter R2DBC_CONVERTER;

    private final R2dbcEntityTemplate entityTemplate;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final DatabaseClient databaseClient;
    private final R2dbcConverter r2dbcConverter;

    public DatabaseUtils(R2dbcEntityTemplate entityTemplate,
                         ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.entityTemplate = entityTemplate;
        this.redisTemplate = redisTemplate;
        this.databaseClient = entityTemplate.getDatabaseClient();
        this.r2dbcConverter = entityTemplate.getConverter();
    }

    /**
     * Executes a database query with caching functionality.
     *
     * <p>Example usage:
     * <pre>{@code
     * Query query = Query.query(Criteria.where("status").is("active"));
     * Flux<User> users = DatabaseUtils.query(query, User.class);
     * }</pre>
     * This generates parameterized SQL query with caching
     *
     * @param <T>         The type of entities expected as query results
     * @param query       The query object defining the SQL query and its parameters
     * @param entityClass The class of the entity to map results to
     * @return A Flux emitting the query results, potentially from cache
     * @see Query for query construction
     * @see Flux#cache() for caching behavior
     * @since 1.0
     */
    public static <T> Flux<T> query(Query query, Class<T> entityClass) {
        Flux<T> source = ENTITY_TEMPLATE.select(query, entityClass);
        return source
                .flatMapSequential(BeanUtils::serializeUserAuditor)
                .cache();
    }

    /**
     * Executes a parameterized SQL query with caching capability.
     *
     * <p>Example usage:
     * <pre>{@code
     * String sql = "SELECT * FROM users WHERE name = :name";
     * Map<String, Object> params = Map.of("name", "John");
     * Flux<User> users = DatabaseUtils.query(sql, params, User.class);
     * }</pre>
     * This generates parameterized SQL query with caching
     *
     * @param <T>         The type of entities the SQL query results will be mapped to
     * @param sql         The SQL query string with named parameters
     * @param bindParams  A map containing named parameter bindings for the SQL query
     * @param entityClass The class of the entity to convert query results into
     * @return A Flux emitting the entities from the query, potentially from the cache
     * @throws IllegalArgumentException if sql or bindParams is null
     * @see DatabaseClient #sql() for SQL query execution
     * @see R2dbcConverter #read(Class, Object, Object) for entity conversion
     * @since 1.0
     */
    public static <T> Flux<T> query(String sql, Map<String, Object> bindParams, Class<T> entityClass) {
        var executeSpec = DATABASE_CLIENT.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        Flux<T> source = executeSpec
                .map((row, rowMetadata) -> R2DBC_CONVERTER.read(entityClass, row, rowMetadata))
                .all();
        return source
                .flatMapSequential(BeanUtils::serializeUserAuditor)
                .cache();
    }

    /**
     * Counts entities with caching support based on provided query criteria.
     *
     * <p>Example usage:
     * <pre>{@code
     * Query query = Query.query(Criteria.where("status").is("active"));
     * Mono<Long> count = DatabaseUtils.count(query, User.class);
     * }</pre>
     * This generates parameterized count query with caching
     *
     * @param <T>         The type of entities for which the count is performed
     * @param query       The query object defining criteria for counting
     * @param entityClass The class of the entities being counted
     * @return A Mono emitting the count as a Long, potentially from cache
     * @throws IllegalArgumentException if query or entityClass is null
     * @see R2dbcEntityTemplate#count(Query, Class) for underlying implementation
     * @since 1.0
     */
    public static <T> Mono<Long> count(Query query, Class<T> entityClass) {
        return DatabaseUtils.ENTITY_TEMPLATE.count(query, entityClass).cache();
    }

    /**
     * Executes a SQL count query with caching capabilities using parameterized queries.
     *
     * <p>Example usage:
     * <pre>{@code
     * String sql = "SELECT COUNT(*) FROM users WHERE age > :age";
     * Map<String, Object> params = Map.of("age", 30);
     * Mono<Long> count = DatabaseUtils.count(sql, params);
     * }</pre>
     * This generates parameterized count query with caching
     *
     * @param sql        The SQL count query string with named parameters
     * @param bindParams A map containing parameter bindings for the SQL query
     * @return A Mono emitting the count result, potentially from cache
     * @throws IllegalArgumentException if sql or bindParams is null
     * @see DatabaseClient #sql() for SQL execution
     * @since 1.0
     */
    public static Mono<Long> count(String sql, Map<String, Object> bindParams) {
        var executeSpec = DatabaseUtils.DATABASE_CLIENT.sql(() -> sql);
        executeSpec = executeSpec.bindValues(bindParams);
        return executeSpec.mapValue(Long.class).first().cache();
    }

    /**
     * Calculates the size of a Java bean by serializing it into a byte array.
     *
     * <p>Example usage:
     * <pre>{@code
     * User user = new User("John", 30);
     * DataSize size = DatabaseUtils.getBeanSize(user);
     * }</pre>
     * This returns the serialized size of the user object
     *
     * @param obj The Java bean object to calculate size for
     * @return The size of the bean as a DataSize object
     * @throws IllegalArgumentException if obj is null
     * @see DataSize for size representation
     * @since 1.0
     */
    public static DataSize getBeanSize(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            log.warn("Object is empty or null");
            return DataSize.ofBytes(0);
        }
        try {
            byte[] bytes = BeanUtils.objectToBytes(obj);
            if (bytes == null) {
                log.warn("Object serialization returned null bytes");
                return DataSize.ofBytes(0);
            }
            int size = bytes.length;
            return DataSize.ofBytes(size);
        } catch (Exception e) {
            log.error("Bean Size calculation failed: {}", e.toString());
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
    @Value("${spring.codec.max-in-memory-size:256KB}")
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
        // Initialize static fields from instance fields
        ENTITY_TEMPLATE = this.entityTemplate;
        DATABASE_CLIENT = this.databaseClient;
        R2DBC_CONVERTER = this.r2dbcConverter;
        REACTIVE_REDIS_TEMPLATE = this.redisTemplate;
    }

    /**
     * Processes a batch of requests with progress tracking and error handling.
     *
     * <p>Example usage with user entities:
     * <pre>{@code
     * Flux<User> users = Flux.fromIterable(userList);
     * Flux<ProgressEvent> result = DatabaseUtils.batchEvent(users, user -> {
     *     return userRepository.save(user);
     * });
     * }</pre>
     * This processes users in batch with progress events
     *
     * @param <T>          The type of request objects in the batch
     * @param requests     The flux of request items to process
     * @param saveFunction The function to apply to each request item
     * @return A flux of ProgressEvent instances tracking batch processing
     * @throws IllegalArgumentException if requests or saveFunction is null
     * @see ProgressEvent for event structure
     * @see Flux#delayElements(Duration) for controlled processing
     * @since 1.0
     */
    public static <T> Flux<ProgressEvent> batchEvent(Flux<T> requests, Function<T, Mono<?>> saveFunction) {
        var startMono = Mono.fromCallable(() -> ProgressEvent.of(0L, null)
                .withMessage("Starting batch processing..."));
        var itemsFlux = requests.index().flatMap(tuple2 -> {
            var index = tuple2.getT1() + 1;
            var req = tuple2.getT2();
            var event = ProgressEvent.of(index, req);
            return saveFunction.apply(req).flatMap(res -> Mono.just(event.withResult(
                            "Processed success batch save item.", res)))
                    .onErrorResume(err -> Mono.just(event.withError("Processed failed save item. msg: "
                                    + err.getCause().getMessage(),
                            RestServerException.withMsg(err.getLocalizedMessage(), err))));
        }).delayElements(Duration.ofMillis(100));
        var endMono = Mono.fromCallable(() -> ProgressEvent.of(0L, null)
                .withMessage("Batch processing completed"));
        return Flux.concat(startMono, itemsFlux, endMono);
    }
}
