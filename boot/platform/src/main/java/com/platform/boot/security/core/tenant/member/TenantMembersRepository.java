package com.platform.boot.security.core.tenant.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface TenantMembersRepository extends R2dbcRepository<TenantMember, Long> {

    /**
     * 根据租户代码删除数据。
     * <p>
     * 此方法通过Mono<Integer>返回类型，表明它是一个异步操作，用于删除与特定租户代码关联的数据。
     * Mono表示操作可能返回0或1个元素，这里返回的是被删除的记录数。
     *
     * @param tenantCode 租户的唯一标识码。这个参数用于确定要删除哪些数据，是执行删除操作的关键条件。
     * @return Mono<Integer> 表示异步操作的结果，即被删除的记录数。
     */
    Mono<Integer> deleteByTenantCode(String tenantCode);
}