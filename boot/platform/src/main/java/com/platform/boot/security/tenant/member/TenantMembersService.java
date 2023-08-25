package com.platform.boot.security.tenant.member;

import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
import lombok.RequiredArgsConstructor;
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

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class TenantMembersService extends DatabaseService {

    private final TenantMembersRepository tenantMembersRepository;

    /**
     * Search for a list of tenant members based on the provided request and pageable parameters.
     *
     * @param request  the request object containing the search criteria
     * @param pageable the pageable object for pagination
     * @return a flux of TenantMemberResponse objects matching the search criteria
     */
    public Flux<TenantMemberResponse> search(TenantMemberRequest request, Pageable pageable) {
        String cacheKey = ContextUtils.cacheKey(request, pageable);
        var parameter = request.buildWhereSql();
        String query = request.querySql() + parameter.getSql() +
                CriteriaUtils.applyPage(pageable, "se_tenant_members");
        return super.queryWithCache(cacheKey, query, parameter.getParams(), TenantMemberResponse.class);
    }

    /**
     * Retrieves a page of TenantMemberResponse objects based on the provided TenantMemberRequest and Pageable.
     *
     * @param request  the TenantMemberRequest object containing the search criteria
     * @param pageable the Pageable object specifying the page size and sorting criteria
     * @return a Mono containing a Page of TenantMemberResponse objects
     */
    public Mono<Page<TenantMemberResponse>> page(TenantMemberRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();

        String cacheKey = ContextUtils.cacheKey(request);
        var parameter = request.buildWhereSql();
        String query = request.countSql() + parameter.getSql();
        Mono<Long> countMono = this.countWithCache(cacheKey, query, parameter.getParams());
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }


    @Transactional(rollbackFor = Exception.class)
    public Mono<TenantMember> operate(TenantMemberRequest request) {
        var tenantMemberMono = this.entityTemplate.selectOne(Query.query(request.toCriteria()), TenantMember.class)
                .defaultIfEmpty(request.toMemberTenant());
        tenantMemberMono = tenantMemberMono.flatMap(old -> {
            old.setEnabled(true);
            return this.tenantMembersRepository.save(old);
        });
        return userDefaultTenant(request.getUserCode())
                .then(tenantMemberMono).doAfterTerminate(() -> this.cache.clear());
    }

    private Mono<Void> userDefaultTenant(String userCode) {
        Query query = Query.query(Criteria.where("userCode").is(userCode));
        Update update = Update.update("enabled", false);
        return entityTemplate.update(TenantMember.class).matching(query).apply(update).then();
    }

    /**
     * Deletes a tenant.
     *
     * @param request the tenant request
     * @return a Mono of void
     */
    public Mono<Void> delete(TenantMemberRequest request) {
        return this.tenantMembersRepository.delete(request.toMemberTenant());
    }
}