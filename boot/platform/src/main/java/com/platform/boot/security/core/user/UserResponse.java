package com.platform.boot.security.core.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.security.core.UserAuditor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserResponse extends User {

    @JsonIgnore
    @Override
    public String getPhone() {
        return super.getPhone();
    }

    @JsonIgnore
    @Override
    public String getEmail() {
        return super.getEmail();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return super.getUsername();
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

    @JsonIgnore
    @Override
    public JsonNode getExtend() {
        return super.getExtend();
    }

    @JsonIgnore
    @Override
    public LocalDateTime getLoginTime() {
        return super.getLoginTime();
    }

    @JsonIgnore
    @Override
    public UserAuditor getCreator() {
        return super.getCreator();
    }

    @JsonIgnore
    @Override
    public UserAuditor getUpdater() {
        return super.getUpdater();
    }

    @JsonIgnore
    @Override
    public LocalDateTime getUpdatedTime() {
        return super.getUpdatedTime();
    }

    @JsonIgnore
    @Override
    public LocalDateTime getCreatedTime() {
        return super.getCreatedTime();
    }
}