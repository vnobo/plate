package com.platform.boot.security.tenant.member;

import com.platform.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "租户[tenantCode]不能为空!")
    private String tenantCode;

    @NotBlank(message = "用户[username]不能为空!")
    private String userCode;

    @NotNull(message = "是否启用[enabled]不能为空!")
    private Boolean enabled;

}