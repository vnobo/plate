package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.utils.BeanUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Represents a request for tenant information, extending the Tenant class.
 * This class includes additional query parameters.
 * <p>
 * The class uses Lombok annotations for boilerplate code reduction:
 * - \@Data generates getters, setters, toString, equals, and hashCode methods.
 * - \@EqualsAndHashCode(callSuper = true) includes the superclass fields in the equals and hashCode methods.
 * - \@ToString(callSuper = true) includes the superclass fields in the toString method.
 * <p>
 * The toTenant method converts this TenantReq instance to a Tenant instance.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TenantReq extends Tenant {

    /**
     * Converts this TenantReq instance to a Tenant instance.
     *
     * @return a new Tenant instance with properties copied from this TenantReq instance.
     */
    public Tenant toTenant() {
        return BeanUtils.copyProperties(this, Tenant.class);
    }

}