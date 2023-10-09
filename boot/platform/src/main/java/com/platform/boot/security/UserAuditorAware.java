package com.platform.boot.security;

import com.platform.boot.commons.utils.ContextUtils;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

/**
 * @author Alex bob(<a href="https://github.com/vnobo">Alex Bob</a>)
 */
public class UserAuditorAware implements ReactiveAuditorAware<UserAuditor> {
    @Override
    @NonNull
    public Mono<UserAuditor> getCurrentAuditor() {
        return ContextUtils.securityDetails().map(UserAuditor::withDetails);
    }

}