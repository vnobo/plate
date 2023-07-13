package com.platform.boot.security.tenant.member;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.BeanUtils;
import com.platform.boot.security.tenant.Tenant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantMemberOnly extends TenantMember {

    private String tenantName;

    private JsonNode tenantExtend;

    public static TenantMemberOnly withTenantMember(TenantMember tenantMember) {
        return BeanUtils.copyProperties(tenantMember, TenantMemberOnly.class);
    }

    public TenantMemberOnly tenant(Tenant tenant) {
        this.setTenantName(tenant.getName());
        this.setTenantExtend(tenant.getExtend());
        return this;
    }

}