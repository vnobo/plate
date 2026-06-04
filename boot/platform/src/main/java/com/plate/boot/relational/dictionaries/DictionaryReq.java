package com.plate.boot.relational.dictionaries;

import com.plate.boot.commons.utils.BeanUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Request object for Dictionary operations including search, create, update, and delete.
 * Provides flexible query criteria building and conversion to Dictionary entity.
 * <p>
 * This class supports:
 * - Full-text search via the search field
 * - Filtering by dictionary type, key, or enabled status
 * - Tenant and parent code filtering
 * - JSON-based extended queries
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DictionaryReq {

    /**
     * Dictionary unique identifier
     */
    private Long id;

    /**
     * Dictionary unique code
     */
    private UUID code;

    /**
     * Parent dictionary code for hierarchical queries
     */
    private UUID pcode;

    /**
     * Tenant code for multi-tenancy filtering
     */
    private UUID tenantCode;

    /**
     * Dictionary type for grouping
     */
    private String dictType;

    /**
     * Dictionary key
     */
    private String dictKey;

    /**
     * Dictionary value
     */
    @NotBlank(message = "Dictionary value cannot be blank!")
    private String dictValue;

    /**
     * Dictionary label for display
     */
    @NotBlank(message = "Dictionary label cannot be blank!")
    private String dictLabel;

    /**
     * Description
     */
    private String description;

    /**
     * Sort number
     */
    private Integer sortNo;

    /**
     * Enable flag
     */
    private Boolean enabled;

    /**
     * Extension data in JSON format
     */
    private JsonNode extend;

    /**
     * Full-text search keyword
     */
    private String search;

    /**
     * Converts this request object to a Dictionary entity.
     * Used when creating new dictionary entries.
     *
     * @return A new Dictionary instance with properties copied from this request
     */
    public Dictionary toDictionary() {
        Dictionary dictionary = new Dictionary();
        BeanUtils.copyProperties(this, dictionary);
        return dictionary;
    }

    /**
     * Builds a Criteria object for querying dictionaries based on this request.
     * Combines multiple search conditions using AND logic.
     * <p>
     * Supported criteria:
     * - Exact match: id, code, pcode, tenantCode, dictType, dictKey
     * - Equals match: enabled
     * - Pattern match: dictValue, dictLabel (case-insensitive LIKE)
     *
     * @return Criteria object for database querying
     */
    public Criteria toCriteria() {
        List<Criteria> criteriaList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(this.id)) {
            criteriaList.add(Criteria.where("id").is(this.id));
        }

        if (!ObjectUtils.isEmpty(this.code)) {
            criteriaList.add(Criteria.where("code").is(this.code));
        }

        if (!ObjectUtils.isEmpty(this.pcode)) {
            criteriaList.add(Criteria.where("pcode").is(this.pcode));
        }

        if (!ObjectUtils.isEmpty(this.tenantCode)) {
            criteriaList.add(Criteria.where("tenantCode").is(this.tenantCode));
        }

        if (StringUtils.hasText(this.dictType)) {
            criteriaList.add(Criteria.where("dictType").is(this.dictType));
        }

        if (StringUtils.hasText(this.dictKey)) {
            criteriaList.add(Criteria.where("dictKey").is(this.dictKey));
        }

        if (StringUtils.hasText(this.dictValue)) {
            criteriaList.add(Criteria.where("dictValue").like("%" + this.dictValue + "%").ignoreCase(true));
        }

        if (StringUtils.hasText(this.dictLabel)) {
            criteriaList.add(Criteria.where("dictLabel").like("%" + this.dictLabel + "%").ignoreCase(true));
        }

        if (!ObjectUtils.isEmpty(this.enabled)) {
            criteriaList.add(Criteria.where("enabled").is(this.enabled));
        }

        if (criteriaList.isEmpty()) {
            return Criteria.empty();
        }

        Criteria criteria = criteriaList.getFirst();
        for (int i = 1; i < criteriaList.size(); i++) {
            criteria = criteria.and(criteriaList.get(i));
        }
        return criteria;
    }
}
