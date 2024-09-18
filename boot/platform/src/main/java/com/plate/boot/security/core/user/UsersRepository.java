package com.plate.boot.security.core.user;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

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
    Mono<User> findByCode(String code);

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

    /**
     * Changes the password for a user with a case-insensitive matching username.
     *
     * @param username The username of the user whose password is to be changed, matched case-insensitively.
     * @param newPassword The new password to set for the user.
     * @return A Mono emitting the number of rows affected by the update operation, which is typically 1 if the user is found and the password is updated successfully.
     */
    @Modifying
    @Query("update se_users set password=:newPassword where username ilike :username")
    Mono<Long> changePassword(String username, String newPassword);

}