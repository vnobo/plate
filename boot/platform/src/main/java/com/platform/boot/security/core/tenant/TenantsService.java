package com.platform.boot.security.core.tenant;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.commons.utils.query.CriteriaUtils;
import com.platform.boot.commons.utils.query.ParamSql;
import com.platform.boot.security.core.tenant.member.TenantMembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class TenantsService extends AbstractDatabase {

    private final TenantsRepository tenantsRepository;

    private final TenantMembersRepository membersRepository;

    public Flux<Tenant> search(TenantRequest request, Pageable pageable) {
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        ParamSql paramSql = request.bindParamSql();
        String query = "select * from se_tenants" + paramSql.whereSql() + CriteriaUtils.applyPage(pageable);
        return super.queryWithCache(cacheKey, query, paramSql.params(), Tenant.class)
                .flatMapSequential(ContextUtils::serializeUserAuditor);
    }

    public Mono<Page<Tenant>> page(TenantRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();

        var cacheKey = BeanUtils.cacheKey(request);
        ParamSql paramSql = request.bindParamSql();
        String query = "select count(*) from se_tenants" + paramSql.whereSql() + CriteriaUtils.applyPage(pageable);
        var countMono = this.countWithCache(cacheKey, query, paramSql.params());

        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<Tenant> operate(TenantRequest request) {
        var tenantMono = this.tenantsRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toTenant());
        tenantMono = tenantMono.flatMap(data -> {
            BeanUtils.copyProperties(request, data);
            return this.save(data);
        });
        return tenantMono.doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Void> delete(TenantRequest request) {
        return Flux.concatDelayError(
                this.tenantsRepository.delete(request.toTenant()),
                this.membersRepository.deleteByTenantCode(request.getCode())).then();
    }

    public Mono<Tenant> save(Tenant tenant) {
        if (tenant.isNew()) {
            return this.tenantsRepository.save(tenant);
        } else {
            assert tenant.getId() != null;
            return this.tenantsRepository.findById(tenant.getId()).flatMap(old -> {
                tenant.setCreatedTime(old.getCreatedTime());
                tenant.setCode(old.getCode());
                return this.tenantsRepository.save(tenant);
            });
        }
    }
}