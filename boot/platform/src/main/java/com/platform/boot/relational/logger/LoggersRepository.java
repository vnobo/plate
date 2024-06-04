package com.platform.boot.relational.logger;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface LoggersRepository extends R2dbcRepository<Logger, Long> {

    Mono<Long> deleteByCreatedTimeBefore(LocalDateTime createdTime);
}