package com.platform.boot.security.core.tenant;

import com.platform.boot.commons.query.ParamSql;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantRequest extends Tenant implements Serializable {

    private Map<String, Object> query;

    private String securityCode;

    public TenantRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public Tenant toTenant() {
        return BeanUtils.copyProperties(this, Tenant.class);
    }

    public ParamSql bindParamSql() {
        return CriteriaUtils.buildParamSql(this, List.of(), null);
    }
}