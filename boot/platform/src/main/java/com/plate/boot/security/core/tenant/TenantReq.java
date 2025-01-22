package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantReq extends Tenant {

    private Map<String, Object> query;

    public Tenant toTenant() {
        return BeanUtils.copyProperties(this, Tenant.class);
    }

}