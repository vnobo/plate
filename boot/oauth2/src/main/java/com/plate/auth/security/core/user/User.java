package com.plate.auth.security.core.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.plate.auth.commons.base.BaseEntity;
import com.plate.auth.security.core.UserAuditor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@Entity
@Table(name = "se_users")
@EntityListeners(AuditingEntityListener.class)
public class User implements BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String tenantCode;

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

    private String email;

    private String phone;

    private String name;

    private String avatar;

    /**
     * 简介
     */
    private String bio;

    private ObjectNode extend;

    private LocalDateTime loginTime;

    @CreatedBy
    private UserAuditor creator;

    @LastModifiedBy
    private UserAuditor updater;

    @LastModifiedDate
    private LocalDateTime updatedTime;

    @CreatedDate
    private LocalDateTime createdTime;

    @Override
    public void setCode(String code) {
        this.code = code.startsWith("U") ? code : "U" + code;
    }
}