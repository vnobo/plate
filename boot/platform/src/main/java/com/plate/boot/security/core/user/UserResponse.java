package com.plate.boot.security.core.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.plate.boot.commons.utils.BeanUtils;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

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

    public static UserResponse withUser(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @JsonIgnore
    @Override
    public String getSearch() {
        return search;
    }

    @JsonIgnore
    @Override
    public String getSecurityCode() {
        return securityCode;
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getQuery() {
        return query;
    }
}