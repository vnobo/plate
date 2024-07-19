package com.plate.authorization.security.core;

import com.plate.authorization.commons.utils.ContextUtils;
import com.plate.authorization.security.SecurityDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class UserAuditorAware implements AuditorAware<UserAuditor> {
    @Override
    public @NonNull Optional<UserAuditor> getCurrentAuditor() {
        SecurityDetails securityDetails = ContextUtils.securityDetails();
        return Optional.of(UserAuditor.withDetails(securityDetails));
    }
}