package com.platform.boot.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.annotation.exception.JsonException;
import com.platform.boot.commons.utils.ContextUtils;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class contains the converters for JsonNode
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
public class JsonNodeConverters {

    @Component
    @WritingConverter
    public static class JsonToNodeWriteConverter implements Converter<JsonNode, Json> {

        /**
         * Convert JsonNode to Json
         *
         * @param source JsonNode
         * @return Json
         */
        @Override
        public Json convert(@NonNull JsonNode source) {
            return Json.of(source.toString());
        }
    }

    @Component
    @ReadingConverter
    public static class JsonToNodeReadConverter implements Converter<Json, JsonNode> {

        /**
         * Convert Json to JsonNode
         *
         * @param source Json
         * @return JsonNode
         */
        @Override
        public JsonNode convert(@NonNull Json source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readTree(source.asArray());
            } catch (IOException e) {
                throw JsonException.withError(e);
            }
        }
    }
}