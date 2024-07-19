package com.plate.authorization.commons.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.plate.authorization.commons.exception.JsonException;
import com.plate.authorization.commons.utils.ContextUtils;
import jakarta.persistence.AttributeConverter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CollectionConverters implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [CollectionConverters]...");
    }

    @Component
    @jakarta.persistence.Converter
    public static class CollectionAttributeConverter implements AttributeConverter<String, Collection<?>> {

        @Override
        public Collection<?> convertToDatabaseColumn(@NonNull String source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readValue(source, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to collection error",
                        "Object converter Json to Collection error, message: " + e.getMessage());
            }
        }

        @Override
        public String convertToEntityAttribute(Collection<?> value) {
            try {
                return ContextUtils.OBJECT_MAPPER.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to String error",
                        "Object converter Collection to Json error, message: " + e.getMessage());
            }
        }
    }

    @Component
    @ReadingConverter
    public static class CollectionReadConverter implements Converter<String, Collection<?>> {

        @Override
        public Collection<?> convert(@NonNull String source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readValue(source, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to collection error",
                        "Object Json converter to Collection error, message: " + e.getMessage());
            }
        }
    }

    @Component
    @WritingConverter
    public static class CollectionWriteConverter implements Converter<Collection<?>, String> {

        @Override
        public String convert(@NonNull Collection<?> source) {
            try {
                return ContextUtils.OBJECT_MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Collection converter to Json error",
                        "Collection converter to Json error, message: " + e.getMessage());
            }
        }
    }
}