package com.plate.boot.security.core.group.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

/**
 * Repository for {@link GroupAuthority} entities, providing reactive data-access operations
 * for managing permissions (authorities) assigned to user groups.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 * @see GroupAuthority
 */
public interface GroupAuthoritiesRepository extends R2dbcRepository<GroupAuthority, Integer> {

    /**
     * Deletes all group authorities whose {@code authority} is contained in the given collection.
     *
     * @param authorities the set of authorities whose group-authority records should be deleted
     * @return a Mono emitting the number of group authorities deleted
     */
    Mono<Integer> deleteByAuthorityIn(Collection<String> authorities);

    /**
     * Finds the group authority for the given group code and authority.
     *
     * @param groupCode the group code to look up
     * @param authority the authority (permission) to look up
     * @return a Mono emitting the matching {@link GroupAuthority}, or empty if none exists
     */
    Mono<GroupAuthority> findByGroupCodeAndAuthority(UUID groupCode, String authority);

    /**
     * Deletes all group authorities for the given group code.
     *
     * @param groupCode the group code whose authorities should be deleted
     * @return a Mono emitting the number of group authorities deleted
     */
    Mono<Integer> deleteByGroupCode(UUID groupCode);
}