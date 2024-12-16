package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
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

    public QueryFragment toParamSql() {
        QueryFragment fragment = QueryHelper.query(this, List.of("users", "securityCode", "username"), "a");

        if (!ObjectUtils.isEmpty(this.getUsers())) {
            fragment.addWhere("a.user_code in (:users)");
            fragment.put("users", StringUtils.collectionToCommaDelimitedString(this.getUsers()));
        }

        if (StringUtils.hasLength(this.getSecurityCode())) {
            fragment.addWhere("a.tenant_code like :securityCode");
            fragment.put("securityCode", this.getSecurityCode());
        }

        if (StringUtils.hasLength(this.getUsername())) {
            fragment.addWhere("c.username = :username");
            fragment.put("username", this.getUsername());
        }

        return fragment;
    }
}