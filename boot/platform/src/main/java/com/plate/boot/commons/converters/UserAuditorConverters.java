package com.plate.boot.commons.converters;

import com.plate.boot.security.core.UserAuditor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Configures converters for handling the transformation between {@link UserAuditor} instances and their string representations.
 * This configuration is particularly useful in data persistence layers where type conversion is necessary, such as when storing
 * auditor details as strings in a database column.
 * <p>
 * The class registers two converters:
 * <ul>
 *   <li>{@link UserAuditorWriteConverter}: Converts a {@link UserAuditor} object to its code as a String.</li>
 *   <li>{@link UserAuditorReadConverter}: Converts a String back to a {@link UserAuditor} instance using the code.</li>
 * </ul>
 * These converters are annotated with Spring's {@link Component}, {@link WritingConverter}, and {@link ReadingConverter}
 * to integrate seamlessly with Spring Data's conversion service.
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class UserAuditorConverters implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [UserAuditorConverters]...");
    }

    /**
     * Converts a {@link UserAuditor} object to its code represented as a String.
     * This converter is intended for write operations, facilitating the storage of auditor details
     * in a textual format, typically used within data persistence frameworks where type conversion
     * services are leveraged.
     * <p>
     * The conversion process involves extracting the 'code' attribute from the provided
     * {@link UserAuditor} instance and returning it as a plain String.
     * <p>
     * Part of the {@link UserAuditorConverters} configuration which manages the bidirectional
     * transformations between {@link UserAuditor} and String for read/write operations.
     */
    @Component
    @WritingConverter
    public static class UserAuditorWriteConverter implements Converter<UserAuditor, UUID> {
        /**
         * Converts a {@link UserAuditor} object to its code represented as a String.
         *
         * @param source The {@link UserAuditor} instance to be converted. Must not be null.
         * @return The code of the {@link UserAuditor} as a String.
         * @throws NullPointerException if the source {@link UserAuditor} is null.
         */
        @Override
        public UUID convert(@NonNull UserAuditor source) {
            return source.code();
        }
    }

    /**
     * Converts a String representation of a user auditor code into a {@link UserAuditor} instance.
     * This converter is designed to be utilized during read operations, where auditor information stored
     * as strings (e.g., in a database) needs to be transformed back into a domain object.
     * <p>
     * The conversion logic employs the {@link UserAuditor#withCode(String)} factory method,
     * passing the source string, which typically corresponds to the 'code' attribute of a {@link UserAuditor},
     * to reconstruct the auditor object with default values for username and name.
     * <p>
     * This class is part of the {@link UserAuditorConverters} configuration, complementing the write-side
     * conversion provided by {@link UserAuditorWriteConverter}.
     *
     * @see UserAuditorConverters
     * @see UserAuditorWriteConverter
     * @see UserAuditor#withCode(UUID)
     */
    @Component
    @ReadingConverter
    public static class UserAuditorReadConverter implements Converter<UUID, UserAuditor> {
        /**
         * Converts a given non-null String source representing a user auditor code
         * into a {@link UserAuditor} instance.
         * <p>
         * This method is part of the read conversion process, used to transform auditor codes
         * stored as strings (for example, in a database) back into rich {@link UserAuditor} objects.
         * It delegates the conversion to the {@link UserAuditor#withCode(UUID)} factory method,
         * which reconstructs the auditor with the provided code while setting default null values for
         * the username and name fields.
         *
         * @param source The non-null String containing the user auditor code to convert.
         * @return A {@link UserAuditor} instance created using the provided code.
         * @throws NullPointerException if the source is null.
         */
        @Override
        public UserAuditor convert(@NonNull UUID source) {
            return UserAuditor.withCode(source);
        }
    }
}