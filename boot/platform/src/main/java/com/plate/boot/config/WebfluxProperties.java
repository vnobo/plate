package com.plate.boot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for web settings.
 * <p>
 * This class binds properties prefixed with `spring.webflux.properties` from the application's configuration.
 * It includes settings for maximum and default page sizes, as well as path prefixes.
 * </p>
 *
 * <p>
 * Example configuration:
 * <pre>
 * spring.webflux.properties.maxPageSize=100
 * spring.webflux.properties.defaultPageSize=25
 * spring.webflux.properties.pathPrefixes[0].path=/api
 * spring.webflux.properties.pathPrefixes[0].basePackage=com.example.api
 * </pre>
 * </p>
 *
 * <p>
 * Usage:
 * <pre>
 * &#64;Autowired
 * private WebfluxProperties webProperties;
 * </pre>
 * </p>
 *
 * <p>
 * Author: <a href="https://github.com/vnobo">Alex Bob</a>
 * </p>
 */
@Data
@ConfigurationProperties(prefix = "spring.webflux.properties")
public class WebfluxProperties {

    /**
     * The maximum page size allowed.
     * Default value is 100.
     */
    private int maxPageSize = 100;

    /**
     * The default page size.
     * Default value is 25.
     */
    private int defaultPageSize = 25;

    /**
     * The default API version.
     * Default value is "v1".
     */
    private String defaultApiVersion = "v1";

    /**
     * The supported API versions.
     * Default value is "v1".
     */
    private String[] supportedVersions = {"v1"};
    /**
     * Route definition list, used to configure the path prefix mapping for WebFlux.
     * <p>
     * Each route definition includes the mapping between a path prefix and the corresponding base package, used to route requests under a specific path to handlers in the specified package.
     * Example configuration:
     * - path: /api/v1 will route requests under this path to handlers in the com.example.api.v1 package.
     * - path: /static will route requests under this path to handlers in the com.example.static package.
     */
    private List<RouteDefinition> pathPrefixes = new ArrayList<>();

    /**
     * Internal class for route definition, used to bind configuration properties
     * <p>
     * Follows Spring Boot's relaxed binding rules, supporting the following configuration formats:
     * plate.webflux.path-prefixes[0].path=/api
     * plate.webflux.path-prefixes[0].base-package=com.example.api
     */
    @Data
    static class RouteDefinition {
        /**
         * The prefix of the request path, used to match the starting part of the request path
         * Example: "/api" will match all requests starting with /api
         */
        private String path;

        /**
         * The base Java package path to scan for request handlers prefixed by this path.
         * This package should contain components annotated with @Controller or @RestController.
         */
        private String basePackage;
    }

}