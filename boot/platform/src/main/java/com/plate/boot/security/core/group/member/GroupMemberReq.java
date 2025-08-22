package com.plate.boot.security.core.group.member;

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
 * Represents a request model for operations involving group members, extending the {@link GroupMember} entity.
 * This class introduces parameters necessary for querying and managing group members, specifically targeting
 * user sets and usernames for filtration purposes.
 *
 * <p>
 * The `users` field is a set of strings representing the unique identifiers (codes) of multiple users.
 * This enables batch operations on group memberships involving multiple users at once.
 *
 * <p>
 * The `username` field allows for searching or filtering group members based on a specific username,
 * adding a layer of granularity to member queries within groups.
 *
 * <p>
 * This class provides utility methods to convert the request object into a {@link GroupMember} instance,
 * a {@link Criteria} for structured querying, and a {@link QueryFragment} to generate parameterized SQL,
 * facilitating database interactions tailored to the request parameters.
 *
 * <ul>
 *   <li>{@link #toGroupMember()} converts this request object into a {@link GroupMember} entity.</li>
 *   <li>{@link #toCriteria()} generates a criteria object based on the request's filterable properties.</li>
 *   <li>{@link #toParamSql()} constructs a QueryFragment for dynamic SQL generation with parameter placeholders,
 *       suitable for complex query construction with optional filters.</li>
 * </ul>
 *
 * @see GroupMember
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberReq extends GroupMember {

    private Set<UUID> users;

    private String username;

    public GroupMember toGroupMember() {
        return BeanUtils.copyProperties(this, GroupMember.class);
    }

    public Criteria toCriteria() {
        return criteria(Set.of("users", "username"));
    }

    public QueryFragment toParamSql() {
        Criteria criteria = toCriteria();
        if (!ObjectUtils.isEmpty(this.getUsers())) {
            criteria = criteria.and("userCode").in(this.getUsers());
        }
        var conditionA = QueryFragment.Condition.of(criteria, "a");

        Criteria criteriaB = Criteria.empty();
        if (StringUtils.hasLength(this.getUsername())) {
            criteriaB = criteriaB.and("username").is(this.getUsername());
        }
        var conditionB = QueryFragment.Condition.of(criteriaB, "c");
        return QueryFragment.conditional(conditionA, conditionB).table("se_group_members a",
                        "inner join se_groups b on a.group_code = b.code",
                        "inner join se_users c on c.code = a.user_code")
                .column("a.*", "b.name as group_name",
                        "b.extend as group_extend", "c.name as login_name", "c.username");
    }

}