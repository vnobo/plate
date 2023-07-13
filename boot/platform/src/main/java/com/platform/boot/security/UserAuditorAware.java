package com.platform.boot.security;

import com.platform.boot.commons.utils.ContextHolder;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
@Component
public class UserAuditorAware implements ReactiveAuditorAware<UserAuditor> {
    @Override
    @NonNull
    public Mono<UserAuditor> getCurrentAuditor() {
        return ContextHolder.securityDetails().map(UserAuditor::withDetails);
    }

}