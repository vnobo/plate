package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;

import java.io.Serializable;
import java.util.Set;

/**
 * Group Authority Request DTO
 * Data Transfer Object for group authority operations, extending GroupAuthority entity.
 * This class includes additional fields and methods necessary for handling group authority requests,
 * particularly for batch operations on authorities.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupAuthorityReq extends GroupAuthority implements Serializable {

    /**
     * Set of authority strings for batch operations
     */
    private Set<String> authorities;

    /**
     * Converts this request object to a GroupAuthority entity
     *
     * @return a new GroupAuthority instance with properties copied from this request
     */
    public GroupAuthority toGroupAuthority() {
        return BeanUtils.copyProperties(this, GroupAuthority.class);
    }

    /**
     * Generates a Criteria object based on the request's filterable properties,
     * excluding the authorities field from the criteria generation
     *
     * @return Criteria object for structured querying
     */
    public Criteria toCriteria() {
        return criteria(Set.of("authorities"));
    }

}