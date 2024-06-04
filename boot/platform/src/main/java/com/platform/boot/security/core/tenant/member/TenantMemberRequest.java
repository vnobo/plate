package com.platform.boot.security.core.tenant.member;

import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.query.CriteriaUtils;
import com.platform.boot.commons.utils.query.ParamSql;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

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

    public ParamSql toParamSql() {
        ParamSql paramSql = CriteriaUtils
                .buildParamSql(this, List.of("users", "securityCode", "username"), "a");

        StringJoiner criteria = paramSql.sql();
        Map<String, Object> params = paramSql.params();
        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria.add("a.user_code in :users");
            params.put("users", this.getUsers());
        }

        if (StringUtils.hasLength(this.getSecurityCode())) {
            criteria.add("a.tenant_code like :securityCode");
            params.put("securityCode", this.getSecurityCode());
        }

        if (StringUtils.hasLength(this.getUsername())) {
            criteria.add("c.username = :username");
            params.put("username", this.getUsername());
        }

        return ParamSql.of(criteria, params);
    }
}