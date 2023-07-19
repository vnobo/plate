package com.platform.boot.security.group;

import com.platform.boot.commons.utils.BeanUtils;
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
public class GroupRequest extends Group {

    private String securityCode;

    public GroupRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public GroupRequest id(Integer id) {
        this.setId(id);
        return this;
    }

    public Group toGroup() {
        return BeanUtils.copyProperties(this, Group.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("securityCode"));
    }
}