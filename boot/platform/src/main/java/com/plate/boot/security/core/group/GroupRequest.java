package com.plate.boot.security.core.group;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
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

    public QueryFragment qSql() {
        return QueryHelper.query(this, List.of(), null);
    }
}