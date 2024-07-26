package com.plate.boot.security.core;

import com.plate.boot.commons.utils.ContextUtils;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public class UserAuditorAware implements ReactiveAuditorAware<UserAuditor> {
    @Override
    public @NonNull Mono<UserAuditor> getCurrentAuditor() {
        return ContextUtils.securityDetails().map(UserAuditor::withDetails);
    }
}