package com.plate.boot.config;

import lombok.NonNull;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson3JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

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
    public ReactiveRedisTemplate<@NonNull String, @NonNull Object> reactiveObjectRedisTemplate(ReactiveRedisConnectionFactory factory,
                                                                                               ObjectMapper objectMapper) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson3JsonRedisSerializer<@NonNull Object> serializer = new Jackson3JsonRedisSerializer<>(objectMapper, Object.class);
        RedisSerializationContext.RedisSerializationContextBuilder<@NonNull String, @NonNull Object> builder =
                RedisSerializationContext.newSerializationContext(serializer);
        RedisSerializationContext<@NonNull String, @NonNull Object> context = builder.key(keySerializer).value(serializer)
                .hashKey(keySerializer).hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}