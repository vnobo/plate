package com.platform.boot.relational.menus;


import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.commons.base.AbstractDatabase;
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
public class MenusService extends AbstractDatabase {
    public final static String AUTHORITY_PREFIX = "ROLE_";

    private final MenusRepository menusRepository;
    private final GroupAuthoritiesRepository groupAuthoritiesRepository;
    private final UserAuthoritiesRepository userAuthoritiesRepository;

    public Flux<Menu> search(MenuRequest request) {
        var cacheKey = ContextUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria()).sort(Sort.by("id").descending());
        return this.queryWithCache(cacheKey, query, Menu.class)
                .flatMap(ContextUtils::serializeUserAuditor);
    }

    public Mono<Menu> add(MenuRequest request) {
        Criteria criteria = MenuRequest.of(request.getTenantCode(), request.getAuthority()).toCriteria();
        return this.entityTemplate.exists(Query.query(criteria), Menu.class).filter(isExists -> !isExists)
                .switchIfEmpty(Mono.error(RestServerException
                        .withMsg("Add menu[" + request.getName() + "] is exists",
                                "The menu already exists, please try another name. is params: " + criteria)))
                .flatMap((b) -> this.operate(request));
    }

    public Mono<Menu> modify(MenuRequest request) {
        var oldMunuMono = this.menusRepository.findByCode(request.getCode())
                .switchIfEmpty(Mono.error(RestServerException.withMsg(
                        "Modify menu [" + request.getName() + "] is empty",
                        "The menu does not exist, please choose another name. is code: " + request.getCode())));
        oldMunuMono = oldMunuMono.flatMap(old -> {
            request.setId(old.getId());
            request.setAuthority(old.getAuthority());
            return this.operate(request);
        });
        return oldMunuMono;
    }

    public Mono<Menu> operate(MenuRequest request) {
        return this.save(request.toMenu()).doAfterTerminate(() -> this.cache.clear());
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