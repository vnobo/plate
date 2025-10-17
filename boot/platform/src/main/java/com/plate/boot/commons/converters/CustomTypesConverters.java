package com.plate.boot.commons.converters;

import com.plate.boot.relational.MethodType;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Custom Types Converters Configuration
 * Provides custom type converters for the application, specifically for converting MethodType enums
 * to and from String representations. This class implements InitializingBean to perform
 * initialization logging when the bean is created.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CustomTypesConverters implements InitializingBean {

    /**
     * Called after all properties are set on this bean.
     * Logs a debug message indicating that custom converters are being initialized.
     *
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("CustomTypesConverters afterPropertiesSet called, initializing custom converters...");
    }

    /**
     * Method Type Read Converter
     * Converts String values to MethodType enum values during data reading operations.
     * This converter is annotated with @ReadingConverter to indicate it's used for reading data.
     */
    @Component
    @ReadingConverter
    public static class MethodTypeReadConverter implements Converter<@NonNull String, @NonNull MethodType> {
        /**
         * Converts a String source value to a MethodType enum value
         *
         * @param source the String value to convert, must not be null
         * @return the corresponding MethodType enum value
         */
        @Override
        public MethodType convert(@NonNull String source) {
            return MethodType.valueOf(source);
        }
    }

    /**
     * Method Type Write Converter
     * Converts MethodType enum values to String values during data writing operations.
     * This converter is annotated with @WritingConverter to indicate it's used for writing data.
     */
    @Component
    @WritingConverter
    public static class MethodTypeWriteConverter implements Converter<@NonNull MethodType, @NonNull String> {
        /**
         * Converts a MethodType source value to a String representation
         *
         * @param source the MethodType value to convert, must not be null
         * @return the String representation of the MethodType
         */
        @Override
        public String convert(@NonNull MethodType source) {
            return source.name();
        }
    }

}
