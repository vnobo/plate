package com.plate.boot.security.core.group;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.CriteriaUtils;
import com.plate.boot.commons.utils.query.ParamSql;
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
public class GroupRequest extends Group {

    private Map<String, Object> query;

    private String securityCode;

    public GroupRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public Group toGroup() {
        return BeanUtils.copyProperties(this, Group.class);
    }

    public ParamSql bindParamSql() {
        return CriteriaUtils.buildParamSql(this, List.of(), null);
    }
}