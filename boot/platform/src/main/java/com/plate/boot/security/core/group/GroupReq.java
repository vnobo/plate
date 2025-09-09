package com.plate.boot.security.core.group;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Group Request DTO
 * Data Transfer Object for group operations, extends Group entity to provide additional request-specific functionality
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupReq extends Group {

    /**
     * Set the security code for this group request
     *
     * @param securityCode the security code to set
     * @return the current GroupReq instance for method chaining
     */
    public GroupReq securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    /**
     * Convert this request object to a Group entity
     *
     * @return a new Group instance with properties copied from this request
     */
    public Group toGroup() {
        return BeanUtils.copyProperties(this, Group.class);
    }
}