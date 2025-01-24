package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_tenant_members")
public class TenantMember extends AbstractEntity<Long> {

    @NotBlank(message = "用户编码[userCode]不能为空!")
    private UUID userCode;

    private Boolean enabled;

}