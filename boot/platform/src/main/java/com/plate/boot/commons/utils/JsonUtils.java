package com.plate.boot.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plate.boot.commons.exception.JsonException;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Component
public class JsonUtils {

    private static ObjectMapper objectMapper;

    public JsonUtils(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
    }

    public static byte[] toBytes(Object object) {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw JsonException.withError("Json processing exception", e);
        }
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw JsonException.withMsg("序列化失败", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw JsonException.withMsg("反序列化失败", e);
        }
    }

    public static JsonNode toJsonNode(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw JsonException.withMsg("JSON解析失败", e);
        }
    }

    public static String toJsonString(Object obj) {
        return toJson(obj);
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        return fromJson(json, clazz);
    }

    public static <T> T fromJsonNode(JsonNode node, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(node, clazz);
        } catch (Exception e) {
            throw JsonException.withMsg("JSON节点转换失败", e);
        }
    }
}
