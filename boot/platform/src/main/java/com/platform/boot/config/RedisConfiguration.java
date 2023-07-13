package com.platform.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis.
 * Contains a bean for ReactiveRedisTemplate.
 * Uses StringRedisSerializer for keys and GenericJackson2JsonRedisSerializer for values.
 * Enables caching.
 *
 * @author Alex Bob (<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfiguration {

    /**
     * Creates a ReactiveRedisTemplate bean.
     *
     * @param factory      the ReactiveRedisConnectionFactory to use
     * @param objectMapper the ObjectMapper to use for serialization
     * @return the ReactiveRedisTemplate bean
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory,
                                                                       ObjectMapper objectMapper) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
                RedisSerializationContext.newSerializationContext(redisSerializer);
        RedisSerializationContext<String, Object> context = builder.key(keySerializer).value(redisSerializer)
                .hashKey(keySerializer).hashValue(redisSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

}