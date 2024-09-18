package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;


/**
 * Represents a user request, extending the base User class with additional attributes specific to requests.
 * It includes a query map for parameters and a security code, offering fluent method for setting the security code.
 * This class also provides utility methods to convert to a plain User instance and to bind request parameters into a SQL query fragment.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRequest extends User {

    /**
     * A map containing the query parameters associated with the user request.
     * Each key-value pair represents a parameter name and its corresponding value.
     */
    private Map<String, Object> query;

    /**
     * The security code associated with the user request.
     * This code is typically used for authentication or authorization purposes to ensure secure access.
     */
    private String securityCode;

    /**
     * Sets the security code for the user request and returns the updated UserRequest instance.
     * This method allows for method chaining in a fluent API style.
     *
     * @param securityCode The security code to be set for the user request.
     * @return The UserRequest instance with the updated security code.
     */
    public UserRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    /**
     * Converts the UserRequest instance into a User instance by copying all properties.
     * This is useful when you need to separate concerns between a request object and a domain object.
     *
     * @return A new User instance with properties copied from this UserRequest.
     */
    public User toUser() {
        return BeanUtils.copyProperties(this, User.class);
    }

}