package com.plate.boot.security.core;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

/**
 * Implements the {@link ReactiveAuditorAware} interface to provide auditor information
 * about the current user within a reactive context. This class utilizes security details
 * to fetch and wrap them into a {@link UserAuditor} instance, enabling tracking of user actions
 * in an asynchronous environment.
 * <p>
 * The {@link #getCurrentAuditor()} method retrieves the auditor details, mapping them directly
 * from the security context, ensuring that each audit interaction is associated with the correct user.
 *
 * @see ReactiveAuditorAware For the contract on providing auditor information reactively.
 * @see UserAuditor For the structure containing user audit details.
 */
public class UserAuditorAware implements ReactiveAuditorAware<UserAuditor> {
    /**
     * Retrieves the current auditor information as a {@link UserAuditor} wrapped in a non-null {@link Mono}.
     * This method accesses the security details from the execution context and maps them
     * to a {@link UserAuditor} instance using the {@link UserAuditor#withDetails(SecurityDetails)} method.
     * It is particularly useful for auditing user actions within a reactive workflow.
     *
     * @return A {@link Mono} emitting the {@link UserAuditor} representing the current auditor details,
     * or an empty Mono if the auditor cannot be determined.
     */
    @Override
    public @NonNull Mono<UserAuditor> getCurrentAuditor() {
        return ContextUtils.securityDetails().map(UserAuditor::withDetails);
    }
}