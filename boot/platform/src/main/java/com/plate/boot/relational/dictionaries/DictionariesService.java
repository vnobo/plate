package com.plate.boot.relational.dictionaries;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing Dictionary entities.
 * Provides comprehensive CRUD operations, caching, and business logic for data dictionaries.
 * <p>
 * Features:
 * - Search and pagination support
 * - Caching for improved performance
 * - Type-based queries for dropdown lists
 * - Hierarchical dictionary support
 * - Event publishing for dictionary lifecycle
 * - Multi-tenancy support
 * - Full-text search capability
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DictionariesService extends AbstractCache {

    private final DictionariesRepository dictionariesRepository;

    /**
     * Searches for dictionaries based on the provided criteria with pagination.
     * Results are cached for improved performance.
     *
     * @param request  The search criteria
     * @param pageable Pagination information
     * @return A Flux of matching dictionaries
     */
    public Flux<Dictionary> search(DictionaryReq request, Pageable pageable) {
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable).sort(Sort.by("sortNo").ascending());
        return this.queryWithCache(cacheKey, query, Dictionary.class);
    }

    /**
     * Retrieves a paginated list of dictionaries.
     *
     * @param request  The search criteria
     * @param pageable Pagination information
     * @return A Mono emitting a Page of dictionaries
     */
    public Mono<Page<Dictionary>> page(DictionaryReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        Query query = Query.query(request.toCriteria());
        var countMono = super.countWithCache(BeanUtils.cacheKey(request), query, Dictionary.class);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Retrieves all dictionaries of a specific type for a tenant.
     * Useful for populating dropdown lists.
     *
     * @param tenantCode The tenant UUID
     * @param dictType   The dictionary type
     * @return A Flux of dictionaries ordered by sortNo
     */
    public Flux<Dictionary> findByType(java.util.UUID tenantCode, String dictType) {
        String cacheKey = "dict:type:" + tenantCode + ":" + dictType;
        return this.queryWithCache(cacheKey, 
            this.dictionariesRepository.findByTenantCodeAndDictTypeOrderBySortNoAsc(tenantCode, dictType));
    }

    /**
     * Retrieves all enabled dictionaries of a specific type for a tenant.
     * Only returns active dictionary items.
     *
     * @param tenantCode The tenant UUID
     * @param dictType   The dictionary type
     * @return A Flux of enabled dictionaries ordered by sortNo
     */
    public Flux<Dictionary> findEnabledByType(java.util.UUID tenantCode, String dictType) {
        String cacheKey = "dict:type:enabled:" + tenantCode + ":" + dictType;
        return this.queryWithCache(cacheKey,
            this.dictionariesRepository.findByTenantCodeAndDictTypeAndEnabledOrderBySortNoAsc(
                tenantCode, dictType, true));
    }

    /**
     * Finds children of a parent dictionary.
     * Supports hierarchical dictionary structures.
     *
     * @param pcode The parent dictionary code
     * @return A Flux of child dictionaries
     */
    public Flux<Dictionary> findChildren(java.util.UUID pcode) {
        String cacheKey = "dict:children:" + pcode;
        return this.queryWithCache(cacheKey,
            this.dictionariesRepository.findByPcodeOrderBySortNoAsc(pcode));
    }

    /**
     * Adds a new dictionary entry.
     * Validates that no duplicate exists before insertion.
     *
     * @param request The dictionary creation request
     * @return A Mono emitting the created dictionary
     */
    @CacheEvict(cacheNames = "dictionaries", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Mono<Dictionary> add(DictionaryReq request) {
        log.debug("Dictionary add request: {}", request);
        
        var existsMono = this.dictionariesRepository.findByTenantCodeAndDictTypeAndDictKey(
            request.getTenantCode(), request.getDictType(), request.getDictKey());
        
        existsMono = existsMono.flatMap(_ -> Mono.error(RestServerException.withMsg(
            "Dictionary [" + request.getDictType() + ":" + request.getDictKey() + "] already exists",
            new IllegalArgumentException("Duplicate dictionary entry with type: " + request.getDictType() 
                + ", key: " + request.getDictKey()))));
        
        return existsMono.switchIfEmpty(Mono.defer(() -> this.operate(request)));
    }

    /**
     * Modifies an existing dictionary entry.
     * Validates that the dictionary exists before modification.
     *
     * @param request The dictionary modification request
     * @return A Mono emitting the updated dictionary
     */
    @CacheEvict(cacheNames = "dictionaries", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Mono<Dictionary> modify(DictionaryReq request) {
        log.debug("Dictionary modify request: {}", request);
        
        var oldDictMono = this.dictionariesRepository.findByCode(request.getCode())
            .switchIfEmpty(Mono.error(RestServerException.withMsg(
                "Dictionary [" + request.getCode() + "] not found",
                new IllegalArgumentException("Dictionary does not exist with code: " + request.getCode()))));
        
        return oldDictMono.flatMap(old -> {
            request.setId(old.getId());
            return this.operate(request);
        });
    }

    /**
     * Performs the actual save operation for dictionary entities.
     * Handles both insert and update operations.
     *
     * @param request The dictionary request
     * @return A Mono emitting the saved dictionary
     */
    public Mono<Dictionary> operate(DictionaryReq request) {
        log.debug("Dictionary operate request: {}", request);
        
        return this.dictionariesRepository.findByCode(request.getCode())
            .switchIfEmpty(Mono.defer(() -> this.dictionariesRepository
                .findByTenantCodeAndDictTypeAndDictKey(request.getTenantCode(), 
                    request.getDictType(), request.getDictKey())))
            .defaultIfEmpty(request.toDictionary())
            .flatMap(dictionary -> {
                BeanUtils.copyProperties(request, dictionary, true);
                return this.save(dictionary);
            })
            .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Saves a dictionary entity.
     * Determines whether to insert or update and publishes appropriate events.
     *
     * @param dictionary The dictionary to save
     * @return A Mono emitting the saved dictionary
     */
    public Mono<Dictionary> save(Dictionary dictionary) {
        if (dictionary.isNew()) {
            return this.dictionariesRepository.save(dictionary)
                .doOnNext(res -> ContextUtils.eventPublisher(DictionaryEvent.insert(res)));
        } else {
            assert dictionary.getId() != null;
            return this.dictionariesRepository.findById(dictionary.getId()).flatMap(old -> {
                dictionary.setCode(old.getCode());
                dictionary.setCreatedAt(old.getCreatedAt());
                return this.dictionariesRepository.save(dictionary);
            }).doOnNext(res -> ContextUtils.eventPublisher(DictionaryEvent.update(res)));
        }
    }

    /**
     * Deletes a dictionary entry.
     * Publishes a delete event and clears cache.
     *
     * @param request The dictionary delete request containing the code
     * @return A Mono indicating completion
     */
    @CacheEvict(cacheNames = "dictionaries", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> delete(DictionaryReq request) {
        log.warn("Delete dictionary request: {}", request);
        
        return this.dictionariesRepository.findByCode(request.getCode())
            .doOnNext(res -> ContextUtils.eventPublisher(DictionaryEvent.delete(res)))
            .flatMap(this.dictionariesRepository::delete)
            .doAfterTerminate(() -> this.cache.clear());
    }
}
