package com.platform.boot.relational.menus;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface MenusRepository extends R2dbcRepository<Menu, Integer> {

    /**
     *  findByCode
     * @param code entity code
     * @return entity
     */
    Mono<Menu> findByCode(String code);
}