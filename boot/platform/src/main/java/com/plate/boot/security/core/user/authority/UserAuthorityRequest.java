package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.utils.BeanUtils;
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
public class UserAuthorityRequest extends UserAuthority {

    public UserAuthority toAuthority() {
        return BeanUtils.copyProperties(this, UserAuthority.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of());
    }

}