package com.plate.boot.security.core.group;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Group Repository Interface
 * Provides data access operations for Group entities
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupsRepository extends R2dbcRepository<Group, Integer> {

    /**
     * Find group by code
     *
     * @param code the unique code of the group to find
     * @return Mono containing the found Group or empty if not found
     */
    Mono<Group> findByCode(UUID code);
}