package com.platform.boot.security.core.tenant.member;

import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_tenant_members")
public class TenantMember implements BaseEntity<Long> {

    @Id
    private Long id;

    private String code;

    @NotBlank(message = "租户编码[tenantCode]不能为空!")
    private String tenantCode;

    @NotBlank(message = "用户编码[userCode]不能为空!")
    private String userCode;

    private Boolean enabled;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    @Override
    public void setCode(String code) {
        this.code = code.startsWith("TM") ? code : "TM" + code;
    }
}