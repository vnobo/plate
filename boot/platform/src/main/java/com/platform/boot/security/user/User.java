package com.platform.boot.security.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.platform.boot.commons.base.BaseEntity;
import com.platform.boot.security.UserAuditor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Update;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Table("se_users")
public class User implements BaseEntity<Long> {

    @Id
    private Long id;

    @NotBlank(message = "user code [code] cannot be empty!", groups = Update.class)
    private String code;

    @NotBlank(message = "Login username [username] cannot be empty!")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{6,16}$", message = "Login username [username] must be " +
            "6 to 16 characters (letters, numbers, _, -)!")
    private String username;

    @NotBlank(message = "User password [password] cannot be empty!")
    @Pattern(regexp = "^.*(?=.{6,})(?=.*\\d)(?=.*[A-Z])(?=.*[a-z]).*$",
            message = "The login password [password] must be at least 6 characters," +
                    " including at least 1 uppercase letter, 1 lowercase letter, and 1 number.")
    private String password;

    private Boolean disabled;

    private Boolean accountExpired;

    private Boolean accountLocked;

    private Boolean credentialsExpired;

    private String name;

    private JsonNode extend;

    private LocalDateTime loginTime;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    @CreatedDate
    private LocalDateTime createdTime;
}