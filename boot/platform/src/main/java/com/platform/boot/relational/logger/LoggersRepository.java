package com.platform.boot.relational.logger;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface LoggersRepository extends R2dbcRepository<Logger, Long> {

    /**
     * 根据创建时间删除记录并返回删除的Logger实体流。
     *
     * @param createdTime 限定的创建时间，删除所有创建时间早于该时间的记录。
     * @return Flux<Logger> 返回一个Flux流，包含所有被删除的Logger实体。
     */
    Mono<Long> deleteByCreatedTimeBefore(LocalDateTime createdTime);
}