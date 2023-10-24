package com.platform.boot.security.group.member;

import com.platform.boot.commons.query.ParamSql;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
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
public class GroupMemberRequest extends GroupMember implements Serializable {

    private Set<String> users;
    private String username;

    public GroupMember toGroupMember() {
        return BeanUtils.copyProperties(this, GroupMember.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("users", "username"));
    }

    public String querySql() {
        return """
                select a.*, b.name as group_name, b.extend as group_extend,c.name as login_name,c.username
                from se_group_members a
                inner join se_groups b on a.group_code = b.code
                inner join se_users c on c.code = a.user_code
                """;
    }

    public String countSql() {
        return """
                select count(*) from se_group_members a
                inner join se_groups b on a.group_code = b.code
                inner join se_users c on c.code = a.user_code
                """;
    }

    public ParamSql toParamSql() {
        ParamSql paramSql = CriteriaUtils.applyParamsSql(this, List.of("users", "username"), "a");

        StringJoiner criteria = paramSql.sql();
        Map<String, Object> params = paramSql.params();

        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria.add("a.user_code in :users");
            params.put("users", this.getUsers());
        }

        if (StringUtils.hasLength(this.getUsername())) {
            criteria.add("c.username = :username");
            params.put("username", this.getUsername());
        }

        return ParamSql.of(criteria, params);
    }

}