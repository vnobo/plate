package com.plate.boot.commons.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Optional;

/**
 * AbstractService serves as a foundation for implementing service layers with common functionalities,
 * such as caching and initialization routines. It integrates with Spring's cache management and
 * initialization callbacks, providing a structured approach to service layer development.
 *
 * <p>This abstract class requires implementations to define business logic while benefiting from
 * built-in support for an {@link ObjectMapper} for JSON serialization/deserialization and a
 * {@link CacheManager} for managing caching operations. It enforces the setup of a dedicated cache
 * per extending service class, enhancing performance through result caching.
 * <p>Notable features include:
 * <ul>
 *   <li>Automatic cache initialization upon bean creation.</li>
 *   <li>Methods to execute database queries with integrated caching capabilities.</li>
 *   <li>Support for dependency injection, including an {@link ObjectMapper} and a {@link CacheManager}.</li>
 * </ul>
 * Extending classes should override or implement specific business logic methods as needed,
 * leveraging the provided utilities and infrastructure setup.
 *
 * @see InitializingBean Interface to be implemented by beans that need to react once all their properties
 * have been set by a BeanFactory.
 */
@Log4j2
public abstract class AbstractService implements InitializingBean {

    /**
     * Cache instance used to store and retrieve data within the service.
     * Initialized through the {@link #initializingCache(String)} method with a cache name defaulting to the class name concatenated with ".cache".
     * This cache is cleared upon initialization and is essential for providing temporary storage that accelerates data access.
     */
    protected Cache cache;

    /**
     * Manages the caching infrastructure for the service.
     * This field holds an instance of a CacheManager which is responsible for creating,
     * retrieving, and managing caches used throughout the application to improve performance by storing frequently accessed data.
     * It is initialized via dependency injection and utilized in the {@link #initializingCache(String)} method to obtain or create specific caches.
     */
    protected CacheManager cacheManager;

    /**
     * Provides an instance of Jackson's {@link ObjectMapper}, which is a powerful JSON processor
     * capable of converting Java objects into their JSON representation and vice versa.
     * This field is automatically wired by Spring's dependency injection, ensuring that it is properly configured
     * and ready to handle complex object mappings and custom serialization/deserialization scenarios within the service layer.
     *
     * <p>Usage of this {@link ObjectMapper} should adhere to best practices regarding JSON processing within the application,
     * including proper handling of date formats, polymorphism, and any custom serializers/deserializers defined
     * for application-specific data structures.
     */
    protected ObjectMapper objectMapper;

    /**
     * Sets the ObjectMapper instance to be used by this component.
     *
     * @param objectMapper The ObjectMapper object which provides functionality for JSON serialization and deserialization.
     */
    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Sets the CacheManager instance to be used by this component.
     *
     * @param cacheManager The CacheManager instance to manage caches.
     */
    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Initializes the cache with the specified name.
     * If the cache manager is set, it attempts to retrieve the cache from the manager;
     * otherwise, it creates a new ConcurrentMapCache with the given name.
     * After initialization, the cache is cleared and a debug log message is generated indicating
     * the cache's class name and name.
     *
     * @param cacheName The name of the cache to initialize.
     */
    protected void initializingCache(String cacheName) {
        this.cache = Optional.ofNullable(this.cacheManager).map(manager -> manager.getCache(cacheName))
                .orElse(new ConcurrentMapCache(cacheName));
        this.cache.clear();
        log.debug("Initializing provider [{}] cache names: {}",
                this.cache.getNativeCache().getClass().getSimpleName(), this.cache.getName());
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied all dependencies for this bean. This method allows the bean instance
     * to perform initialization only possible when all bean properties have been set
     * and to throw an exception in the event of misconfiguration.
     *
     * <p>This implementation initializes the cache associated with this component,
     * using the bean's class name concatenated with ".cache" as the cache identifier.
     */
    @Override
    public void afterPropertiesSet() {
        initializingCache(this.getClass().getName().concat(".cache"));
    }
}