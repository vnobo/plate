package com.platform.boot.security.tenant.member;

import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantMemberRequest extends TenantMember {

    private Set<String> users;
    private String username;
    private String securityCode;

    public static TenantMemberRequest withUserCode(String userCode) {
        TenantMemberRequest request = new TenantMemberRequest();
        request.setUserCode(userCode);
        return request;
    }

    public static TenantMemberRequest of(String tenantCode, String userCode) {
        TenantMemberRequest request = withUserCode(userCode);
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
        return criteria(Set.of("securityCode", "users", "username"));
    }

    public String querySql() {
        return """
                select a.*, b.name as tenant_name, b.extend as tenant_extend,c.name as login_name,c.username
                from se_tenant_members a
                inner join se_tenants b on a.tenant_code = b.code
                inner join se_users c on c.code = a.user_code
                """;
    }

    public String countSql() {
        return """
                select count(*) from se_tenant_members a
                inner join se_tenants b on a.tenant_code = b.code
                inner join se_users c on c.code = a.user_code
                """;
    }

    public String buildWhereSql() {
        String whereSql = CriteriaUtils
                .whereSql(this, List.of("users", "securityCode", "username"), "a");

        Criteria criteria = Criteria.empty();

        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria = criteria.and("a.user_code").in(this.getUsers());
        }

        if (StringUtils.hasLength(this.getSecurityCode())) {
            criteria = criteria.and("a.tenant_code").like(this.getSecurityCode() + "%");
        }

        if (StringUtils.hasLength(this.getUsername())) {
            criteria = criteria.and("c.username").is(this.getUsername()).ignoreCase(true);
        }
        if (StringUtils.hasLength(whereSql)) {
            return whereSql + (criteria.isEmpty() ? "" : " and " + criteria);
        }
        if (criteria.isEmpty()) {
            return "";
        }
        return "Where " + criteria;
    }
}