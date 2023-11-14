package com.platform.boot.security.core.user.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface UserAuthoritiesRepository extends R2dbcRepository<UserAuthority, Integer> {

    /**
     * This method deletes records from the database based on the provided authority value.
     *
     * @param authorities the authority value to delete by
     * @return a Mono<Integer> indicating the number of records deleted
     */
    Mono<Integer> deleteByAuthorityIn(List<String> authorities);

}