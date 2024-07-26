package com.plate.boot.relational.logger;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface LoggersRepository extends R2dbcRepository<Logger, Long> {

    /**
     * 根据创建时间删除数据
     * 此方法用于删除数据库中所有创建时间早于指定时间的数据。它使用Reactive Streams API中的Mono类型来表示操作的结果，
     * 即删除操作影响的行数。这种方法适用于那些需要根据时间戳进行批量删除操作的场景，例如定期清理过期数据。
     *
     * @param createdTime 一个LocalDateTime对象，表示需要删除的数据的创建时间上限。
     * @return 返回一个Mono对象，该对象在处理完成后将包含被删除行的数量。这允许调用者知道操作的影响程度。
     */
    Mono<Long> deleteByCreatedTimeBefore(LocalDateTime createdTime);
}