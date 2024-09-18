package com.plate.boot.security.core;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.core.user.User;

import java.io.Serializable;

/**
 * Represents an immutable data structure for auditing user information within an application.
 * This record captures essential details about a user, including a unique code, username, and full name,
 * facilitating tracking and logging user actions.
 * <p>
 * The {@code UserAuditor} provides factory methods to conveniently create instances from various sources
 * like direct inputs, security details, or user entities, ensuring a consistent and secure way of handling user data.
 * </p>
 *
 * @see Serializable The record implements Serializable for potential persistence or distributed computing needs.
 */
public record UserAuditor(String code, String username, String name) implements Serializable {

    /**
     * Creates an instance of {@link UserAuditor} with the specified user details.
     * This method serves as a convenient alternative constructor, promoting the use of named arguments
     * for better code readability and ensuring the creation of fully initialized auditor objects.
     *
     * @param code     The unique code identifying the user within the auditing context.
     * @param username The username associated with the user's account.
     * @param name     The full name of the user for personal identification in audits.
     * @return A new instance of {@link UserAuditor} populated with the provided user details.
     */
    public static UserAuditor of(String code, String username, String name) {
        return new UserAuditor(code, username, name);
    }

    /**
     * Creates a {@link UserAuditor} instance with the specified code and default null values for username and name.
     * <p>
     * This factory method is useful when only the auditor's code is known or relevant, initializing
     * the other fields to null. It promotes the creation of auditor objects in scenarios where
     * partial information is available or when the username and name are not required for a given operation.
     *
     * @param code The unique code identifying the user auditor. Must not be null.
     * @return A new {@link UserAuditor} instance initialized with the given code and null username and name.
     * @throws NullPointerException if the provided code is null.
     */
    public static UserAuditor withCode(String code) {
        return of(code, null, null);
    }

    /**
     * Creates a {@link UserAuditor} instance populated with details extracted from the provided {@link SecurityDetails}.
     * This method facilitates the transformation from security-related information to an auditor entity, primarily used for auditing purposes.
     *
     * @param securityDetails The security details containing the code, username, and name necessary for auditor initialization.
     * @return A new {@code UserAuditor} instance with its fields set according to the provided {@code SecurityDetails}.
     */
    public static UserAuditor withDetails(SecurityDetails securityDetails) {
        return of(securityDetails.getCode(), securityDetails.getUsername(), securityDetails.getName());
    }

    /**
     * Creates a {@link UserAuditor} instance based on the provided {@link User} object.
     * This method extracts the code, username, and name from the given user to initialize a new auditor record.
     *
     * @param user The user object containing the necessary details for auditing purposes.
     * @return A new {@link UserAuditor} instance populated with the user's code, username, and name.
     */
    public static UserAuditor withUser(User user) {
        return of(user.getCode(), user.getUsername(), user.getName());
    }

}