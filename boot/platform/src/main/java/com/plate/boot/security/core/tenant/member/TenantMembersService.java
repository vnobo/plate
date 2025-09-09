package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.user.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service class for handling tenant member operations.
 * Provides methods for searching, paging, saving, and deleting tenant members.
 * Also handles user deletion events to clean up tenant member data.
 * <p>
 * Author: <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class TenantMembersService extends AbstractCache {

    /**
     * Repository for tenant member operations.
     */
    private final TenantMembersRepository tenantMembersRepository;

    /**
     * Searches for tenant members based on the provided request and pageable parameters.
     *
     * @param request  the tenant member request containing search criteria
     * @param pageable the pagination information
     * @return a Flux of TenantMemberRes objects matching the search criteria
     */
    public Flux<TenantMemberRes> search(TenantMemberReq request, Pageable pageable) {
        QueryFragment fragment = request.toParamSql().pageable(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), fragment.querySql(),
                fragment, TenantMemberRes.class);
    }

    /**
     * Retrieves a paginated list of tenant members based on the provided request and pageable parameters.
     *
     * @param request  the tenant member request containing search criteria
     * @param pageable the pagination information
     * @return a Mono of Page containing TenantMemberRes objects
     */
    public Mono<Page<TenantMemberRes>> page(TenantMemberReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment queryFragment = request.toParamSql();
        Mono<Long> countMono = this.countWithCache(BeanUtils.cacheKey(request), queryFragment.countSql(), queryFragment);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Saves or updates a tenant member based on the provided request.
     * If the tenant member does not exist, it creates a new one.
     * Also updates the user's default tenant status.
     *
     * @param request the tenant member request containing the details to save
     * @return a Mono of the saved TenantMember object
     */
    @Transactional(rollbackFor = Exception.class)
    public Mono<TenantMember> operate(TenantMemberReq request) {
        var tenantMemberMono = DatabaseUtils.ENTITY_TEMPLATE.selectOne(Query.query(request.toCriteria()), TenantMember.class)
                .defaultIfEmpty(request.toMemberTenant());
        tenantMemberMono = tenantMemberMono.flatMap(old -> {
            old.setEnabled(true);
            return this.tenantMembersRepository.save(old);
        });
        return userDefaultTenant(request.getUserCode())
                .then(tenantMemberMono).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Updates the default tenant status for a user.
     * Sets all tenant memberships for the user to disabled.
     *
     * @param userCode the unique identifier of the user
     * @return a Mono indicating completion of the update operation
     */
    private Mono<Void> userDefaultTenant(UUID userCode) {
        Query query = Query.query(Criteria.where("userCode").is(userCode));
        Update update = Update.update("enabled", false);
        return DatabaseUtils.ENTITY_TEMPLATE.update(TenantMember.class).matching(query).apply(update).then();
    }

    /**
     * Deletes a tenant member based on the provided request.
     *
     * @param request the tenant member request containing the ID of the tenant member to delete
     * @return a Mono indicating completion of the delete operation
     */
    public Mono<Void> delete(TenantMemberReq request) {
        return this.tenantMembersRepository.delete(request.toMemberTenant());
    }

    /**
     * Handles user deletion events to clean up tenant member data.
     * Deletes tenant members associated with the deleted user.
     *
     * @param event the user deletion event
     */
    @EventListener(value = UserEvent.class, condition = "#event.kind.name() == 'DELETE'")
    public void onUserDeletedEvent(UserEvent event) {
        this.tenantMembersRepository.deleteByUserCode(event.entity().getCode())
                .doAfterTerminate(() -> this.cache.clear())
                .subscribe();
    }
}