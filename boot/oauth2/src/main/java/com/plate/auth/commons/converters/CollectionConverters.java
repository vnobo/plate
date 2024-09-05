package com.plate.auth.commons.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.plate.auth.commons.exception.JsonException;
import com.plate.auth.commons.utils.ContextUtils;
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
 * CollectionConverters is a Spring configuration class that provides converters to facilitate
 * the serialization and deserialization of {@link Collection} objects into and from JSON strings,
 * primarily for database storage and retrieval operations. This class registers these converters
 * to be automatically applied within the Spring context where applicable.
 *
 * <p>The converters encapsulated within this class are:</p>
 * <ul>
 *   <li>{@link CollectionAttributeConverter}: A JPA AttributeConverter to convert collections to/from JSON strings.</li>
 *   <li>{@link CollectionReadConverter}: A Spring Data MongoDB converter to read JSON strings into collections.</li>
 *   <li>{@link CollectionWriteConverter}: A Spring Data MongoDB converter to write collections into JSON strings.</li>
 * </ul>
 *
 * <p>These converters leverage {@link ContextUtils#OBJECT_MAPPER} for JSON processing.</p>
 *
 * <p><strong>Note:</strong> This class implements {@link InitializingBean} to log an initialization message.</p>
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CollectionConverters implements InitializingBean {

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied all dependencies for this bean. This method allows the bean instance
     * to perform initialization only possible when all bean properties have been set
     * and to throw an exception in the event of misconfiguration.
     * <p>
     * This implementation logs a message indicating the initialization of
     * {@code CollectionConverters}, which provides converters for handling
     * {@link Collection} serialization and deserialization within a Spring context.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [CollectionConverters]...");
    }

    /**
     * A converter class designed to facilitate the persistence of {@link Collection} objects into a database and their retrieval back into Java objects.
     * This converter serializes collections to JSON strings when persisting into a database column and deserializes them back into collections when loading from the database.
     * It is annotated to be automatically applied by JPA for any {@link Collection} fields marked with the appropriate JPA annotations.
     */
    @Component
    @jakarta.persistence.Converter(autoApply = true)
    public static class CollectionAttributeConverter implements AttributeConverter<Collection<?>, String> {

        /**
         * Converts a given Collection object into a JSON formatted string suitable for storage in a database column.
         * This method utilizes the OBJECT_MAPPER from ContextUtils to serialize the Collection.
         *
         * @param source The Collection object to be converted. Must not be null.
         * @return A JSON string representation of the input Collection.
         * @throws JsonException If the serialization process encounters a JsonProcessingException,
         *                      indicating a failure to convert the Collection to a JSON string.
         */
        @Override
        public String convertToDatabaseColumn(@NonNull Collection<?> source) {
            try {
                return ContextUtils.OBJECT_MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to String error",
                        "Object converter Collection to Json error, message: " + e.getMessage());
            }

        }

        /**
         * Converts a JSON string, retrieved from a database column, back into the original Collection object.
         * This method uses the OBJECT_MAPPER from ContextUtils to deserialize the JSON string into a Collection of the appropriate type.
         *
         * @param value The JSON string representation of the Collection that was stored in the database. Must not be null.
         * @return The deserialized Collection object that the input string represents.
         */
        @Override
        public Collection<?> convertToEntityAttribute(String value) {
            try {
                return ContextUtils.OBJECT_MAPPER.readValue(value, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to collection error",
                        "Object converter Json to Collection error, message: " + e.getMessage());
            }
        }
    }

    /**
     * A converter class designed to facilitate the deserialization of JSON strings into Collection objects.
     * This class is annotated as a Spring component and specifically designated as a reading converter,
     * enabling it to be auto-detected and utilized within Spring's data access framework.
     * It leverages Jackson's `ObjectMapper` to parse JSON strings into collections of arbitrary types.
     *
     * <p>Usage Note:
     * The converter expects the input string to be formatted as a valid JSON array.
     * If the source string fails to deserialize due to invalid JSON format,
     * a {@link JsonException} is thrown, providing a descriptive error message.</p>
     *
     * @see Converter Interface that this class implements for type conversion.
     * @see Component Spring annotation marking this class as a managed bean.
     * @see ReadingConverter Spring Data MongoDB annotation indicating this is a read converter.
     */
    @Component
    @ReadingConverter
    public static class CollectionReadConverter implements Converter<String, Collection<?>> {

        /**
         * Converts a JSON formatted string into a Collection object.
         * This method utilizes the Jackson ObjectMapper to deserialize the input string into a Collection type,
         * inferred through a TypeReference.
         *
         * @param source The JSON string that needs to be converted into a Collection.
         *               It must be a valid JSON structure that can be mapped to a Collection type.
         * @return A Collection object deserialized from the input JSON string.
         * @throws JsonException If an error occurs during the JSON processing, a JsonException is thrown
         *                      with a specific error message detailing the failure.
         */
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

    /**
     * A converter class designed to transform collections into their JSON string representation.
     * This class is annotated as a Spring component and designated as a writing converter,
     * facilitating its integration within Spring's data binding and conversion infrastructure.
     * It utilizes the Jackson Object Mapper for the conversion process.
     *
     * <p>Upon invocation of the {@link #convert(Collection)} method, the source collection is serialized
     * into a JSON string. If any exception occurs during the serialization, a custom {@link JsonException}
     * is thrown, encapsulating the original error message to provide a clearer indication of the issue.</p>
     *
     * @implNote The conversion relies on the static reference to `ContextUtils.OBJECT_MAPPER`,
     * expecting that the `ContextUtils` class provides a pre-configured instance of `ObjectMapper`.
     *
     * @see Converter
     * @see WritingConverter
     * @see JsonProcessingException
     */
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