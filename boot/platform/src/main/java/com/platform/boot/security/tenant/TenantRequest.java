package com.platform.boot.security.tenant;

import com.platform.boot.commons.BeanUtils;
import com.platform.boot.commons.utils.CriteriaHolder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantRequest extends Tenant implements Serializable {

    private String securityCode;

    public TenantRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public Tenant toTenant() {
        return BeanUtils.copyProperties(this, Tenant.class);
    }

    public Criteria toCriteria() {

        Criteria criteria = CriteriaHolder.build(this, Set.of("securityCode"));

        if (StringUtils.hasLength(this.securityCode)) {
            criteria = criteria.and("code").like(this.securityCode + "%");
        }

        return criteria;
    }
}