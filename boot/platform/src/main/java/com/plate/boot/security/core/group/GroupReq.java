package com.plate.boot.security.core.group;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupReq extends Group {

    public GroupReq securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public Group toGroup() {
        return BeanUtils.copyProperties(this, Group.class);
    }
}