package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.base.AbstractEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Represents a member associated with a tenant within the system. This class extends the {@link AbstractEntity}
 * to inherit common entity properties and adds specific fields relevant to a tenant member.
 * <p>
 * The {@code userCode} field uniquely identifies the user associated with the tenant and must not be blank.
 * The {@code enabled} field indicates whether the tenant member is currently enabled or not.
 *
 * @see AbstractEntity
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("se_tenant_members")
public class TenantMember extends AbstractEntity<Long> {

    @NotBlank(message = "用户编码[userCode]不能为空!")
    private UUID userCode;

    private Boolean enabled;

}