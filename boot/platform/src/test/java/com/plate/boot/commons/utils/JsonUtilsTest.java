package com.plate.boot.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = {ObjectMapper.class})
@DisplayName("JsonUtils Unit Tests")
class JsonUtilsTest {

    @BeforeEach
    void setUp() {
    }

    // Test classes
    static class TestObject {
        public String name;
        public Integer value;

        public TestObject(String name, Integer value) {
            this.name = name;
            this.value = value;
        }
    }

    static class ComplexObject {
        public String name;
        public List<TestObject> items;

        public ComplexObject(String name, List<TestObject> items) {
            this.name = name;
            this.items = items;
        }
    }

    static class EmptyObject {
        // No fields
    }

    @Nested
    @DisplayName("Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize simple object to JSON string")
        void shouldSerializeSimpleObjectToJsonString() {
            TestObject obj = new TestObject("test", 123);
            String json = JsonUtils.toJson(obj);
            assertThat(json).contains("\"name\":\"test\"").contains("\"value\":123");
        }

        @Test
        @DisplayName("Should serialize complex object to JSON string")
        void shouldSerializeComplexObjectToJsonString() {
            TestObject nested = new TestObject("nested", 456);
            ComplexObject obj = new ComplexObject("main", List.of(nested));
            String json = JsonUtils.toJson(obj);
            assertThat(json).contains("\"name\":\"main\"").contains("\"items\":[{").contains("\"name\":\"nested\"");
        }

        @Test
        @DisplayName("Should serialize object to bytes")
        void shouldSerializeObjectToBytes() {
            TestObject obj = new TestObject("test", 123);
            byte[] bytes = JsonUtils.toBytes(obj);
            assertThat(bytes).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Should handle null object serialization")
        void shouldHandleNullObjectSerialization() {
            String json = JsonUtils.toJson(null);
            assertThat(json).isEqualTo("null");
        }

        @Test
        @DisplayName("Should handle empty object serialization")
        void shouldHandleEmptyObjectSerialization() {
            EmptyObject obj = new EmptyObject();
            String json = JsonUtils.toJson(obj);
            assertThat(json).isEqualTo("{}");
        }

        @Test
        @DisplayName("Should handle collection serialization")
        void shouldHandleCollectionSerialization() {
            List<TestObject> list = List.of(
                    new TestObject("item1", 1),
                    new TestObject("item2", 2)
            );
            String json = JsonUtils.toJson(list);
            assertThat(json).contains("[{").contains("\"item1\"").contains("\"item2\"");
        }
    }

    @Nested
    @DisplayName("Deserialization Tests")
    class DeserializationTests {

        @Test
        @DisplayName("Should deserialize JSON string to object")
        void shouldDeserializeJsonStringToObject() {
            String json = "{\"name\":\"test\",\"value\":123}";
            TestObject obj = JsonUtils.fromJson(json, TestObject.class);
            assertThat(obj.name).isEqualTo("test");
            assertThat(obj.value).isEqualTo(123);
        }

        @Test
        @DisplayName("Should deserialize JSON string to complex object")
        void shouldDeserializeJsonStringToComplexObject() {
            String json = "{\"name\":\"main\",\"items\":[{\"name\":\"nested\",\"value\":456}]}";
            ComplexObject obj = JsonUtils.fromJson(json, ComplexObject.class);
            assertThat(obj.name).isEqualTo("main");
            assertThat(obj.items).hasSize(1);
            assertThat(obj.items.getFirst().name).isEqualTo("nested");
            assertThat(obj.items.getFirst().value).isEqualTo(456);
        }

        @Test
        @DisplayName("Should handle invalid JSON during deserialization")
        void shouldHandleInvalidJsonDuringDeserialization() {
            String invalidJson = "{\"name\":\"test\", \"value\":}";
            assertThatThrownBy(() -> JsonUtils.fromJson(invalidJson, TestObject.class))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle null JSON during deserialization")
        void shouldHandleNullJsonDuringDeserialization() {
            assertThatThrownBy(() -> JsonUtils.fromJson(null, TestObject.class))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle empty JSON during deserialization")
        void shouldHandleEmptyJsonDuringDeserialization() {
            assertThatThrownBy(() -> JsonUtils.fromJson("", TestObject.class))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle JSON with missing fields")
        void shouldHandleJsonWithMissingFields() {
            String json = "{\"name\":\"test\"}"; // Missing 'value' field
            TestObject obj = JsonUtils.fromJson(json, TestObject.class);
            assertThat(obj.name).isEqualTo("test");
            assertThat(obj.value).isNull(); // Default value for Integer
        }
    }

    @Nested
    @DisplayName("JsonNode Tests")
    class JsonNodeTests {

        @Test
        @DisplayName("Should parse JSON string to JsonNode")
        void shouldParseJsonStringToJsonNode() {
            String json = "{\"name\":\"test\",\"value\":123}";
            var node = JsonUtils.toJsonNode(json);
            assertThat(node.get("name").asText()).isEqualTo("test");
            assertThat(node.get("value").asInt()).isEqualTo(123);
        }

        @Test
        @DisplayName("Should convert JsonNode to object")
        void shouldConvertJsonNodeToObject() {
            String json = "{\"name\":\"test\",\"value\":123}";
            var node = JsonUtils.toJsonNode(json);
            TestObject obj = JsonUtils.fromJsonNode(node, TestObject.class);
            assertThat(obj.name).isEqualTo("test");
            assertThat(obj.value).isEqualTo(123);
        }

        @Test
        @DisplayName("Should handle invalid JSON when parsing to JsonNode")
        void shouldHandleInvalidJsonWhenParsingToJsonNode() {
            String invalidJson = "{\"name\":\"test\", \"value\":}";
            assertThatThrownBy(() -> JsonUtils.toJsonNode(invalidJson))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should handle invalid JsonNode conversion")
        void shouldHandleInvalidJsonNodeConversion() {
            String json = "{\"name\":\"test\",\"value\":\"invalid\"}"; // Value should be int
            var node = JsonUtils.toJsonNode(json);
            assertThatThrownBy(() -> JsonUtils.fromJsonNode(node, TestObject.class))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in JSON")
        void shouldHandleSpecialCharactersInJson() {
            TestObject obj = new TestObject("test\"with\"quotes", 123);
            String json = JsonUtils.toJson(obj);
            TestObject deserialized = JsonUtils.fromJson(json, TestObject.class);
            assertThat(deserialized.name).isEqualTo("test\"with\"quotes");
        }

        @Test
        @DisplayName("Should handle unicode characters in JSON")
        void shouldHandleUnicodeCharactersInJson() {
            TestObject obj = new TestObject("测试中文", 123);
            String json = JsonUtils.toJson(obj);
            TestObject deserialized = JsonUtils.fromJson(json, TestObject.class);
            assertThat(deserialized.name).isEqualTo("测试中文");
        }

        @Test
        @DisplayName("Should handle nested collections")
        void shouldHandleNestedCollections() {
            Map<String, List<TestObject>> nestedMap = Map.of(
                    "group1", List.of(new TestObject("item1", 1), new TestObject("item2", 2)),
                    "group2", List.of(new TestObject("item3", 3))
            );
            String json = JsonUtils.toJson(nestedMap);
            assertThat(json).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Should handle large objects")
        void shouldHandleLargeObjects() {
            // Create a large object with many fields
            TestObject obj = new TestObject("a".repeat(1000), 123);
            String json = JsonUtils.toJson(obj);
            TestObject deserialized = JsonUtils.fromJson(json, TestObject.class);
            assertThat(deserialized.name).hasSize(1000);
        }
    }
}