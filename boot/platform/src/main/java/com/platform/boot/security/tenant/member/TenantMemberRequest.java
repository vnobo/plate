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
import java.util.Map;
import java.util.Optional;
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
        request.setUserCode(username);
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

    /**
     * Retrieves the SQL query for querying data from the database.
     *
     * @return the SQL query as a String
     */
    public String querySql() {
        return """
                select *, se_tenants.name as tenant_name, se_tenants.extend as tenant_extend,
                    se_users.name as user_name
                from se_tenant_members
                inner join se_tenants on tenant_code = code
                inner join se_users on se_users.username = se_tenant_members.username
                """;
    }

    /**
     * Generates a SQL statement to count the number of records in the `se_tenant_members` table
     * by joining it with the `se_tenants` and `se_users` tables.
     *
     * @return a SQL statement that counts the number of records
     */
    public String countSql() {
        return """
                select count(*) from se_tenant_members
                inner join se_tenants on tenant_code = code
                inner join se_users on se_users.username = se_tenant_members.username
                """;
    }

    public CriteriaUtils.Parameter buildWhereSql() {
        var parameter = CriteriaUtils
                .whereParameterSql(this, List.of("users", "securityCode"), "se_tenant_members");
        Map<String, Object> bindParams = parameter.getParams();
        String whereSql = Optional.ofNullable(parameter.getSql()).orElse("Where 1=1 ");
        if (!ObjectUtils.isEmpty(this.getUsers())) {
            bindParams.put("users", this.getUsers());
            whereSql += " and se_tenant_members.username in( :users) ";
        }
        if (StringUtils.hasLength(this.getSecurityCode())) {
            bindParams.put("securityCode", this.getSecurityCode());
            whereSql += " and se_tenant_members.tenant_code like :securityCode";
        }
        return CriteriaUtils.Parameter.of(whereSql, bindParams);
    }
}