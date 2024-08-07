package com.plate.boot.commons.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
    @ReadingConverter
    public static class CollectionReadConverter implements Converter<Json, Collection<?>> {

        @Override
        public Collection<?> convert(@NonNull Json source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readValue(source.asString(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to collection error",
                        "Object Json converter to Collection error, message: " + e.getMessage());
            }
        }
    }

    @Component
    @WritingConverter
    public static class CollectionWriteConverter implements Converter<Collection<?>, Json> {

        @Override
        public Json convert(@NonNull Collection<?> source) {
            try {
                return Json.of(ContextUtils.OBJECT_MAPPER.writeValueAsBytes(source));
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Collection converter to Json error",
                        "Collection converter to Json error, message: " + e.getMessage());
            }
        }
    }
}