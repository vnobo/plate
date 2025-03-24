package com.plate.boot.relational.menus;


import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.group.authority.GroupAuthoritiesRepository;
import com.plate.boot.security.core.user.authority.UserAuthoritiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service class for managing {@link Menu} entities.
 * <p>
 * This class provides methods to perform CRUD operations on menu items.
 * It interacts with the data access layer to retrieve and persist menu data.
 * </p>
 *
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class MenusService extends AbstractCache {
    /**
     * Prefix for authority roles.
     */
    public final static String AUTHORITY_PREFIX = "ROLE_";

    /**
     * Repository for managing menu entities.
     */
    private final MenusRepository menusRepository;

    /**
     * Repository for managing group authorities.
     */
    private final GroupAuthoritiesRepository groupAuthoritiesRepository;

    /**
     * Repository for managing user authorities.
     */
    private final UserAuthoritiesRepository userAuthoritiesRepository;

    /**
     * Searches for menus based on the provided request and pageable information.
     * Caches the result using a generated cache key.
     *
     * @param request  The menu request containing search criteria.
     * @param pageable The pagination information.
     * @return A Flux of Menu entities matching the search criteria.
     */
    public Flux<Menu> search(MenuReq request, Pageable pageable) {
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable).sort(Sort.by("sortNo"));
        return this.queryWithCache(cacheKey, query, Menu.class);
    }

    /**
     * Retrieves a paginated list of menus based on the provided request and pageable information.
     * Combines the search results with the total count of matching menus.
     *
     * @param request  The menu request containing search criteria.
     * @param pageable The pagination information.
     * @return A Mono of Page containing Menu entities.
     */
    public Mono<Page<Menu>> page(MenuReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        Query query = Query.query(request.toCriteria());
        var countMono = super.countWithCache(BeanUtils.cacheKey(request), query, Menu.class);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Adds a new menu based on the provided request.
     * Checks for the existence of a menu with the same criteria before adding.
     *
     * @param request The menu request containing the details of the menu to be added.
     * @return A Mono of the added Menu entity.
     */
    public Mono<Menu> add(MenuReq request) {
        log.debug("Menu add request: {}", request);
        Criteria criteria = MenuReq.of(request.getTenantCode(), request.getAuthority()).toCriteria();
        var existsMono = DatabaseUtils.ENTITY_TEMPLATE.exists(Query.query(criteria), Menu.class);
        existsMono = existsMono.filter(isExists -> !isExists);
        existsMono = existsMono.switchIfEmpty(Mono.error(RestServerException
                .withMsg("Add menu[" + request.getName() + "] is exists",
                        new IllegalArgumentException("The menu already exists, please try another name. is params: "
                                + criteria))));
        return existsMono.flatMap((b) -> this.operate(request));
    }

    /**
     * Modifies an existing menu based on the provided request.
     * Checks for the existence of the menu before modifying.
     *
     * @param request The menu request containing the details of the menu to be modified.
     * @return A Mono of the modified Menu entity.
     */
    public Mono<Menu> modify(MenuReq request) {
        log.debug("Menu modify request: {}", request);
        var oldMunuMono = this.menusRepository.findByCode(request.getCode())
                .switchIfEmpty(Mono.error(RestServerException.withMsg(
                        "Modify menu [" + request.getName() + "] is empty",
                        new IllegalArgumentException("The menu does not exist, please choose another name. is code: "
                                + request.getCode()))));
        oldMunuMono = oldMunuMono.flatMap(old -> {
            request.setId(old.getId());
            request.setAuthority(old.getAuthority());
            return this.operate(request);
        });
        return oldMunuMono;
    }

    /**
     * Saves a menu entity based on the provided request.
     *
     * @param request The menu request containing the details of the menu to be saved.
     * @return A Mono of the saved Menu entity.
     */
    public Mono<Menu> operate(MenuReq request) {
        log.debug("Menu operate request: {}", request);
        if (ObjectUtils.isEmpty(request)) {
            return Mono.empty();
        }
        var menu = request.toMenu();
        return this.save(menu).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Saves a menu entity.
     * If the menu is new, it is inserted; otherwise, it is updated.
     *
     * @param menu The menu entity to be saved.
     * @return A Mono of the saved Menu entity.
     */
    public Mono<Menu> save(Menu menu) {
        if (menu.isNew()) {
            return this.menusRepository.save(menu);
        } else {
            assert menu.getId() != null;
            return this.menusRepository.findById(menu.getId()).flatMap(old -> {
                menu.setCode(old.getCode());
                menu.setCreatedAt(old.getCreatedAt());
                return this.menusRepository.save(menu);
            });
        }
    }

    /**
     * Deletes a menu based on the provided request.
     * If the tenant code is "0", associated authorities are also deleted.
     *
     * @param request The menu request containing the details of the menu to be deleted.
     * @return A Mono indicating completion of the delete operation.
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> delete(MenuReq request) {
        log.warn("Delete menu request: {}", request);
        if (ObjectUtils.nullSafeEquals(request.getTenantCode(), "0")) {
            List<String> rules = new ArrayList<>(Collections.singletonList(request.getAuthority()));
            var deleteAuthorityMono = Flux.concatDelayError(this.groupAuthoritiesRepository.deleteByAuthorityIn(rules),
                    this.userAuthoritiesRepository.deleteByAuthorityIn(rules));
            var deleteNextMono = Flux.concatDelayError(
                    this.menusRepository.deleteByAuthority(request.getAuthority()), deleteAuthorityMono);
            return deleteNextMono.then().doAfterTerminate(() -> this.cache.clear());
        } else {
            return this.menusRepository.delete(request.toMenu()).doAfterTerminate(() -> this.cache.clear());
        }
    }
}