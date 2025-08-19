package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.query.QueryHelper;
import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

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
    private Set<String> users;

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
        return criteria(Set.of("securityCode", "users", "username"));
    }

    /**
     * Generates a QueryFragment object with SQL parameters based on the fields of the TenantMemberReq instance.
     *
     * @return the generated QueryFragment object
     */
    public QueryFragment toParamSql() {
        QueryFragment fragment = QueryHelper.query(this, List.of("users", "securityCode", "username"), "a");

        if (!ObjectUtils.isEmpty(this.getUsers())) {
            fragment.where("a.user_code in (:users)");
            fragment.put("users", StringUtils.collectionToCommaDelimitedString(this.getUsers()));
        }

        if (StringUtils.hasLength(this.getSecurityCode())) {
            fragment.where("a.tenant_code like :securityCode");
            fragment.put("securityCode", this.getSecurityCode());
        }

        if (StringUtils.hasLength(this.getUsername())) {
            fragment.where("c.username = :username");
            fragment.put("username", this.getUsername());
        }

        return fragment;
    }
}