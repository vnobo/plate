package com.plate.boot.commons.converters;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.exception.JsonException;
import com.plate.boot.commons.utils.ContextUtils;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Configuration class for registering custom converters between JSON and Jackson's JsonNode.
 * This class ensures the converters are properly initialized within a Spring context.
 * It includes two inner classes: {@link JsonToNodeWriteConverter} for writing JSON to JsonNode,
 * and {@link JsonToNodeReadConverter} for reading JsonNode back to JSON.
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class JsonNodeConverters implements InitializingBean {

    /**
     * Invoked by the containing BeanFactory after it has set all bean properties
     * and satisfied all dependencies for this bean. This method allows the bean instance
     * to perform initialization only possible when all bean properties have been set
     * and to throw an exception in the event of misconfiguration.
     * <p>
     * In this implementation, it logs the initialization status of the [JsonNodeConverters],
     * indicating that the converter setup process is being initiated.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [JsonNodeConverters]...");
    }

    /**
     * Converts a Jackson {@link JsonNode} to a custom {@link Json} object for write operations.
     * This converter is designed to be used within a Spring context and is annotated as a component
     * and a writing converter.
     * <p>
     * The conversion process involves converting the JsonNode to its string representation
     * and then wrapping it into a Json object using the {@link Json#of(String)} method.
     */
    @Component
    @WritingConverter
    public static class JsonToNodeWriteConverter implements Converter<JsonNode, Json> {
        /**
         * Converts a Jackson {@link JsonNode} to a custom {@link Json} object.
         *
         * @param source The JsonNode to be converted. Must not be null.
         * @return A {@link Json} object representing the stringified input JsonNode.
         * @throws NullPointerException if the source argument is null.
         */
        @Override
        public Json convert(@NonNull JsonNode source) {
            return Json.of(source.toString());
        }
    }

    /**
     * Converts a custom {@link Json} object to a Jackson {@link JsonNode} for read operations.
     * This converter is designed to be used within a Spring context and is annotated as a component
     * and a reading converter.
     * <p>
     * The conversion process involves parsing the Json object into a JSON array and then
     * using Jackson's ObjectMapper to read this array into a JsonNode structure.
     * If an {@link IOException} occurs during the conversion, it is rethrown as a {@link JsonException}
     * to propagate the error appropriately.
     */
    @Component
    @ReadingConverter
    public static class JsonToNodeReadConverter implements Converter<Json, JsonNode> {
        /**
         * Converts a custom {@link Json} object to a Jackson {@link JsonNode} for read operations.
         *
         * @param source The {@link Json} object to be converted. Must not be null.
         * @return The converted {@link JsonNode} instance ready for read operations.
         * @throws JsonException If an {@link IOException} occurs during the conversion process.
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