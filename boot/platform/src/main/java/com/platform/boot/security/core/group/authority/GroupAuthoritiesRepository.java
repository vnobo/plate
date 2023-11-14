package com.platform.boot.security.core.group.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupAuthoritiesRepository extends R2dbcRepository<GroupAuthority, Integer> {
    /**
     * This method deletes records from the database based on the provided authority value.
     *
     * @param authorities the authority value to delete by
     * @return a Mono<Integer> indicating the number of records deleted
     */
    Mono<Integer> deleteByAuthorityIn(Collection<String> authorities);

    /**
     * This method deletes records from the database based on the provided authority value.
     *
     * @param groupCode the groupCode
     * @return a Flux<GroupAuthority>
     */
    Flux<GroupAuthority> findByGroupCode(String groupCode);
}