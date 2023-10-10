package com.platform.boot.security.group.member;

import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.security.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_group_members")
public class GroupMember implements BaseEntity<Long> {

    @Id
    private Long id;

    private String code;

    @NotBlank(message = "角色[groupCode]不能为空!")
    private String groupCode;

    @NotBlank(message = "用户[username]不能为空!")
    private String userCode;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;
}