package com.plate.boot.relational.logger;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * Logger Response DTO
 * Data Transfer Object for logger operations, extending Logger entity to provide additional
 * response-specific fields such as search rank for text search operations.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class LoggerRes extends Logger {

    /**
     * Text search rank sort
     * Represents the relevance ranking of a log entry when performing text searches,
     * with higher values indicating better matches
     */
    @ReadOnlyProperty
    private Double rank;

}
