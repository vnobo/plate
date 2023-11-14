package com.platform.boot.security.core.user.authority;

import com.platform.boot.commons.utils.BeanUtils;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserAuthorityRequest extends UserAuthority {

    @NotNull(message = "权限[authorities]不能为空!")
    private List<String> authorities;

    public UserAuthority toAuthority() {
        return BeanUtils.copyProperties(this, UserAuthority.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("authorities"));
    }

}