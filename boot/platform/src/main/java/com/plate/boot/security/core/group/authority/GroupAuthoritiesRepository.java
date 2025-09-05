package com.plate.boot.security.core.group.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupAuthoritiesRepository extends R2dbcRepository<GroupAuthority, Integer> {

    /**
     * Deletes users based on a set of permissions.
     * <p>
     * This function encapsulates an asynchronous deletion operation using Mono. It takes a set of permissions as a parameter and aims to delete all users who possess these permissions.
     * The use of the Reactive programming model enhances the efficiency of concurrent processing and elegantly handles asynchronous data streams.
     *
     * @param authorities The set of permissions of the users to be deleted. Users are located via their permission sets, as permissions are often an effective way to identify users.
     * @return Mono<Integer> Represents the result of the asynchronous operation, returning the number of users deleted. Mono is a Reactive Streams type used to represent a sequence of 0 or 1 elements.
     */
    Mono<Integer> deleteByAuthorityIn(Collection<String> authorities);

    /**
     * Finds users based on a specific group code and permission.
     * <p>
     * This function encapsulates an asynchronous search operation using Flux. It takes a group code and a permission as parameters and aims to find all users who belong to the specified group and have the specified permission.
     * The use of the Reactive programming model enhances the efficiency of concurrent processing and elegantly handles asynchronous data streams.
     *
     * @param groupCode The group code of the users to be searched. Users are located via their group membership, as group codes are often an effective way to identify users.
     * @param authority The permission of the users to be searched. Users are located via their permission sets, as permissions are often an effective way to identify users.
     * @return Flux<GroupAuthority> Represents the result of the asynchronous operation, returning a sequence of users that meet the search criteria. Flux is a Reactive Streams type used to represent a
     * sequence of 0 or more elements.
     * @see Mono
     * @see GroupAuthority
     * @see UUID
     */
    Mono<GroupAuthority> findByGroupCodeAndAuthority(UUID groupCode, String authority);
}