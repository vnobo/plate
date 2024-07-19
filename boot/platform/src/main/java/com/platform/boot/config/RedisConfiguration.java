package com.platform.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Alex Bob (<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisConfiguration {

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