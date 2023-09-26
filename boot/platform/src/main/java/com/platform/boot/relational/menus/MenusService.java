package com.platform.boot.relational.menus;


import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.group.authority.GroupAuthoritiesRepository;
import com.platform.boot.security.user.authority.UserAuthoritiesRepository;
import lombok.RequiredArgsConstructor;
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
 * MenusService is a class to provide service operations for menus.
 * It uses the MenusRepository for database operations.
 *
 * @author billb
 */
@Service
@RequiredArgsConstructor
public class MenusService extends DatabaseService {
    public final static String AUTHORITY_PREFIX = "ROLE_";

    private final MenusRepository menusRepository;
    private final GroupAuthoritiesRepository groupAuthoritiesRepository;
    private final UserAuthoritiesRepository userAuthoritiesRepository;

    /**
     * This method searches for Menu objects based on the MenuRequest.
     * It creates a query with the given request, sorts the query by 'sort' and finds Menus with the Query.
     *
     * @param request the request object with criteria to search
     * @return a Flux of Menus with the search criteria
     */
    public Flux<Menu> search(MenuRequest request) {
        String cacheKey = ContextUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria()).sort(Sort.by("sort"));
        return this.queryWithCache(cacheKey, query, Menu.class);
    }

    /**
     * This method adds a new Menu to the database.
     * It first checks if any existing Menus have the same tenantCode and authority as the new Menu.
     * If a Menu already exists with the same tenantCode and authority, this method will throw an error.
     * Otherwise, it will call the operate method with the new Menu.
     *
     * @param request the request object with criteria to add
     * @return a Mono of Menu with the new request
     */
    public Mono<Menu> add(MenuRequest request) {
        Criteria criteria = MenuRequest.of(request.getTenantCode(), request.getAuthority()).toCriteria();
        return this.entityTemplate.exists(Query.query(criteria), Menu.class).filter(isExists -> !isExists)
                .switchIfEmpty(Mono.error(RestServerException
                        .withMsg("Add menu[" + request.getName() + "] is exists",
                                "Menu already exists, Please choose another name. is params: " + criteria)))
                .flatMap((b) -> this.operate(request));
    }

    /**
     * This method modifies an existing Menu in the database.
     * It first finds the Menu with the given code.
     * If the Menu does not exist, this method will throw an error.
     * Otherwise, it will update the Menu with the request and then call the operate method to save it.
     *
     * @param request the request object with criteria to modify
     * @return a Mono of Menu with the modified request
     */
    public Mono<Menu> modify(MenuRequest request) {
        var oldMunuMono = this.menusRepository.findByCode(request.getCode())
                .switchIfEmpty(Mono.error(RestServerException.withMsg(
                        "Modify menu [" + request.getName() + "] is empty",
                        "Menu does not exist, Please choose another name. is code: " + request.getCode())));
        oldMunuMono = oldMunuMono.flatMap(old -> {
            request.setId(old.getId());
            request.setAuthority(old.getAuthority());
            return this.operate(request);
        });
        return oldMunuMono;
    }

    /**
     * This method saves a Menu to the database.
     * If the Menu is new, this method will save it to the database.
     * Otherwise, it will find the existing Menu and update it with the new Menu.
     *
     * @param request the request object with criteria to save
     * @return a Mono of Menu with the saved request
     */
    public Mono<Menu> operate(MenuRequest request) {
        return this.save(request.toMenu()).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * This method saves a Menu to the database.
     * It checks if the Menu is new, and then saves or updates the Menu accordingly.
     *
     * @param menu the Menu object to be saved
     * @return a Mono of Menu with the saved object
     */
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

    /**
     * This method deletes a Menu from the database.
     * It uses the MenuRequest to find the Menu to be deleted and then deletes it.
     *
     * @param request the request object with criteria to delete
     * @return a Mono of void
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> delete(MenuRequest request) {
        List<String> rules = new ArrayList<>(Collections.singletonList(request.getAuthority()));
        if (!ObjectUtils.isEmpty(request.getPermissions())) {
            rules.addAll(request.getPermissions().stream().map(Menu.Permission::getAuthority).toList());
        }
        var deleteAuthorityMono = Flux.concatDelayError(this.groupAuthoritiesRepository.deleteByAuthorityIn(rules),
                this.userAuthoritiesRepository.deleteByAuthorityIn(rules));
        var deleteNextMono = Flux.concatDelayError(this.menusRepository.delete(request.toMenu()), deleteAuthorityMono);
        return deleteNextMono.then().doAfterTerminate(() -> this.cache.clear());
    }
}