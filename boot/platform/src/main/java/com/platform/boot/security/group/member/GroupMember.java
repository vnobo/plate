package com.platform.boot.security.group.member;

import com.platform.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_group_members")
public class GroupMember implements BaseEntity<Long> {

    @Id
    private Long id;

    @NotBlank(message = "角色[groupCode]不能为空!")
    private String groupCode;

    @NotBlank(message = "用户[username]不能为空!")
    private String username;

}