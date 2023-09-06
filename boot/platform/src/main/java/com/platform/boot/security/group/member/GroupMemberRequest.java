package com.platform.boot.security.group.member;

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
import java.util.Set;

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

    public String buildWhereSql() {
        String whereSql = CriteriaUtils.whereSql(this, List.of("users", "username"), "a");

        Criteria criteria = Criteria.empty();

        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria = criteria.and("a.user_code").in(this.getUsers());
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