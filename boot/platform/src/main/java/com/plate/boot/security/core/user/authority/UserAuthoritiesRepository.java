package com.plate.boot.security.core.user.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing UserAuthority entities.
 * This interface extends R2dbcRepository to provide reactive CRUD operations for UserAuthority entities.
 * <p>
 * The class is annotated with \@author to indicate the author of the code.
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface UserAuthoritiesRepository extends R2dbcRepository<UserAuthority, Integer> {

    /**
     * Finds a UserAuthority entity by userCode and authority.
     *
     * @param userCode  the userCode to search by
     * @param authority the authority to search by
     * @return a Mono emitting the UserAuthority entity if found, or an empty Mono if not found
     */
    Mono<UserAuthority> findByUserCodeAndAuthority(UUID userCode, String authority);

    /**
     * Deletes records from the database based on the provided authority values.
     *
     * @param authorities the list of authority values to delete by
     * @return a Mono emitting the number of records deleted
     */
    Mono<Integer> deleteByAuthorityIn(List<String> authorities);

    /**
     * Deletes records from the database based on the provided userCode.
     *
     * @param userCode the userCode to delete by
     * @return a Mono emitting the number of records deleted
     */
    Mono<Integer> deleteByUserCode(UUID userCode);

}