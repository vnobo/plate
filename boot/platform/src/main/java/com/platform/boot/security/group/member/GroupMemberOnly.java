package com.platform.boot.security.group.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.security.group.Group;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class GroupMemberOnly extends GroupMember {

    private String groupName;

    private JsonNode groupExtend;

    public static GroupMemberOnly withGroupMember(GroupMember groupMember) {
        return BeanUtils.copyProperties(groupMember, GroupMemberOnly.class);
    }

    public GroupMemberOnly group(Group group) {
        this.setGroupName(group.getName());
        this.setGroupExtend(group.getExtend());
        return this;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return super.isNew();
    }

}