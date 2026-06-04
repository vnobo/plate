package com.plate.boot.relational.menus;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Interface representing a repository for menu operations with reactive support.
 * Extends R2dbcRepository to inherit basic CRUD operations and additional functionality.
 */
public interface MenusRepository extends R2dbcRepository<Menu, Integer> {


    /**
     * Finds a menu entry based on the provided code.
     *
     * @param code The unique code that identifies the menu.
     * @return A Mono emitting the found {@link Menu} object, or empty if no menu exists with the given code.
     */
    Mono<Menu> findByCode(UUID code);

    /**
     * Finds a menu entry based on the provided tenant code and authority.
     *
     * @param tenantCode The unique code that identifies the tenant.
     * @param authority  The authority associated with the menu.
     * @return A Mono emitting the found {@link Menu} object, or empty if no menu exists with the given tenant code and authority.
     */
    Mono<Menu> findByTenantCodeAndAuthority(UUID tenantCode, String authority);
}