package com.plate.boot.relational.menus;


import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.BeanUtils;
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
public class MenusService extends AbstractDatabase {
    public final static String AUTHORITY_PREFIX = "ROLE_";

    private final MenusRepository menusRepository;
    private final GroupAuthoritiesRepository groupAuthoritiesRepository;
    private final UserAuthoritiesRepository userAuthoritiesRepository;

    public Flux<Menu> search(MenuRequest request, Pageable pageable) {
        Query query = Query.query(request.toCriteria()).with(pageable).sort(Sort.by("sortNo"));
        return this.queryWithCache(BeanUtils.cacheKey(request), query, Menu.class);
    }

    public Mono<Page<Menu>> page(MenuRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        Query query = Query.query(request.toCriteria());
        var countMono = super.countWithCache(BeanUtils.cacheKey(request), query, Menu.class);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<Menu> add(MenuRequest request) {
        log.debug("Menu add request: {}", request);
        Criteria criteria = MenuRequest.of(request.getTenantCode(), request.getAuthority()).toCriteria();
        var existsMono = this.entityTemplate.exists(Query.query(criteria), Menu.class);
        existsMono = existsMono.filter(isExists -> !isExists);
        existsMono = existsMono.switchIfEmpty(Mono.error(RestServerException
                .withMsg("Add menu[" + request.getName() + "] is exists!",
                        "The menu already exists, please try another name. is params: " + criteria)));
        return existsMono.flatMap((b) -> this.operate(request));
    }

    public Mono<Menu> modify(MenuRequest request) {
        log.debug("Menu modify request: {}", request);
        var oldMunuMono = this.menusRepository.findByCode(request.getCode())
                .switchIfEmpty(Mono.error(RestServerException.withMsg(
                        "Modify menu [" + request.getName() + "] is empty!",
                        "The menu does not exist, please choose another name. is code: " + request.getCode())));
        oldMunuMono = oldMunuMono.flatMap(old -> {
            request.setId(old.getId());
            request.setAuthority(old.getAuthority());
            return this.operate(request);
        });
        return oldMunuMono;
    }

    /**
     * save menu entity
     *
     * @param request menu request
     * @return Mono menu entity
     */
    public Mono<Menu> operate(MenuRequest request) {
        log.debug("Menu operate request: {}", request);
        if (ObjectUtils.isEmpty(request)) {
            return Mono.empty();
        }
        var menu = request.toMenu();
        return this.save(menu).doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Menu> save(Menu menu) {
        if (menu.isNew()) {
            return this.menusRepository.save(menu);
        } else {
            assert menu.getId() != null;
            return this.menusRepository.findById(menu.getId()).flatMap(old -> {
                menu.setCode(old.getCode());
                menu.setCreatedTime(old.getCreatedTime());
                return this.menusRepository.save(menu);
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> delete(MenuRequest request) {
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