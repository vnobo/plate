package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;


/**
 * Represents a user request, extending the base User class with additional attributes specific to requests.
 * It includes a from map for parameters and a security code, offering fluent method for setting the security code.
 * This class also provides utility methods to convert to a plain User instance and to bind request parameters into a SQL from fragment.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserReq extends User {

    /**
     * Sets the security code for the user request and returns the updated UserReq instance.
     * This method allows for method chaining in a fluent API style.
     *
     * @param securityCode The security code to be set for the user request.
     * @return The UserReq instance with the updated security code.
     */
    public UserReq securityCode(UUID securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    /**
     * Converts the UserReq instance into a User instance by copying all properties.
     * This is useful when you need to separate concerns between a request object and a domain object.
     *
     * @return A new User instance with properties copied from this UserReq.
     */
    public User toUser() {
        return BeanUtils.copyProperties(this, User.class);
    }

}