package com.plate.boot.relational.dictionaries;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Dictionary entity operations.
 * Provides reactive CRUD operations and custom query methods.
 * <p>
 * Extends R2dbcRepository to inherit standard reactive database operations
 * and adds domain-specific query methods for dictionaries.
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
public interface DictionariesRepository extends R2dbcRepository<Dictionary, Long> {

    /**
     * Finds a dictionary by its unique code.
     *
     * @param code The unique UUID code of the dictionary
     * @return A Mono emitting the Dictionary if found, empty otherwise
     */
    Mono<Dictionary> findByCode(UUID code);

    /**
     * Finds a dictionary by tenant code, type, and key combination.
     * This corresponds to the unique constraint on these three fields.
     *
     * @param tenantCode The tenant UUID
     * @param dictType   The dictionary type
     * @param dictKey    The dictionary key
     * @return A Mono emitting the Dictionary if found, empty otherwise
     */
    Mono<Dictionary> findByTenantCodeAndDictTypeAndDictKey(UUID tenantCode, String dictType, String dictKey);

    /**
     * Finds all dictionaries of a specific type within a tenant.
     * Results are ordered by sortNo ascending.
     *
     * @param tenantCode The tenant UUID
     * @param dictType   The dictionary type
     * @return A Flux emitting all matching dictionaries
     */
    Flux<Dictionary> findByTenantCodeAndDictTypeOrderBySortNoAsc(UUID tenantCode, String dictType);

    /**
     * Finds all enabled dictionaries of a specific type within a tenant.
     * Useful for getting active dropdown options.
     *
     * @param tenantCode The tenant UUID
     * @param dictType   The dictionary type
     * @param enabled    The enabled flag (typically true)
     * @return A Flux emitting all matching enabled dictionaries ordered by sortNo
     */
    Flux<Dictionary> findByTenantCodeAndDictTypeAndEnabledOrderBySortNoAsc(UUID tenantCode, String dictType, Boolean enabled);

    /**
     * Finds all child dictionaries under a parent code.
     * Useful for hierarchical dictionary structures.
     *
     * @param pcode The parent dictionary code
     * @return A Flux emitting all child dictionaries
     */
    Flux<Dictionary> findByPcodeOrderBySortNoAsc(UUID pcode);
}
