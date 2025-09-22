package com.plate.boot.security.core.tenant;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for managing Tenant entities.
 * This interface extends R2dbcRepository to provide reactive CRUD operations for Tenant entities.
 */
public interface TenantsRepository extends R2dbcRepository<Tenant, Integer> {

    /**
     * Finds a Tenant by its code.
     *
     * @param code the code of the tenant
     * @return a Mono emitting the Tenant if found, or empty if not
     */
    Mono<Tenant> findByCode(UUID code);
}