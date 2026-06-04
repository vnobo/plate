package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;

/**
 * Request class for UserAuthority operations.
 * This class extends UserAuthority and provides methods to convert to UserAuthority and Criteria objects.
 * It uses Lombok annotations for boilerplate code reduction.
 * <p>
 * The class is annotated with \@Data, \@EqualsAndHashCode, and \@ToString to generate getter, setter, equals, hashCode, and toString methods.
 * It includes methods to convert the request to a UserAuthority entity and to create Criteria for database queries.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UserAuthorityReq extends UserAuthority {

    /**
     * Converts this request to a UserAuthority entity.
     *
     * @return a UserAuthority entity with properties copied from this request
     */
    public UserAuthority toAuthority() {
        return BeanUtils.copyProperties(this, UserAuthority.class);
    }

    /**
     * Creates a Criteria object based on this request.
     *
     * @return a Criteria object for querying the database
     */
    public Criteria toCriteria() {
        var criteria = criteria(Set.of());
        if (!ObjectUtils.isEmpty(this.getTenantCode())) {
            criteria = criteria.and("tenantCode").in(List.of(this.getTenantCode(), DEFAULT_UUID_CODE));
        }
        if (StringUtils.hasLength(this.getAuthority())) {
            criteria = criteria.and("authority").is(this.getAuthority());
        }
        return criteria;
    }

}