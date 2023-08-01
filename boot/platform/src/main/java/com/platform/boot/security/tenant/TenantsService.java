package com.platform.boot.security.tenant;

import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.tenant.member.TenantMembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class TenantsService extends DatabaseService {

    private final TenantsRepository tenantsRepository;
    private final TenantMembersRepository membersRepository;

    /**
     * @param request  租户请求对象
     * @param pageable 分页参数对象
     * @return 包含租户信息的Flux对象
     */
    public Flux<Tenant> search(TenantRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request, pageable);
        // 使用Java 17中的var关键字，类型推断更加简洁
        var query = Query.query(request.toCriteria()).with(pageable);
        // 使用Java 17中的新方法of，避免使用Tuple2
        return super.queryWithCache(cacheKey, query, Tenant.class).flatMap(ContextUtils::userAuditorSerializable);
    }

    /**
     * @param request  租户请求对象
     * @param pageable 分页参数对象
     * @return 包含租户信息的Mono对象
     */
    public Mono<Page<Tenant>> page(TenantRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        // 使用Java 17中的var关键字，类型推断更加简洁
        var tenantsMono = this.search(request, pageable).collectList();
        var countMono = this.countWithCache(cacheKey, query, Tenant.class);
        // 使用Java 17中的新方法ofTuple，避免使用Tuple2
        return Mono.zip(tenantsMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2())); // 强制类型转换
    }

    /**
     * 对租户进行操作，根据请求参数中的租户编码查找租户，如果找到则更新租户信息，否则创建新租户。
     * 操作完成后清空缓存。
     *
     * @param request 租户请求参数
     * @return Mono<Tenant> 返回操作后的租户信息
     */
    public Mono<Tenant> operate(TenantRequest request) {
        var tenantMono = this.tenantsRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toTenant());
        tenantMono = tenantMono.flatMap(data -> {
            BeanUtils.copyProperties(request, data);
            return this.save(data);
        });
        return tenantMono.doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Deletes a tenant.
     *
     * @param request the tenant request
     * @return a Mono of void
     */
    public Mono<Void> delete(TenantRequest request) {
        return Flux.concatDelayError(
                this.tenantsRepository.delete(request.toTenant()),
                this.membersRepository.deleteByTenantCode(request.getCode())).then();
    }

    /**
     * Saves a tenant.
     *
     * @param tenant the tenant to be saved
     * @return a Mono of Tenant
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