package com.platform.boot.security.tenant.member;

import com.platform.boot.commons.utils.BeanUtils;
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
public class TenantMemberRequest extends TenantMember {

    private Set<String> users;

    private String securityCode;

    public static TenantMemberRequest withUsername(String username) {
        TenantMemberRequest request = new TenantMemberRequest();
        request.setUsername(username);
        return request;
    }

    public static TenantMemberRequest of(String tenantCode, String username) {
        TenantMemberRequest request = withUsername(username);
        request.setTenantCode(tenantCode);
        return request;
    }

    public TenantMemberRequest securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    public TenantMember toMemberTenant() {
        return BeanUtils.copyProperties(this, TenantMember.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("securityCode", "users"));
    }

}