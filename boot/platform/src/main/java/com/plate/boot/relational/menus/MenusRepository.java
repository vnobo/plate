package com.plate.boot.relational.menus;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

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
    Mono<Menu> findByCode(String code);


    /**
     * Deletes menu records by the specified authority.
     * <p>
     * This operation uses Mono to represent the asynchronous deletion process,
     * returning the number of deleted records wrapped in a Mono. It is designed
     * to work seamlessly in reactive environments, suitable for applications requiring
     * non-blocking handling of data operations.
     *
     * @param authority The authority identifier used to match menu records for deletion.
     * @return A Mono containing the count of deleted menu records. If no records are deleted, returns Mono.empty().
     */
    Mono<Long> deleteByAuthority(String authority);
}