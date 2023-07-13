package com.platform.boot.converters;

import com.platform.boot.security.UserAuditor;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class contains converters for UserAuditor objects.
 *
 * @author billb
 */
public final class UserAuditorConverters {
    public static final UserAuditorConverters INSTANCE = new UserAuditorConverters();

    /**
     * Gets a collection of converters.
     *
     * @return a collection of converters
     */
    public Collection<Object> getConverters() {
        List<Object> converters = new ArrayList<>();
        converters.add(UserAuditorWriteConverter.INSTANCE);
        converters.add(UserAuditorReadConverter.INSTANCE);
        return converters;
    }

    @WritingConverter
    private enum UserAuditorWriteConverter implements Converter<UserAuditor, String> {
        INSTANCE;

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

    @ReadingConverter
    private enum UserAuditorReadConverter implements Converter<String, UserAuditor> {
        INSTANCE;

        /**
         * Converts a string to a UserAuditor object.
         *
         * @param source the string to convert
         * @return a UserAuditor object with the given username
         */
        @Override
        public UserAuditor convert(@NonNull String source) {
            return UserAuditor.withUsername(source);
        }
    }
}