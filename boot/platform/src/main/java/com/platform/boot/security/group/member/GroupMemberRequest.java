package com.platform.boot.security.group.member;

import com.platform.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberRequest extends GroupMember implements Serializable {

    private Set<String> users;

    public GroupMember toGroupMember() {
        return BeanUtils.copyProperties(this, GroupMember.class);
    }

    public GroupMemberRequest id(Long id) {
        this.setId(id);
        return this;
    }

    public Criteria toCriteria() {
        return criteria(Set.of());
    }

}