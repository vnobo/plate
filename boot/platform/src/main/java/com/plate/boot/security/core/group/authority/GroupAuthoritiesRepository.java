package com.plate.boot.security.core.group.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
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
     * Query permission information based on the group code.
     * <p>
     * This method retrieves the permission groups associated with the specified group code. It allows the system to dynamically determine the permissions a user has based on their group membership. This is crucial for implementing Role-Based Access Control (RBAC).
     *
     * @param groupCode The unique identifier code of the group. This parameter is the key criterion for filtering permission groups.
     * @return Returns a Flux object containing the GroupAuthority entities that meet the criteria. Flux is a reactive stream that allows for non-blocking processing of asynchronous data sequences.
     */
    Flux<GroupAuthority> findByGroupCode(UUID groupCode);
}