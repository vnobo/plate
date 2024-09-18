package com.plate.boot.security.core.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a response object for user data, extending the base User class.
 * This class provides a JSON-friendly version of the User entity, with specific
 * handling for the password field to ensure it is not exposed in responses.
 *
 * <p><strong>NOTE:</strong> The getPassword method is overridden to be ignored during JSON serialization.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserResponse extends User {

    /**
     * Retrieves the password associated with the user.
     * This method is overridden to be ignored during JSON serialization, ensuring the password
     * is not exposed in API responses for security reasons.
     *
     * @return The password of the user, internally accessible but not exposed publicly.
     */
    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }

}