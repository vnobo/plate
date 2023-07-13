package com.platform.boot.security;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex bob</a>)
 */
@Data
public class UserAuditor implements Serializable {

    private String username;

    public static UserAuditor withUsername(String username) {
        UserAuditor userAuditor = new UserAuditor();
        userAuditor.setUsername(username);
        return userAuditor;
    }

    public static UserAuditor withDetails(SecurityDetails securityDetails) {
        return UserAuditor.withUsername(securityDetails.getUsername());
    }

}