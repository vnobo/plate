package com.plate.authorization.security.core.user;

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

    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }

}