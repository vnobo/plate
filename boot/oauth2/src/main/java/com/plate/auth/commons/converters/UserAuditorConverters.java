package com.plate.auth.commons.converters;

import com.plate.auth.security.core.UserAuditor;
import jakarta.persistence.AttributeConverter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class UserAuditorConverters implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [UserAuditorConverters]...");
    }

    @Component
    @jakarta.persistence.Converter(autoApply = true)
    public static class UserAuditorAttributeConverter implements AttributeConverter<UserAuditor,String> {

        @Override
        public String convertToDatabaseColumn(@NonNull UserAuditor source) {
                return  source.code();
        }

        @Override
        public UserAuditor convertToEntityAttribute(String value) {
                return UserAuditor.withCode(value);
        }
    }

    @Component
    @WritingConverter
    public static class UserAuditorWriteConverter implements Converter<UserAuditor, String> {
        @Override
        public String convert(@NonNull UserAuditor source) {
            return source.code();
        }
    }

    @Component
    @ReadingConverter
    public static class UserAuditorReadConverter implements Converter<String, UserAuditor> {
        @Override
        public UserAuditor convert(@NonNull String source) {
            return UserAuditor.withCode(source);
        }
    }
}