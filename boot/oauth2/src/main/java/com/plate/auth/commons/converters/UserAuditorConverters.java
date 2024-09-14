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
 * Converts between {@link UserAuditor} instances and their string representations for persistence and retrieval.
 * This class defines converters to facilitate the persistence and reading of {@link UserAuditor} objects into/from
 * database columns that are of string type. It includes converters for both JPA (Java Persistence API) and Spring Data.
 * The class is designed to initialize these converters upon its own setup, ensuring they are ready for use when needed.
 * <p>
 * The converters provided are:
 * <ul>
 *   <li>{@link UserAuditorAttributeConverter}: A JPA AttributeConverter to handle conversion during JPA entity operations.</li>
 *   <li>{@link UserAuditorWriteConverter}: A Spring Data WritingConverter to convert entities to strings for writing to the database.</li>
 *   <li>{@link UserAuditorReadConverter}: A Spring Data ReadingConverter to convert database strings back to entity instances.</li>
 * </ul>
 * All converters use the {@code code} attribute of the {@link UserAuditor} as the basis for conversion, ensuring
 * a consistent serialization/deserialization mechanism across different layers of the application.
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class UserAuditorConverters implements InitializingBean {

    /**
     * Callback method that indicates bean initialization is complete and all properties have been set.
     * This method is implemented from the {@link InitializingBean} interface.
     * It logs an informational message indicating the initialization of the converters within the {@link UserAuditorConverters} class.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [UserAuditorConverters]...");
    }

    /**
     * Converts a {@link UserAuditor} object to its string representation and vice versa for database storage and retrieval.
     * <p>
     * This converter is automatically applied to all {@link UserAuditor} fields marked with JPA annotations,
     * ensuring seamless conversion between the entity attribute and the database column.
     * </p>
     *
     * <b>ConvertToDatabaseColumn:</b><br>
     * Transforms a {@link UserAuditor} instance into its string code equivalent, suitable for storing in the database.
     * <p>
     * <b>ConvertToEntityAttribute:</b><br>
     * Restores a {@link UserAuditor} instance from the string value retrieved from the database column.
     *
     * @see UserAuditor
     * @see AttributeConverter
     */
    @Component
    @jakarta.persistence.Converter(autoApply = true)
    public static class UserAuditorAttributeConverter implements AttributeConverter<UserAuditor, String> {

        /**
         * Converts a {@link UserAuditor} object into its code representation for database storage.
         *
         * @param source The non-null {@link UserAuditor} object to be converted.
         * @return The code attribute of the {@link UserAuditor} as a String, suitable for storing in the database.
         */
        @Override
        public String convertToDatabaseColumn(@NonNull UserAuditor source) {
            return source.code();
        }

        /**
         * Converts a string value from the database into a {@link UserAuditor} entity attribute.
         *
         * @param value The string value representing the UserAuditor code, retrieved from the database.
         * @return A {@link UserAuditor} instance constructed using the provided code value.
         */
        @Override
        public UserAuditor convertToEntityAttribute(String value) {
            return UserAuditor.withCode(value);
        }
    }

    /**
     * Converts a {@link UserAuditor} object to its string representation (code).
     * This converter is specifically designed to be used within the context of data binding and serialization frameworks.
     * It implements the {@link Converter} interface, providing a unidirectional conversion from {@link UserAuditor}
     * to {@link String}.
     */
    @Component
    @WritingConverter
    public static class UserAuditorWriteConverter implements Converter<UserAuditor, String> {
        @Override
        public String convert(@NonNull UserAuditor source) {
            return source.code();
        }
    }

    /**
     * Converts a String representation into a UserAuditor object.
     * This class is specifically designed to be used as a reading converter within a data access framework.
     * It implements the Converter interface to transform a source String value into a UserAuditor instance.
     */
    @Component
    @ReadingConverter
    public static class UserAuditorReadConverter implements Converter<String, UserAuditor> {
        @Override
        public UserAuditor convert(@NonNull String source) {
            return UserAuditor.withCode(source);
        }
    }
}