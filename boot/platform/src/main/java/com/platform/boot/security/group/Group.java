package com.platform.boot.security.group;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.base.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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

    private String pcode;

    @NotBlank(message = "租户编码[tenantCode]不能为空!")
    private String tenantCode;

    @NotBlank(message = "角色[name]不能为空!")
    private String name;

    private JsonNode extend;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;

}