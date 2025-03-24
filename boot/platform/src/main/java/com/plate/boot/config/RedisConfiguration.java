package com.plate.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for setting up Redis caching and reactive Redis operations within a Spring application context.
 * This configuration enables caching annotations and provides customizations for RedisCacheManager and a
 * ReactiveRedisTemplate.
 *
 * <p>The `myRedisCacheManagerBuilderCustomizer` bean configures the default cache settings to use
 * a StringRedisSerializer for keys and a Jackson2JsonRedisSerializer for values, allowing seamless
 * serialization/deserialization of objects into/from Redis.
 * <p>The `reactiveObjectRedisTemplate` bean sets up a ReactiveRedisTemplate suitable for interacting
 * with Redis in a non-blocking manner. It uses a combination of StringRedisSerializer for keys and
 * a Jackson2JsonRedisSerializer for values, ensuring compatibility with JSON data and enabling efficient
 * object storage and retrieval.
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfiguration {

    /**
     * Customizes the RedisCacheManagerBuilder by setting up default serialization for keys and values.
     *
     * <p>This method configures the cache manager to use a StringRedisSerializer for serializing keys,
     * ensuring they are stored as strings in Redis. For values, it utilizes a Jackson2JsonRedisSerializer,
     * which allows any Java object to be serialized into JSON format before being saved into Redis, with the
     * help of the provided ObjectMapper instance.
     *
     * @param objectMapper The ObjectMapper instance used to serialize and deserialize Java objects to and from JSON.
     * @return A RedisCacheManagerBuilderCustomizer that applies the serialization settings to the cache builder.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer myRedisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        return (builder) -> builder.cacheDefaults()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Object.class)));
    }

    /**
     * Creates and configures a {@link ReactiveRedisTemplate} for interacting with Redis in a reactive manner.
     * This template is designed to work with string keys and arbitrary Java objects, serialized as JSON,
     * enabling efficient storage and retrieval of complex data structures from a Redis data store.
     *
     * @param factory      A {@link ReactiveRedisConnectionFactory} that provides the connection to the Redis server.
     *                     This factory should be capable of supporting reactive operations.
     * @param objectMapper An {@link ObjectMapper} instance used for serializing and deserializing Java objects
     *                     to and from JSON. This is crucial for handling value serialization in a way that is
     *                     compatible with the application's object model.
     * @return A configured instance of {@link ReactiveRedisTemplate} ready to perform
     * reactive Redis operations, with keys as strings and values as arbitrary Java objects (serialized as JSON).
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveObjectRedisTemplate(ReactiveRedisConnectionFactory factory,
                                                                             ObjectMapper objectMapper) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
                RedisSerializationContext.newSerializationContext(serializer);
        RedisSerializationContext<String, Object> context = builder.key(keySerializer).value(serializer)
                .hashKey(keySerializer).hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}