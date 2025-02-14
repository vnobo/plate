package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import com.plate.boot.security.core.tenant.member.TenantMembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing tenants.
 * This class provides methods for searching, paginating, operating, deleting, and saving tenants.
 * It uses reactive programming with Project Reactor.
 * <p>
 * The class is annotated with \@Service to indicate that it's a service component in the Spring context.
 * It is also annotated with \@RequiredArgsConstructor to generate a constructor with required arguments.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class TenantsService extends AbstractDatabase {

    private final TenantsRepository tenantsRepository;
    private final TenantMembersRepository membersRepository;

    /**
     * Searches for tenants based on the given request and pageable parameters.
     *
     * @param request  the tenant request containing search criteria
     * @param pageable the pagination information
     * @return a Flux emitting the tenants that match the search criteria
     */
    public Flux<Tenant> search(TenantReq request, Pageable pageable) {
        QueryFragment queryFragment = QueryHelper.query(request, pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), queryFragment.querySql(), queryFragment, Tenant.class);
    }

    /**
     * Paginates the tenants based on the given request and pageable parameters.
     *
     * @param request  the tenant request containing search criteria
     * @param pageable the pagination information
     * @return a Mono emitting a Page of tenants that match the search criteria
     */
    public Mono<Page<Tenant>> page(TenantReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment queryFragment = QueryHelper.query(request, pageable);
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), queryFragment.countSql(), queryFragment);

        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Operates on a tenant based on the given request.
     * If the tenant exists, it updates the tenant; otherwise, it creates a new tenant.
     *
     * @param request the tenant request containing tenant information
     * @return a Mono emitting the operated tenant
     */
    public Mono<Tenant> operate(TenantReq request) {
        var tenantMono = this.tenantsRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toTenant());
        tenantMono = tenantMono.flatMap(data -> {
            BeanUtils.copyProperties(request, data);
            return this.save(data);
        });
        return tenantMono.doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Deletes a tenant based on the given request.
     * It deletes the tenant and its associated members.
     *
     * @param request the tenant request containing tenant information
     * @return a Mono indicating when the deletion is complete
     */
    public Mono<Void> delete(TenantReq request) {
        return Flux.concatDelayError(
                this.tenantsRepository.delete(request.toTenant()),
                this.membersRepository.deleteByTenantCode(request.getCode())).then();
    }

    /**
     * Saves a tenant.
     * If the tenant is new, it creates a new tenant; otherwise, it updates the existing tenant.
     *
     * @param tenant the tenant to be saved
     * @return a Mono emitting the saved tenant
     */
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