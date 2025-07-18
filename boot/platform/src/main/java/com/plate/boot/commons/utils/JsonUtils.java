package com.plate.boot.commons.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
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
}
