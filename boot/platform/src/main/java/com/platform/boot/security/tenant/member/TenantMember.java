package com.platform.boot.security.tenant.member;

import com.platform.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_tenant_members")
public class TenantMember implements BaseEntity<Long> {

    @Id
    private Long id;

    @NotBlank(message = "租户编码[tenantCode]不能为空!")
    private String tenantCode;

    @NotBlank(message = "用户编码[userCode]不能为空!")
    private String userCode;

    private Boolean enabled;

}