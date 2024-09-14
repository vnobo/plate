package com.plate.auth.commons.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Configures JSON node converters for seamless integration between JSON and database entities.
 * This class sets up write and read converters for {@link ObjectNode} to facilitate
 * persistence and retrieval of JSON data within an ORM framework like Spring Data.
 * It also initializes these converters upon application setup.
 *
 * <p>Contains inner classes that act as converters:
 * <ul>
 *   <li>{@link CollectionAttributeConverter}: Converts an {@link ObjectNode} to a database-compatible string
 *       during persistence and vice versa.</li>
 *   <li>{@link JsonToNodeWriteConverter}: Converts an {@link ObjectNode} to a string for writing into the database.</li>
 *   <li>{@link JsonToNodeReadConverter}: Reads a string from the database and converts it back to an {@link ObjectNode}.</li>
 * </ul>
 *
 * <p>Note: Relies on {@link ContextUtils#OBJECT_MAPPER} for JSON serialization/deserialization operations.
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class JsonNodeConverters implements InitializingBean {

    /**
     * Invoked by a BeanFactory on beans that carry a {@code InitializingBean} interface,
     * after it has set all bean properties supplied in its bean definition.
     * This method allows the bean instance to perform initialization only possible
     * when all bean properties have been set and to throw an exception in the event of misconfiguration.
     * <p>
     * Within this implementation, it logs the initialization status of the [JsonNodeConverters] component,
     * indicating that the converter setup process is being initiated.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [JsonNodeConverters]...");
    }

    /**
     * Converts a collection represented as an {@link ObjectNode} to a string for database storage
     * and vice versa. This class is designed to be used within an ORM framework that supports JPA's
     * {@link AttributeConverter} interface, automatically applying the conversion for entities using
     * {@link ObjectNode} fields.
     *
     * <p>The conversion process handles null and empty collections gracefully, ensuring database
     * integrity and efficient storage by avoiding unnecessary entries for empty collections.
     *
     * <p>During the conversion to a database column, the method {@link #convertToDatabaseColumn(ObjectNode)}
     * utilizes JSON serialization to transform the {@link ObjectNode} into a string representation.
     * Conversely, {@link #convertToEntityAttribute(String)} deserializes the string back into an
     * {@link ObjectNode} when loading from the database.
     *
     * @see AttributeConverter
     * @see jakarta.persistence.Converter
     */
    @Component
    @jakarta.persistence.Converter(autoApply = true)
    public static class CollectionAttributeConverter implements AttributeConverter<ObjectNode, String> {

        /**
         * Converts an {@link ObjectNode} to a JSON formatted string for storage in the database.
         * If the input is empty or null, the method returns null.
         * Throws a {@link JsonException} if there's an issue during JSON processing.
         *
         * @param source The {@link ObjectNode} object to be converted into a JSON string.
         * @return A JSON formatted string representation of the input {@link ObjectNode},
         * or null if the input is empty or null.
         * @throws JsonException If an error occurs during JSON processing.
         */
        @Override
        public String convertToDatabaseColumn(@NonNull ObjectNode source) {
            try {
                if (ObjectUtils.isEmpty(source) || source.isEmpty()) {
                    return null;
                }
                return ContextUtils.OBJECT_MAPPER.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to String error",
                        "Object converter Collection to Json error, message: " + e.getMessage());
            }
        }

        /**
         * Converts a JSON string representation of an {@link ObjectNode} back to its original ObjectNode form.
         * This method is utilized by an ORM framework when reading data from the database into entity attributes.
         *
         * @param value The JSON string as stored in the database, representing an ObjectNode.
         * @return The parsed {@link ObjectNode} corresponding to the input string. Returns null if the input is blank.
         * @throws JsonException If the provided string cannot be converted to an ObjectNode due to a JSON processing issue.
         */
        @Override
        public ObjectNode convertToEntityAttribute(String value) {
            try {
                if (StringUtils.hasLength(value)) {
                    return ContextUtils.OBJECT_MAPPER.readValue(value, new TypeReference<>() {
                    });
                }
                return null;
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Json converter to collection error",
                        "Object converter Json to Collection error, message: " + e.getMessage());
            }

        }
    }

    /**
     * A converter class designed to transform an ObjectNode into its corresponding JSON string representation.
     * This class is annotated as a Spring Component and serves as a WritingConverter,
     * facilitating the serialization process within Spring's type conversion system.
     */
    @Component
    @WritingConverter
    public static class JsonToNodeWriteConverter implements Converter<ObjectNode, String> {
        @Override
        public String convert(@NonNull ObjectNode source) {
            return source.toString();
        }
    }

    /**
     * A converter class designed to transform JSON strings into Jackson {@link ObjectNode} instances.
     * This class is annotated as a Spring component and specifically as a reading converter,
     * allowing it to be automatically detected and used within Spring's data access framework
     * for converting database-stored JSON strings into their respective object node representations.
     *
     * <p>The conversion process involves parsing the input JSON string using Jackson's
     * {@link ObjectMapper#readTree(String)} method to create a tree representation of the JSON,
     * followed by a deep copy operation to ensure the converted {@link ObjectNode} is independent
     * of the original source string, thus preventing unintended side effects from subsequent modifications.
     *
     * <p>In the event of a parsing failure, such as when the source string is not a valid JSON,
     * the method throws a {@link JsonException}, encapsulating the original {@link IOException},
     * to propagate the error in a controlled manner.
     *
     * @see ReadingConverter
     * @see Converter
     * @see ObjectNode
     */
    @Component
    @ReadingConverter
    public static class JsonToNodeReadConverter implements Converter<String, ObjectNode> {
        @Override
        public ObjectNode convert(@NonNull String source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readTree(source).deepCopy();
            } catch (IOException e) {
                throw JsonException.withError(e);
            }
        }
    }
}