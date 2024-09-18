package com.plate.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.base.BaseEntity;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Represents a database entity for logging activities within a system.
 * This class maps directly to the "se_loggers" table and captures essential details
 * about each logging event, including metadata like tenant, operator, and request specifics.
 *
 * <p>The {@code Logger} class integrates with Spring Data JPA annotations for ORM operations
 * and utilizes Lombok's {@code @Data} annotation for automatic getter/setter generation.
 * It also includes timestamp fields annotated with Spring's auditing annotations to track
 * when log entries are created and last modified.</p>
 *
 * <p>Fields like {@code code}, {@code tenantCode}, and {@code operator} enable granular
 * tracking and filtering of logs by different dimensions. The {@code context} field,
 * stored as a {@link JsonNode}, allows for flexible storage of additional structured data
 * related to the logged event.</p>
 *
 * @see BaseEntity for the base entity contract this class extends, providing common entity behaviors.
 */
@Data
@Table("se_loggers")
public class Logger implements BaseEntity<Long> {

    /**
     * Unique identifier for the logger entity.
     * This field represents the primary key in the database table "se_loggers"
     * and is automatically generated and managed by the persistence framework.
     */
    @Id
    private Long id;

    /**
     * Stores a unique code associated with the logging event.
     * This string represents a distinct identifier that can be used for
     * tracing and referencing specific log entries across the system.
     * It is integral to log management and analysis, supporting operations
     * like filtering and correlation of log data.
     */
    private String code;

    /**
     * The tenant code associated with the logging activity.
     * This string identifier denotes the specific tenant within a multi-tenant environment
     * responsible for or related to the logged event. It facilitates segregation and filtering
     * of logs based on different tenants in the system.
     */
    private String tenantCode;

    /**
     *
     */
    private String operator;

    /**
     * Prefix attached to certain log entries.
     * This string value serves as a marker or category prefix for log messages,
     * allowing easier filtering and classification of logs based on their context or source.
     */
    private String prefix;

    /**
     * The URL associated with the logging event.
     * This string represents the endpoint or resource accessed during the operation being logged.
     */
    private String url;

    /**
     * Represents the HTTP method used during the logging event.
     * This string field captures the type of HTTP request method (e.g., GET, POST, PUT, DELETE)
     * associated with the logged activity, providing insights into the nature of the operation performed.
     */
    private String method;

    /**
     * Represents the status of the logging event.
     * This field stores a string value that indicates the outcome or state of the operation
     * being logged, such as "success", "error", "pending", etc.
     * It is crucial for analyzing and categorizing log entries based on their result.
     */
    private String status;

    /**
     * Stores additional structured data related to the logging event.
     * This field is versatile and can hold various types of information,
     * serialized as a JSON object, enabling the logging of complex data structures
     * without requiring explicit schema definition in the database.
     * It complements the structured fields by offering an extendable mechanism
     * to capture and filter log events based on dynamic content.
     *
     * @see JsonNode for the Jackson library class used to represent JSON data.
     */
    private JsonNode context;

    /**
     * The date and time when the log entry was created.
     * This field is automatically populated by the system upon the creation of a new log record.
     * It is annotated with {@link CreatedDate} to facilitate auditing and tracking of log events over time.
     */
    @CreatedDate
    private LocalDateTime createdTime;

    /**
     * The date and time when this log entry was last updated.
     * This field is automatically managed by the underlying persistence framework
     * and reflects the last modification timestamp of the corresponding database record.
     */
    @LastModifiedDate
    private LocalDateTime updatedTime;
}