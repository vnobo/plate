package com.plate.auth.commons.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.plate.auth.commons.exception.JsonException;
import com.plate.auth.commons.utils.ContextUtils;
import jakarta.persistence.AttributeConverter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class JsonNodeConverters implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [JsonNodeConverters]...");
    }

    @Component
    @jakarta.persistence.Converter(autoApply = true)
    public static class CollectionAttributeConverter implements AttributeConverter<ObjectNode, String> {

        @Override
        public String convertToDatabaseColumn(@NonNull ObjectNode source) {
            try {
                if (ObjectUtils.isEmpty(source) || source.isEmpty()) {
                    return null;
                }
                return ContextUtils.OBJECT_MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to String error",
                        "Object converter Collection to Json error, message: " + e.getMessage());
            }
        }

        @Override
        public ObjectNode convertToEntityAttribute(String value) {
            try {
                if (StringUtils.hasLength(value)) {
                    return ContextUtils.OBJECT_MAPPER.readValue(value, new TypeReference<>() {
                    });
                }
                return null;
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to collection error",
                        "Object converter Json to Collection error, message: " + e.getMessage());
            }

        }
    }

    @Component
    @WritingConverter
    public static class JsonToNodeWriteConverter implements Converter<ObjectNode, String> {
        @Override
        public String convert(@NonNull ObjectNode source) {
            return source.toString();
        }
    }

    @Component
    @ReadingConverter
    public static class JsonToNodeReadConverter implements Converter<String, ObjectNode> {
        @Override
        public ObjectNode convert(@NonNull String source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readTree(source).deepCopy();
            } catch (IOException e) {
                throw JsonException.withError(e);
            }
        }
    }
}