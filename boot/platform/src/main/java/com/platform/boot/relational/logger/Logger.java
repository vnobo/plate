package com.platform.boot.relational.logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.base.BaseEntity;
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
@Table("se_loggers")
public class Logger implements BaseEntity<Long> {

    @Id
    private Long id;

    private String code;

    private String tenantCode;

    private String operator;

    private String prefix;

    private String url;

    private String method;

    private String status;

    private JsonNode context;

    @CreatedDate
    private LocalDateTime createdTime;

    @LastModifiedDate
    private LocalDateTime updatedTime;
}