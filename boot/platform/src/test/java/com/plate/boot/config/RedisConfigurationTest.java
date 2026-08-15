package com.plate.boot.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link RedisConfiguration#reactiveObjectRedisTemplate}.
 */
class RedisConfigurationTest {

    @Test
    void reactiveObjectRedisTemplateUsesStringKeysAndJsonValues() {
        ReactiveRedisConnectionFactory factory = mock(ReactiveRedisConnectionFactory.class);
        JsonMapper mapper = JsonMapper.builder().build();
        RedisConfiguration configuration = new RedisConfiguration();

        ReactiveRedisTemplate<String, Object> template =
                configuration.reactiveObjectRedisTemplate(factory, mapper);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isSameAs(factory);

        RedisSerializationContext<String, Object> context = template.getSerializationContext();
        assertThat(context).isNotNull();

        String roundTrippedKey = context.getKeySerializationPair()
                .read(context.getKeySerializationPair().write("test-key"));
        assertThat(roundTrippedKey).isEqualTo("test-key");

        Object roundTrippedValue = context.getValueSerializationPair()
                .read(context.getValueSerializationPair().write("test-value"));
        assertThat(roundTrippedValue).isEqualTo("test-value");
    }
}
