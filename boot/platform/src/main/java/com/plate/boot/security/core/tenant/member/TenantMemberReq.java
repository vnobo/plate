package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.query.Condition;
import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;

/**
 * Represents a request for tenant member operations, extending the TenantMember class.
 * This class includes additional fields and methods for handling tenant member-related requests.
 * It provides functionality to convert the request to a TenantMember object, create criteria for queries,
 * and generate SQL query fragments with parameters.
 * <p>
 * Author: <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantMemberReq extends TenantMember {

    /**
     * A set of user codes associated with the tenant member request.
     */
    private Set<UUID> users;

    /**
     * The username associated with the tenant member request.
     */
    private String username;

    /**
     * The security code associated with the tenant member request.
     */
    private String securityCode;

    /**
     * Sets the security code for the tenant member request.
     *
     * @param securityCode the security code to set
     * @return the updated TenantMemberReq instance
     */
    public TenantMemberReq securityCode(String securityCode) {
        this.setSecurityCode(securityCode);
        return this;
    }

    /**
     * Converts the TenantMemberReq instance to a TenantMember object.
     *
     * @return the converted TenantMember object
     */
    public TenantMember toMemberTenant() {
        return BeanUtils.copyProperties(this, TenantMember.class);
    }

    /**
     * Creates a Criteria object based on the fields of the TenantMemberReq instance.
     *
     * @return the created Criteria object
     */
    public Criteria toCriteria() {
        return criteria(Set.of("users", "username"));
    }

    /**
     * Generates a QueryFragment object with SQL parameters based on the fields of the TenantMemberReq instance.
     *
     * @return the generated QueryFragment object
     */
    public QueryFragment toParamSql() {
        Criteria criteria = toCriteria();
        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria = criteria.and("userCode").in(this.getUsers());
        }
        var conditionA = Condition.of(criteria, "a");

        Criteria criteriaB = Criteria.empty();
        if (StringUtils.hasLength(this.getUsername())) {
            criteriaB = criteriaB.and("username").is(this.getUsername());
        }
        var conditionB = Condition.of(criteriaB, "c");
        return QueryFragment.conditional(conditionA, conditionB).column("a.*", "b.name as tenant_name",
                        "b.extend as tenant_extend", "c.name as login_name", "c.username")
                .table("se_tenant_members a",
                        "inner join se_tenants b on a.tenant_code = b.code",
                        "inner join se_users c on c.code = a.user_code");
    }
}