package com.plate.boot.security.core.group;

import com.fasterxml.jackson.databind.JsonNode;
import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.security.core.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_groups")
public class Group implements BaseEntity<Integer> {

    @Id
    private Integer id;

    private String code;

    @NotBlank(message = "Tenant [tenantCode] not be empty!")
    private String tenantCode;

    @NotBlank(message = " Rules [name] not be empty!")
    private String name;

    private JsonNode extend;

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
        this.code = code.startsWith("G") ? code : "G" + code;
    }
}