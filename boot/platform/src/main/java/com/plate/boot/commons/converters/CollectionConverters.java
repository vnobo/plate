package com.plate.boot.commons.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plate.boot.commons.exception.JsonException;
import com.plate.boot.commons.utils.ContextUtils;
import io.r2dbc.postgresql.codec.Json;
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
 * CollectionConverters is a Spring Configuration class responsible for initializing converters
 * that facilitate the transformation between JSON and Java Collection types. It provides
 * ReadingConverter and WritingConverter components to support these conversions seamlessly
 * within a Spring application context, particularly useful in scenarios involving data binding
 * with JSON data sources like databases or web requests.
 *
 * <p>This class implements InitializingBean, ensuring that the converters are properly initialized
 * upon the application's startup sequence.
 *
 * <h3>Components:</h3>
 * <ul>
 *     <li>{@link CollectionReadConverter}: Converts JSON data into a Collection of objects.</li>
 *     <li>{@link CollectionWriteConverter}: Converts a Collection of objects into JSON format.</li>
 * </ul>
 * <p>Both converters utilize {@link ContextUtils#OBJECT_MAPPER}, an ObjectMapper instance configured
 * elsewhere in the application context, to perform the actual JSON serialization and deserialization.
 *
 * @see InitializingBean Spring's InitializingBean interface for callback on bean initialization.
 * @see ReadingConverter Marker interface for converters reading from a MongoDB representation.
 * @see WritingConverter Marker interface for converters writing to a MongoDB representation.
 * @since Typically used in applications requiring data binding with JSON.
 */
@Log4j2
@Configuration(proxyBeanMethods = false)
public class CollectionConverters implements InitializingBean {

    /**
     * Initializes the converters managed by this class. This method is part of the
     * Spring Framework's {@link InitializingBean} contract, ensuring that the converters
     * are properly set up when the Spring container initializes this bean.
     * <p>
     * Logs a message indicating the start of the initialization process for the
     * [CollectionConverters], which is crucial for preparing the application to handle
     * JSON to Collection and vice versa transformations seamlessly.
     *
     * @see InitializingBean for more details on the callback contract for bean initialization.
     */
    @Override
    public void afterPropertiesSet() {
        log.info("Initializing converter [CollectionConverters]...");
    }

    /**
     * CollectionReadConverter is a Spring component and a ReadingConverter that is designed to
     * deserialize JSON data into a Collection of objects. It is part of the CollectionConverters
     * configuration, ensuring seamless integration within a Spring context where conversion between
     * JSON and Java Collections is required, such as when interacting with MongoDB or processing web requests.
     * <p>This converter leverages the OBJECT_MAPPER from ContextUtils to perform JSON deserialization.
     * It defines the conversion logic for transforming a Json representation into a Collection of generic type.
     * <p>In the event of a JsonProcessingException during deserialization, this converter throws a
     * JsonException, encapsulating the error details for further handling.
     *
     * @see ContextUtils For the setup of the OBJECT_MAPPER utilized in deserialization.
     * @see JsonException For the exception type thrown upon JSON processing errors.
     * @see CollectionConverters The parent configuration class managing this converter.
     */
    @Component
    @ReadingConverter
    public static class CollectionReadConverter implements Converter<Json, Collection<?>> {

        /**
         * Converts the given Json source into a Collection of objects.
         * <p>
         * This method utilizes the OBJECT_MAPPER from ContextUtils to deserialize the JSON string
         * represented by the Json source into a Collection of a generic type determined at runtime.
         * In case of a JsonProcessingException during deserialization, a JsonException is thrown
         * with a descriptive error message including the original exception's message.
         *
         * @param source The Json source to be converted into a Collection. Must not be null.
         * @return A Collection of objects resulting from the JSON deserialization.
         * @throws JsonException If an error occurs during the JSON processing or deserialization.
         */
        @Override
        public Collection<?> convert(@NonNull Json source) {
            try {
                return ContextUtils.OBJECT_MAPPER.readValue(source.asString(), new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Object Json converter to Collection error", e);
            }
        }
    }

    /**
     * A converter class designed to serialize collections into their corresponding JSON representations.
     * This class is annotated as a Spring Component and a WritingConverter, indicating its role within
     * the Spring ConversionService for writing data to JSON format.
     *
     * <p>The primary method, {@link #convert(Collection)}, accepts a collection of any type and
     * utilizes Jackson's `ObjectMapper` to serialize the collection into a byte array, which is then
     * encapsulated within a {@link Json} object for further processing or transmission.</p>
     *
     * <p>Note: The method throws a {@link JsonException} if the serialization process encounters an
     * issue, providing a descriptive error message that includes the original exception's details.</p>
     */
    @Component
    @WritingConverter
    public static class CollectionWriteConverter implements Converter<Collection<?>, Json> {

        /**
         * Converts a given collection into its JSON representation.
         * <p>
         * This method takes a collection of any type and attempts to serialize it into a JSON object
         * using the Jackson {@link ObjectMapper}. If the serialization process encounters a
         * {@link JsonProcessingException}, it is caught and rethrown as a {@link JsonException} with a
         * descriptive error message, preserving the original exception's details.
         *
         * @param source The collection to be converted into JSON. Must not be null.
         * @return A {@link Json} object representing the serialized form of the input collection.
         * @throws JsonException If an error occurs during the JSON serialization process.
         */
        @Override
        public Json convert(@NonNull Collection<?> source) {
            try {
                return Json.of(ContextUtils.OBJECT_MAPPER.writeValueAsBytes(source));
            } catch (JsonProcessingException e) {
                throw JsonException.withMsg("Collection converter to Json error", e);
            }
        }
    }
}