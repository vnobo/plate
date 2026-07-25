package com.plate.boot.config;

import lombok.NonNull;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * Redis configuration that enables Spring's caching annotations and provides a reactive Redis template
 * for non-blocking JSON object storage and retrieval.
 *
 * <p>The {@link #reactiveObjectRedisTemplate(ReactiveRedisConnectionFactory, JsonMapper)} bean builds a
 * {@link ReactiveRedisTemplate} with {@link StringRedisSerializer} keys and Jackson JSON
 * ({@code JacksonJsonRedisSerializer}) values, using the application's shared {@link JsonMapper}.
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
     * @param objectMapper An {@link JsonMapper} instance used for serializing and deserializing Java objects
     *                     to and from JSON. This is crucial for handling value serialization in a way that is
     *                     compatible with the application's object model.
     * @return A configured instance of {@link ReactiveRedisTemplate} ready to perform
     * reactive Redis operations, with keys as strings and values as arbitrary Java objects (serialized as JSON).
     */
    @Bean
    public ReactiveRedisTemplate<@NonNull String, @NonNull Object> reactiveObjectRedisTemplate(ReactiveRedisConnectionFactory factory,
                                                                                               JsonMapper objectMapper) {
        StringRedisSerializer keySerializer = StringRedisSerializer.UTF_8;
        JacksonJsonRedisSerializer<@NonNull Object> serializer =
                new JacksonJsonRedisSerializer<>(objectMapper, Object.class);
        RedisSerializationContext.RedisSerializationContextBuilder<@NonNull String, @NonNull Object> builder =
                RedisSerializationContext.newSerializationContext(serializer);
        RedisSerializationContext<@NonNull String, @NonNull Object> context =
                builder.key(keySerializer).value(serializer).hashKey(keySerializer).hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}