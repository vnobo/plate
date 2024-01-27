package com.platform.boot.security.core.user;

import com.platform.boot.commons.query.CriteriaUtils;
import com.platform.boot.commons.query.ParamSql;
import com.platform.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;


/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserRequest extends User {

    private Map<String, Object> query;

    private String securityCode;

    public UserRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }
    public User toUser() {
        return BeanUtils.copyProperties(this, User.class);
    }

    public ParamSql bindParamSql() {
        return CriteriaUtils.buildParamSql(this, List.of(), null);
    }
}