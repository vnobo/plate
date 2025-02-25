package com.plate.boot.security.core.user;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interface for accessing and manipulating user data within the repository.
 * This extends the R2dbcRepository, providing reactive CRUD operations for User
 * entities with a primary key of type Long.
 */
public interface UsersRepository extends R2dbcRepository<User, Long> {
    /**
     * Retrieves a User entity based on the provided code.
     *
     * @param code The unique code used to identify the user.
     * @return A Mono emitting the User entity if found, or an empty Mono otherwise.
     */
    Mono<User> findByCode(UUID code);

    /**
     * Finds a user by their username.
     *
     * @param username The username of the user to find.
     * @return A Mono emitting the User if found, or an empty Mono otherwise.
     */
    Mono<User> findByUsername(String username);

    /**
     * Checks if a user with the given username, case-insensitively, exists in the repository.
     *
     * @param username The username to check for existence, case-insensitively.
     * @return A Mono emitting {@code true} if a user with the specified username exists, {@code false} otherwise.
     */
    Mono<Boolean> existsByUsernameIgnoreCase(String username);

}