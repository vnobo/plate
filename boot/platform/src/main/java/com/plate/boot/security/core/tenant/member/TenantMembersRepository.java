package com.plate.boot.security.core.tenant.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for tenant member operations.
 * Extends the R2dbcRepository interface to provide reactive CRUD operations for TenantMember entities.
 * Includes custom methods for deleting tenant members by tenantCode and userCode.
 * <p>
 * Author: <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface TenantMembersRepository extends R2dbcRepository<TenantMember, Long> {

    /**
     * Deletes records from the database based on the provided tenantCode.
     *
     * @param tenantCode the tenantCode to delete by
     * @return a Mono emitting the number of records deleted
     */
    Mono<Integer> deleteByTenantCode(String tenantCode);

    /**
     * Deletes records from the database based on the provided userCode.
     *
     * @param userCode the userCode to delete by
     * @return a Mono emitting the number of records deleted
     */
    Mono<Integer> deleteByUserCode(UUID userCode);
}