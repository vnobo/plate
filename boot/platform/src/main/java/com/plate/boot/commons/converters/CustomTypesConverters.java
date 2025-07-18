package com.plate.boot.commons.converters;

import com.plate.boot.commons.utils.MethodType;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CustomTypesConverters implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("CustomTypesConverters afterPropertiesSet called, initializing custom converters...");
    }

    @Component
    @ReadingConverter
    public static class MethodTypeReadConverter implements Converter<String, MethodType> {
        @Override
        public MethodType convert(@org.springframework.lang.NonNull String source) {
            return MethodType.valueOf(source);
        }
    }

    @Component
    @WritingConverter
    public static class MethodTypeWriteConverter implements Converter<MethodType, String> {
        @Override
        public String convert(@org.springframework.lang.NonNull MethodType source) {
            return source.name();
        }
    }

}
