package com.platform.boot.security.core.tenant;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface TenantsRepository extends R2dbcRepository<Tenant, Integer> {

    /**
     * 根据租户代码查找租户信息。
     * <p>
     * 本方法旨在通过租户的唯一标识码来检索对应的租户对象。这在多租户系统中是常见的需求，
     * 例如，为了根据租户标识对请求进行路由，或者为了获取特定租户的配置信息。
     *
     * @param code 租户的唯一标识码。这个参数是检索租户的关键，必须是唯一的。
     * @return 匹配的租户对象。如果找不到匹配的租户，则返回空Mono。
     */
    Mono<Tenant> findByCode(String code);
}