package com.platform.boot.security.core.group;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupsRepository extends R2dbcRepository<Group, Integer> {
    /**
     * get Group by code
     *
     * @param code Group code
     * @return Group result
     */
    Mono<Group> findByCode(String code);
}