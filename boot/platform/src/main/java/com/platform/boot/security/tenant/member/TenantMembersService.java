package com.platform.boot.security.tenant.member;

import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.security.tenant.TenantsRepository;
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
    private final TenantsRepository tenantsRepository;

    public Flux<TenantMemberOnly> search(TenantMemberRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(cacheKey, query, TenantMember.class)
                .flatMapSequential(this::serializeOnly);
    }

    public Mono<Page<TenantMemberOnly>> page(TenantMemberRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var searchMono = this.search(request, pageable).collectList();
        Mono<Long> countMono = this.countWithCache(cacheKey, query, TenantMember.class);
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    private Mono<TenantMemberOnly> serializeOnly(TenantMember tenantMember) {
        return this.tenantsRepository.findByCode(tenantMember.getTenantCode())
                .map(tenant -> TenantMemberOnly.withTenantMember(tenantMember).tenant(tenant));
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<TenantMember> operate(TenantMemberRequest request) {
        var tenantMemberMono = this.entityTemplate.selectOne(Query.query(request.toCriteria()), TenantMember.class)
                .defaultIfEmpty(request.toMemberTenant());
        tenantMemberMono = tenantMemberMono.flatMap(old -> {
            old.setEnabled(true);
            return this.save(old);
        });
        return userDefaultTenant(request.getUsername())
                .then(tenantMemberMono).doAfterTerminate(() -> this.cache.clear());
    }

    private Mono<Void> userDefaultTenant(String username) {
        Query query = Query.query(Criteria.where("username").is(username));
        Update update = Update.update("enabled", false);
        return entityTemplate.update(TenantMember.class).matching(query).apply(update).then();
    }

    public Mono<TenantMember> save(TenantMember tenantMember) {
        if (tenantMember.isNew()) {
            return this.tenantMembersRepository.save(tenantMember);
        } else {
            assert tenantMember.getId() != null;
            return this.tenantMembersRepository.findById(tenantMember.getId())
                    .flatMap(old -> this.tenantMembersRepository.save(tenantMember));
        }
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