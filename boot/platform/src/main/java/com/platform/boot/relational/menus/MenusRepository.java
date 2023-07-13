package com.platform.boot.relational.menus;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * This interface extends R2dbcRepository to provide
 * the necessary methods to interact with the database for the Menu entity.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface MenusRepository extends R2dbcRepository<Menu, Integer> {

    /**
     * 根据code查找
     *
     * @param code CODE
     * @return entity
     */
    Mono<Menu> findByCode(String code);
}