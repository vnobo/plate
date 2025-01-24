package com.plate.boot.security.core.group.authority;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupAuthoritiesRepository extends R2dbcRepository<GroupAuthority, Integer> {

    /**
     * 根据权限集合删除用户。
     * <p>
     * 本函数通过Mono封装了异步删除操作，它接收一个权限集合作为参数，旨在删除所有拥有这些权限的用户。
     * 使用Reactive编程模型可以提高并发处理的效率，并且能够优雅地处理异步数据流。
     *
     * @param authorities 要删除的用户的权限集合。通过权限集合来定位需要删除的用户，这是因为权限通常是一种有效的用户标识方式。
     * @return Mono<Integer> 表示异步操作的结果，返回被删除的用户数量。Mono是一种Reactive Streams类型，用于表示0或1个元素的序列。
     */
    Mono<Integer> deleteByAuthorityIn(Collection<String> authorities);

    /**
     * 根据组代码查询权限信息。
     * <p>
     * 本方法通过指定的组代码查询与之相关的权限组。这允许系统根据用户的组成员身份，
     * 动态确定用户具有哪些权限。这对于实现基于角色的访问控制（RBAC）是非常重要的。
     *
     * @param groupCode 组的唯一标识码。这个参数是用来筛选权限组的关键依据。
     * @return 返回一个Flux对象，该对象包含满足条件的GroupAuthority实体。Flux是一个响应式流，
     * 允许以非阻塞方式处理异步数据序列。
     */
    Flux<GroupAuthority> findByGroupCode(UUID groupCode);
}