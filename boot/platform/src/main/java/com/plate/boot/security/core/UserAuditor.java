package com.plate.boot.security.core;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.core.user.User;

import java.io.Serializable;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public record UserAuditor(String code, String username, String name) implements Serializable {

    public static UserAuditor of(String code, String username, String name) {
        return new UserAuditor(code, username, name);
    }

    public static UserAuditor withCode(String code) {
        return of(code, null, null);
    }

    public static UserAuditor withDetails(SecurityDetails securityDetails) {
        return of(securityDetails.getCode(), securityDetails.getUsername(), securityDetails.getName());
    }

    public static UserAuditor withUser(User user) {
        return of(user.getCode(), user.getUsername(), user.getName());
    }

}