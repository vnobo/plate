package com.platform.boot.security;

import lombok.Data;

import java.io.Serializable;

/**
 * UserAuditor is a class that represents an auditor for user information.
 * It implements the Serializable interface to support serialization.
 * <p>
 * The class has two private fields: username and name, which store the username and name of the user auditor.
 * <p>
 * The class provides a static method withUsername(String username) that
 * creates a new UserAuditor object and sets the username field to the given username.
 * <p>
 * The class also provides a static method withDetails(SecurityDetails securityDetails) that
 * creates a new UserAuditor object and sets the username field to the username from the given SecurityDetails object.
 * <p>
 * This class is typically used in auditing user actions or tracking user information.
 * <p>
 *
 * @author Alex bob (<a href="https://github.com/vnobo">Alex bob</a>)
 */
@Data
public class UserAuditor implements Serializable {

    private String username;

    private String name;

    public static UserAuditor withUsername(String username) {
        UserAuditor userAuditor = new UserAuditor();
        userAuditor.setUsername(username);
        return userAuditor;
    }

    public static UserAuditor withDetails(SecurityDetails securityDetails) {
        return UserAuditor.withUsername(securityDetails.getUsername());
    }

}