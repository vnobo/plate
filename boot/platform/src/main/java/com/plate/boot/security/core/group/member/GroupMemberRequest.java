package com.plate.boot.security.core.group.member;

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
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberRequest extends GroupMember {

    private Set<String> users;

    private String username;

    public GroupMember toGroupMember() {
        return BeanUtils.copyProperties(this, GroupMember.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("users", "username"));
    }

    public QueryFragment toParamSql() {
        QueryFragment QueryFragment = QueryHelper.query(this, List.of("users", "username"), "a");

        StringJoiner criteria = QueryFragment.sql();
        Map<String, Object> params = QueryFragment.params();

        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria.add("a.user_code in :users");
            params.put("users", this.getUsers());
        }

        if (StringUtils.hasLength(this.getUsername())) {
            criteria.add("c.username = :username");
            params.put("username", this.getUsername());
        }

        return QueryFragment.of(criteria, params);
    }

}