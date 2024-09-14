package com.plate.auth.commons.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * AbstractService serves as a foundation for implementing service layers within an application,
 * encapsulating common functionality and providing a structure for dependency injection and initialization.
 * It integrates with Spring Framework features, ensuring that services are properly set up and ready to use.
 *
 * <p>This abstract class offers the following core functionalities:</p>
 * <ul>
 *   <li>Automatically configures an {@link ObjectMapper} instance for JSON serialization/deserialization.</li>
 *   <li>Implements {@link InitializingBean} to define initialization logic after all properties are set.</li>
 *   <li>Logs debug information upon initialization, indicating the specific service class being initialized.</li>
 * </ul>
 *
 * <p>Subclasses should extend this class and may override {@link #afterPropertiesSet()}
 * to include custom initialization steps, ensuring they call `super.afterPropertiesSet()` to maintain base behavior.</p>
 *
 * <p>Dependencies like {@link JdbcTemplate}, {@link NamedParameterJdbcTemplate}, and {@link ConversionService}
 * can be injected via respective setter methods (not shown directly here), following Spring's dependency injection principles.</p>
 */
@Log4j2
public abstract class AbstractService implements InitializingBean {

    /**
     * Provides an instance of {@link ObjectMapper} configured for JSON serialization and deserialization.
     * This object is essential for converting Java objects to and from JSON format.
     * It is automatically configured and injected into subclasses of {@link AbstractService},
     * ensuring that service layers have the capability to handle JSON data processing consistently.
     * <p>
     * Subclasses can utilize this {@code objectMapper} directly to perform JSON operations without needing
     * to manage the configuration themselves.
     */
    protected ObjectMapper objectMapper;

    /**
     * Sets the {@link ObjectMapper} instance to be used for JSON serialization and deserialization.
     * This method is typically called by the Spring framework to inject a pre-configured ObjectMapper bean.
     *
     * @param objectMapper The ObjectMapper instance to be set, which should be configured for the application's needs.
     */
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Invoked by the Spring IoC container after all properties
     * of this bean have been set. This method is an implementation
     * of the {@link InitializingBean} interface, allowing for custom
     * initialization logic to be executed at the appropriate time
     * in the bean lifecycle. It logs a debug message indicating
     * the name of the initializing provider class.
     * <p>
     * Subclasses may override this method but should call
     * `super.afterPropertiesSet()` to maintain the base
     * class's initialization behavior.
     */
    @Override
    public void afterPropertiesSet() {
        log.debug("Initializing provider names: {}", this.getClass().getName());
    }
}