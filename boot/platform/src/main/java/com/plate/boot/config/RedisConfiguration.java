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

    @Bean
    public RedisCacheManagerBuilderCustomizer myRedisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        return (builder) -> builder.cacheDefaults()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, Object.class)));
    }

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