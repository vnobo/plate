package com.platform.boot.converters;

import com.platform.boot.security.UserAuditor;
import com.platform.boot.security.user.User;
import com.platform.boot.security.user.UsersService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Duration;

/**
 * This class contains converters for UserAuditor objects.
 *
 * @author billb
 */
@Configuration(proxyBeanMethods = false)
public class UserAuditorConverters {

    @Component
    @WritingConverter
    public static class UserAuditorWriteConverter implements Converter<UserAuditor, String> {
        /**
         * Converts a UserAuditor object to a string.
         *
         * @param source the UserAuditor object to convert
         * @return the username of the UserAuditor object
         */
        @Override
        public String convert(@NonNull UserAuditor source) {
            return source.getUsername();
        }
    }

    @Component
    @ReadingConverter
    @RequiredArgsConstructor
    public static class UserAuditorReadConverter implements Converter<String, UserAuditor> {

        private final UsersService usersService;

        /**
         * Converts a string to a UserAuditor object.
         *
         * @param source the string to convert
         * @return a UserAuditor object with the given username
         */
        @Override
        public UserAuditor convert(@NonNull String source) {
            UserAuditor userAuditor = UserAuditor.withUsername(source);
            User user = this.usersService.loadByUsername(source)
                    .timeout(Duration.ofSeconds(1)).share().block();
            if (!ObjectUtils.isEmpty(user)) {
                userAuditor.setName(user.getName());
            }
            return userAuditor;
        }
    }
}