package com.platform.boot.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.annotation.exception.JsonException;
import com.platform.boot.commons.utils.ContextHolder;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains the converters for JsonNode
 *
 * @author billb
 */
public final class JsonNodeConverters {
    public static final JsonNodeConverters INSTANCE = new JsonNodeConverters();

    /**
     * Get the list of converters
     *
     * @return Collection of converters
     */
    public Collection<Object> getConverters() {
        List<Object> converters = new ArrayList<>();
        converters.add(JsonToNodeWriteConverter.INSTANCE);
        converters.add(JsonToNodeReadConverter.INSTANCE);
        return converters;
    }

    @WritingConverter
    private enum JsonToNodeWriteConverter implements Converter<JsonNode, Json> {
        /**
         * default INSTANCE
         */
        INSTANCE;

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

    @ReadingConverter
    private enum JsonToNodeReadConverter implements Converter<Json, JsonNode> {
        /**
         * default INSTANCE
         */
        INSTANCE;

        /**
         * Convert Json to JsonNode
         *
         * @param source Json
         * @return JsonNode
         */
        @Override
        public JsonNode convert(@NonNull Json source) {
            try {
                return ContextHolder.OBJECT_MAPPER.readTree(source.asArray());
            } catch (IOException e) {
                throw JsonException.withError(e);
            }
        }
    }
}