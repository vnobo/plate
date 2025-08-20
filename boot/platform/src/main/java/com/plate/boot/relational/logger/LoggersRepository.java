package com.plate.boot.relational.logger;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Defines the repository interface for interacting with logger entities within a reactive environment.
 * Extending from R2dbcRepository, this interface inherits CRUD operations and adds a custom method
 * to delete log records based on their creation time, facilitating efficient cleanup and management
 * of log data.
 *
 * <p>This repository is designed specifically to work with the {@link Logger} entity and provides
 * a reactive approach to data access, leveraging Project Reactor's Mono and Flux types for asynchronous,
 * non-blocking interactions with the underlying data store.</p>
 *
 * <p>Notably, it includes:</p>
 * <ul>
 *   <li>{@link #deleteByCreatedTimeBefore(LocalDateTime)}: A method to remove outdated log entries
 *       based on the specified creation time threshold.</li>
 * </ul>
 *
 * <p>Usage of this repository promotes scalable and responsive logging systems, particularly in
 * scenarios toSql high throughput and low latency are critical.</p>
 *
 * @see R2dbcRepository for the base repository functionality provided.
 * @see Logger for the entity this repository manages.
 */
public interface LoggersRepository extends R2dbcRepository<Logger, Long> {

    /**
     * Deletes all log records from the database whose creation time is before the specified timestamp.
     *
     * @param createdTime The threshold LocalDateTime before which log records should be deleted.
     * @return A Mono emitting the number of log records deleted upon successful execution.
     */
    Mono<Long> deleteByCreatedAtBefore(LocalDateTime createdTime);
}