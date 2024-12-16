package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
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
public class TenantMembersService extends AbstractDatabase {
    private final static String QUERY_SQL = """
            select a.*, b.name as tenant_name, b.extend as tenant_extend,c.name as login_name,c.username
            from se_tenant_members a
            inner join se_tenants b on a.tenant_code = b.code
            inner join se_users c on c.code = a.user_code
            """;
    private final static String COUNT_SQL = """
            select count(*) from se_tenant_members a
            inner join se_tenants b on a.tenant_code = b.code
            inner join se_users c on c.code = a.user_code
            """;

    private final TenantMembersRepository tenantMembersRepository;

    public Flux<TenantMemberResponse> search(TenantMemberRequest request, Pageable pageable) {
        QueryFragment queryFragment = QueryHelper.query(request, pageable, "a");
        queryFragment = queryFragment.merge(request.toParamSql());
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), queryFragment.querySql(),
                queryFragment, TenantMemberResponse.class);
    }

    public Mono<Page<TenantMemberResponse>> page(TenantMemberRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment queryFragment = request.toParamSql();
        String query = COUNT_SQL + queryFragment.whereSql();
        Mono<Long> countMono = this.countWithCache(BeanUtils.cacheKey(request), query, queryFragment);
        return searchMono.zipWith(countMono)
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

    public Mono<Void> delete(TenantMemberRequest request) {
        return this.tenantMembersRepository.delete(request.toMemberTenant());
    }
}