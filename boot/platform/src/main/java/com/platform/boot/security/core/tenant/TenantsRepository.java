package com.platform.boot.security.core.tenant;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface TenantsRepository extends R2dbcRepository<Tenant, Integer> {

    /**
     * get tenant by code
     *
     * @param code tenant code
     * @return tenant result
     */
    Mono<Tenant> findByCode(String code);
}