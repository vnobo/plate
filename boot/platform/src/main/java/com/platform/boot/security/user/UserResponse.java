package com.platform.boot.security.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserResponse extends User {
    /**
     * UserResponse.java
     * <p>
     * This file contains the code for the UserResponse class.
     * It is written in the Java programming language.
     * The code overrides the getPassword() method from the superclass
     * and uses the @JsonIgnore annotation to indicate that the password
     * should be ignored during serialization and deserialization.
     */
    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }
}