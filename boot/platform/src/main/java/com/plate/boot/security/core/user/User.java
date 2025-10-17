package com.plate.boot.security.core.user;

import com.fasterxml.jackson.annotation.JsonView;
import com.plate.boot.commons.base.AbstractEntity;
import com.plate.boot.commons.base.BaseEntity;
import com.plate.boot.commons.base.BaseView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Table;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

/**
 * Represents a user entity with various attributes necessary for user management within a system.
 * This class includes fields for basic user details, authentication, contact information, and audit trails.
 * It also integrates with database annotations for ORM operations and includes custom business logic
 * for password handling and status flags.
 *
 * <p>The class implements the {@link BaseEntity} interface, inheriting common functionality
 * like unique code assignment and persistence operations with a generic type identifier of {@link Long}.</p>
 *
 * <p>Notable fields include:</p>
 * <ul>
 *     <li>{@code id}: The unique identifier for the user.</li>
 *     <li>{@code code}: A unique code for the user, often prefixed with 'U'.</li>
 *     <li>{@code tenantCode}: Identifier for the tenant or organization the user belongs to.</li>
 *     <li>{@code username}, {@code password}: Login credentials with validation constraints.</li>
 *     <li>{@code disabled}, {@code accountExpired}, {@code accountLocked}, {@code credentialsExpired}:
 *         Flags indicating the user's account status.</li>
 *     <li>{@code email}, {@code phone}, {@code name}, {@code avatar}: Contact and personal details.</li>
 *     <li>{@code bio}: A brief introduction or biography of the user.</li>
 *     <li>{@code extend}: A flexible field using {@link JsonNode} for storing additional user data.</li>
 *     <li>{@code loginTime}: Records the last login timestamp.</li>
 *     <li>{@code creator}, {@code updater}: Audit fields tracking who created and last modified the record.</li>
 *     <li>{@code updatedTime}, {@code createdTime}: Timestamps for creation and last update.</li>
 * </ul>
 *
 * <p>The class overrides the {@code setCode} method from {@link BaseEntity} to enforce a prefixing rule
 * for the user code and provides bean-style getters and setters for all properties.</p>
 *
 * @see BaseEntity for the base entity contract and common operations.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("se_users")
public class User extends AbstractEntity<Long> {

    /**
     * The username of the user for login authentication.
     * This field must not be empty and should adhere to a specific character restriction.
     * It accepts alphanumeric characters along with underscores (_) and hyphens (-),
     * ensuring a secure and standardized format for usernames.
     * The length must be within the range of 6 to 16 characters.
     *
     * @see #getUsername()
     * @see #setUsername(String)
     */
    @NotBlank(message = "Username [username] cannot be empty!")
    @Size(min = 3, max = 64, message = "Username [username] must be between 3 and 64 characters!")
    @JsonView(BaseView.Public.class)
    private String username;

    /**
     * The password associated with the user's account.
     * This field is required and must adhere to specific complexity rules:
     * - Must not be empty.
     * - Must be at least 6 characters long.
     * - Must contain at least 1 uppercase letter.
     * - Must contain at least 1 lowercase letter.
     * - Must contain at least 1 numeric digit.
     */
    @NotBlank(message = "User password [password] cannot be empty!")
    @Pattern(regexp = "^.*(?=.{6,})(?=.*\\d)(?=.*[A-Z])(?=.*[a-z]).*$",
            message = "The login password [password] must be at least 6 characters," +
                    " including at least 1 uppercase letter, 1 lowercase letter, and 1 number.")
    @JsonView(BaseView.Admin.class)
    private String password;

    /**
     * Indicates the status of whether the user's account is disabled or not.
     * A value of {@code true} signifies that the account is disabled, while {@code false} implies it is active.
     * This flag is instrumental in controlling user access and determining account availability.
     */
    @JsonView(BaseView.Admin.class)
    private Boolean disabled;

    /**
     * Indicates whether the user's account has expired.
     * This flag is often used in security contexts to determine if an account is no longer valid due to passage of time.
     * A true value signifies that the account is expired and access should be denied until the account is renewed.
     */
    @ReadOnlyProperty
    @JsonView(BaseView.Admin.class)
    private Boolean accountExpired;

    /**
     * Indicates the status of the user's account lock.
     * When this flag is set to {@code true}, it signifies that the user's account has been locked,
     * typically due to security reasons such as multiple failed login attempts.
     * A locked account prevents the user from logging in until it is unlocked by an administrator.
     */
    @ReadOnlyProperty
    @JsonView(BaseView.Admin.class)
    private Boolean accountLocked;

    /**
     * Indicates whether the user's credentials have expired.
     * This flag is typically used in authentication systems to determine if a user needs to renew their credentials
     * (e.g., password) before they can proceed with accessing secure resources.
     */
    @ReadOnlyProperty
    @JsonView(BaseView.Admin.class)
    private Boolean credentialsExpired;

    /**
     * The email address associated with the user.
     * This field holds the user's email which is used for communication and can be a primary contact point.
     */
    @JsonView(BaseView.Detail.class)
    private String email;

    /**
     * Represents the user's contact phone number.
     * This string field holds the phone number associated with a user's profile.
     * It is used for communication purposes, such as account verification, service notifications, or support contacts.
     */
    @JsonView(BaseView.Detail.class)
    private String phone;

    /**
     * The private field representing the name of the user.
     * This string holds the personal name or full name of the user account.
     */
    @JsonView(BaseView.Detail.class)
    private String name;

    /**
     * Represents the profile picture or graphical representation associated with a user.
     * This string field holds the reference or URL to the user's avatar image.
     */
    @JsonView(BaseView.Detail.class)
    private String avatar;

    /**
     * Represents the biography or a brief description about the user.
     * This string field can include personal background, professional experience, or any other relevant long-form text
     * that provides more insight into the user's identity or profile.
     */
    @JsonView(BaseView.Detail.class)
    private String bio;

    /**
     * Represents the timestamp when the user logged in.
     * This field captures the exact date and time when the user authenticated into the system.
     * It is typically set during the login process to track user activity and can be utilized
     * for analytics, session management, or security auditing purposes.
     */
    @ReadOnlyProperty
    @JsonView(BaseView.Admin.class)
    private LocalDateTime loginTime;

}