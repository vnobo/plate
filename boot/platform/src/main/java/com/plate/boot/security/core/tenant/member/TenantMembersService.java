package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
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
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class TenantMembersService extends AbstractCache {

    private final TenantMembersRepository tenantMembersRepository;

    public Flux<TenantMemberRes> search(TenantMemberReq request, Pageable pageable) {
        QueryFragment fragment = request.toParamSql();
        QueryFragment queryFragment = QueryFragment.of(pageable.getPageSize(), pageable.getOffset(), fragment)
                .columns("a.*", "b.name as tenant_name",
                        "b.extend as tenant_extend", "c.name as login_name", "c.username")
                .from("se_tenant_members a",
                        "inner join se_tenants b on a.tenant_code = b.code",
                        "inner join se_users c on c.code = a.user_code");
        QueryHelper.applySort(queryFragment, pageable.getSort(), "a");
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), queryFragment.querySql(),
                queryFragment, TenantMemberRes.class);
    }

    public Mono<Page<TenantMemberRes>> page(TenantMemberReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment queryFragment = request.toParamSql();
        Mono<Long> countMono = this.countWithCache(BeanUtils.cacheKey(request), queryFragment.countSql(), queryFragment);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

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

    private Mono<Void> userDefaultTenant(UUID userCode) {
        Query query = Query.query(Criteria.where("userCode").is(userCode));
        Update update = Update.update("enabled", false);
        return DatabaseUtils.ENTITY_TEMPLATE.update(TenantMember.class).matching(query).apply(update).then();
    }

    public Mono<Void> delete(TenantMemberReq request) {
        return this.tenantMembersRepository.delete(request.toMemberTenant());
    }

    @EventListener(value = UserEvent.class, condition = "#event.kind.name() == 'DELETE'")
    public void onUserDeletedEvent(UserEvent event) {
        this.tenantMembersRepository.deleteByUserCode(event.entity().getCode())
                .doAfterTerminate(() -> this.cache.clear())
                .subscribe();
    }
}