package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupAuthorityRequest extends GroupAuthority implements Serializable {

    private Set<String> authorities;

    public GroupAuthority toGroupAuthority() {
        return BeanUtils.copyProperties(this, GroupAuthority.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("authorities"));
    }

}