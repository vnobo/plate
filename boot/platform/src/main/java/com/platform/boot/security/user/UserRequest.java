package com.platform.boot.security.user;

import com.platform.boot.commons.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;

import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRequest extends User {

    private String securityCode;

    public UserRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public User toUser() {
        return BeanUtils.copyProperties(this, User.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("securityCode"));
    }

}