package com.platform.boot.security;

import com.platform.boot.security.user.User;
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

    private String code;
    private String username;
    private String name;

    public static UserAuditor withDetails(SecurityDetails securityDetails) {
        UserAuditor userAuditor = UserAuditor.withCode(securityDetails.getCode());
        userAuditor.setName(securityDetails.getName());
        userAuditor.setUsername(securityDetails.getUsername());
        return userAuditor;
    }

    public static UserAuditor withCode(String code) {
        UserAuditor userAuditor = new UserAuditor();
        userAuditor.setCode(code);
        return userAuditor;
    }

    public UserAuditor withUser(User user) {
        this.code = user.getCode();
        this.username = user.getUsername();
        this.name = user.getName();
        return this;
    }

}