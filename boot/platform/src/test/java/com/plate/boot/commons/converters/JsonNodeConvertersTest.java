package com.plate.boot.commons.converters;

import com.plate.boot.commons.exception.JsonException;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the pure converter logic in {@link JsonNodeConverters}.
 * No Spring / R2DBC connection is required: only the nested {@code Converter}
 * implementations and the static {@link ContextUtils#OBJECT_MAPPER} are exercised.
 */
class JsonNodeConvertersTest {

    private static JsonMapper savedMapper;

    @BeforeAll
    static void setUp() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDown() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @Test
    void methodTypeReadConverterResolvesKnownMethod() {
        var converter = new JsonNodeConverters.MethodTypeReadConverter();

        assertThat(converter.convert("POST")).isEqualTo(com.plate.boot.relational.MethodType.POST);
        assertThat(converter.convert("DELETE")).isEqualTo(com.plate.boot.relational.MethodType.DELETE);
    }

    @Test
    void methodTypeReadConverterThrowsForUnknownMethod() {
        var converter = new JsonNodeConverters.MethodTypeReadConverter();

        assertThatThrownBy(() -> converter.convert("NOT_A_METHOD"))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void methodTypeWriteConverterReturnsName() {
        var converter = new JsonNodeConverters.MethodTypeWriteConverter();

        assertThat(converter.convert(com.plate.boot.relational.MethodType.PUT)).isEqualTo("PUT");
    }

    @Test
    void jsonWriteConverterWrapsNodeAsJson() {
        var converter = new JsonNodeConverters.JsonToNodeWriteConverter();
        JsonNode node = ContextUtils.OBJECT_MAPPER.createObjectNode().put("name", "John").put("age", 30);

        Json json = converter.convert(node);

        assertThat(json).isNotNull();
    }

    @Test
    void jsonReadConverterParsesJsonBackToNode() {
        var read = new JsonNodeConverters.JsonToNodeReadConverter();
        Json json = Json.of("{\"name\":\"John\",\"age\":30}");

        JsonNode node = read.convert(json);

        assertThat(node).isNotNull();
        assertThat(node.get("name").asText()).isEqualTo("John");
        assertThat(node.get("age").asInt()).isEqualTo(30);
    }

    @Test
    void jsonWriteThenReadConverterRoundTrips() {
        var write = new JsonNodeConverters.JsonToNodeWriteConverter();
        var read = new JsonNodeConverters.JsonToNodeReadConverter();
        JsonNode original = ContextUtils.OBJECT_MAPPER.createObjectNode().put("active", true);

        Json json = write.convert(original);
        JsonNode restored = read.convert(json);

        assertThat(restored).isEqualTo(original);
    }

    @Test
    void jsonReadConverterThrowsJsonExceptionOnInvalidJson() {
        var read = new JsonNodeConverters.JsonToNodeReadConverter();
        Json json = Json.of("this is not valid json");

        assertThatThrownBy(() -> read.convert(json))
                .isInstanceOf(JsonException.class);
    }
}
