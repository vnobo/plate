package com.plate.boot.relational.dictionaries;

import com.plate.boot.commons.base.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Dictionary entity representing a data dictionary entry in the system.
 * Provides key-value pair storage with support for hierarchical structure,
 * multi-tenancy, and flexible extension through JSON fields.
 * <p>
 * Data dictionaries are commonly used for:
 * - Dropdown options and select lists
 * - System configuration parameters
 * - Enumeration values
 * - Classification and categorization
 * <p>
 * Features:
 * - Hierarchical structure support via pcode (parent code)
 * - Multi-tenant isolation via tenant_code
 * - Type-based grouping for organizing related dictionary items
 * - Sorting support for ordered display
 * - Enable/disable flag for soft deletion
 * - Full-text search capability
 * - JSON extension field for custom attributes
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("se_dictionaries")
@Schema(description = "Dictionary entity representing a data dictionary entry")
public class Dictionary extends AbstractEntity<Long> {

    /**
     * Parent dictionary code for hierarchical structure.
     * Default value is '00000000-0000-0000-0000-000000000000' for root-level dictionaries.
     */
    @Schema(description = "Parent dictionary code for hierarchical structure",
            example = "00000000-0000-0000-0000-000000000000")
    private UUID pcode;

    /**
     * Dictionary type for grouping related dictionary items.
     * Examples: "USER_STATUS", "GENDER", "EDUCATION_LEVEL", "COUNTRY"
     * <p>
     * This field is used to categorize dictionaries and allows
     * querying all items of a specific type.
     */
    @NotBlank(message = "Dictionary type cannot be blank!")
    @Size(max = 128, message = "Dictionary type cannot exceed 128 characters!")
    @Schema(description = "Dictionary type for grouping related items",
            example = "USER_STATUS", required = true, maxLength = 128)
    private String dictType;

    /**
     * Dictionary key - unique identifier within a type.
     * This is typically a code or constant value used in the application.
     * <p>
     * Examples: "ACTIVE", "INACTIVE", "MALE", "FEMALE", "BACHELOR", "MASTER"
     */
    @NotBlank(message = "Dictionary key cannot be blank!")
    @Size(max = 256, message = "Dictionary key cannot exceed 256 characters!")
    @Schema(description = "Dictionary key - unique identifier within a type",
            example = "ACTIVE", required = true, maxLength = 256)
    private String dictKey;

    /**
     * Dictionary value - the actual value stored.
     * This can be a simple value or complex data depending on usage.
     * <p>
     * Examples: "1", "0", "M", "F", "active_status", "inactive_status"
     */
    @NotBlank(message = "Dictionary value cannot be blank!")
    @Schema(description = "Dictionary value - the actual stored value",
            example = "1", required = true)
    private String dictValue;

    /**
     * Dictionary label - human-readable display text.
     * This is typically shown in the UI for user selection.
     * <p>
     * Examples: "Active", "Inactive", "Male", "Female", "Bachelor's Degree", "Master's Degree"
     */
    @NotBlank(message = "Dictionary label cannot be blank!")
    @Size(max = 512, message = "Dictionary label cannot exceed 512 characters!")
    @Schema(description = "Dictionary label - human-readable display text",
            example = "Active", required = true, maxLength = 512)
    private String dictLabel;

    /**
     * Description providing additional information about the dictionary item.
     * Optional field for documentation and clarification purposes.
     */
    @Schema(description = "Additional description for the dictionary item",
            example = "This status indicates an active user account")
    private String description;

    /**
     * Sort number for ordering dictionary items within the same type.
     * Lower numbers appear first. Default is 0.
     */
    @Schema(description = "Sort order number - lower numbers appear first",
            example = "1", defaultValue = "0")
    private Integer sortNo;

    /**
     * Enable flag indicating whether this dictionary item is active.
     * Default is true. Can be used for soft deletion or temporary disabling.
     */
    @Schema(description = "Enable flag - whether the dictionary item is active",
            example = "true", defaultValue = "true")
    private Boolean enabled;
}
