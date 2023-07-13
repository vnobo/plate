package com.platform.boot.security.tenant.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface TenantMembersRepository extends R2dbcRepository<TenantMember, Long> {

    /**
     *  delete by tenant code
     * @param tenantCode tenant code
     * @return delete result count
     */
    Mono<Integer> deleteByTenantCode(String tenantCode);
}