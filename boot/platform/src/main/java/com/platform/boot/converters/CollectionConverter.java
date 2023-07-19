package com.platform.boot.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.platform.boot.commons.annotation.exception.JsonException;
import com.platform.boot.commons.utils.ContextUtils;
import io.r2dbc.postgresql.codec.Json;
import lombok.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Configuration(proxyBeanMethods = false)
public class CollectionConverter {

    @Component
    @ReadingConverter
    public static class CollectionReadConverter implements Converter<Json, Collection<?>> {

        @Override
        public Collection<?> convert(@NonNull Json source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readValue(source.asString(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("序列化数据Json为Set类型错误,信息: " + e.getMessage());
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
                throw JsonException.withMsg("序列化数据Json为Set类型错误,信息: " + e.getMessage());
            }
        }
    }
}