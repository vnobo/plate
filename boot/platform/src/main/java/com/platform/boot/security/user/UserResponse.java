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
    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @JsonIgnore
    @Override
    public Boolean getDisabled() {
        return super.getDisabled();
    }

    @JsonIgnore
    @Override
    public Boolean getAccountExpired() {
        return super.getAccountExpired();
    }

    @JsonIgnore
    @Override
    public Boolean getAccountLocked() {
        return super.getAccountLocked();
    }

    @JsonIgnore
    @Override
    public Boolean getCredentialsExpired() {
        return super.getCredentialsExpired();
    }
}