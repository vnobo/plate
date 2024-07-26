package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.CriteriaUtils;
import com.plate.boot.commons.utils.query.ParamSql;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantRequest extends Tenant {

    private Map<String, Object> query;

    public Tenant toTenant() {
        return BeanUtils.copyProperties(this, Tenant.class);
    }

    public ParamSql bindParamSql() {
        return CriteriaUtils.buildParamSql(this, List.of("securityCode"), null);
    }
}