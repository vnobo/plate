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

    /**
     * The code associated with the logger entity.
     */
    private String code;

    /**
     * The tenant code associated with the logger entity.
     */
    private String tenantCode;

    /**
     * The operator associated with the logger entity.
     */
    private String operator;

    /**
     * The prefix associated with the logger entity.
     */
    private String prefix;

    /**
     * The URL associated with the logger entity.
     */
    private String url;

    /**
     * The HTTP method associated with the logger entity.
     */
    private String method;

    /**
     * The status associated with the logger entity.
     */
    private String status;

    /**
     * The context associated with the logger entity, stored as a JSON tree structure
     * using the Jackson library's JsonNode class.
     */
    private JsonNode context;

    /**
     * The date and time when the logger entity was created, automatically populated
     * by the @CreatedDate annotation provided by Spring Data.
     */
    @CreatedDate
    private LocalDateTime createdTime;

    /**
     * The date and time when the logger entity was last modified, automatically updated
     * by the @LastModifiedDate annotation provided by Spring Data.
     */
    @LastModifiedDate
    private LocalDateTime updatedTime;
}