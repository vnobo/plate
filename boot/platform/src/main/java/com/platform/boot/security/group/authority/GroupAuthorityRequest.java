package com.platform.boot.security.group.authority;

import com.platform.boot.commons.utils.BeanUtils;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "权限[authorities]不能为空!")
    private Set<String> authorities;

    public GroupAuthority toGroupAuthority() {
        return BeanUtils.copyProperties(this, GroupAuthority.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("authorities"));
    }

}