package com.platform.boot.relational.logger;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface LoggersRepository extends R2dbcRepository<Logger, Long> {
}