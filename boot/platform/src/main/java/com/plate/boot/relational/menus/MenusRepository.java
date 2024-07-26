package com.plate.boot.relational.menus;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface MenusRepository extends R2dbcRepository<Menu, Integer> {


    /**
     * 根据代码查询菜单信息的函数。
     * <p>
     * 本函数通过Mono类型返回一个菜单对象，该对象包含特定代码的菜单信息。
     * 使用Reactive Streams API的设计，使得这个函数可以在异步环境中优雅地工作，
     * 适用于那些需要对数据流进行反应式处理的应用场景。
     *
     * @param code 菜单的唯一标识代码，用于查询特定的菜单。
     * @return 包含特定代码菜单信息的Mono对象。如果找不到匹配的菜单，则Mono对象为空。
     */
    Mono<Menu> findByCode(String code);


    /**
     * 根据权限标识删除角色。
     * <p>
     * 本方法通过Mono<Long>返回类型，表明它执行的是一个异步操作，特别适用于基于Reactor的响应式编程场景。
     * 它的目的是从数据库中删除具有特定权限标识的角色。Mono表示操作的结果是一个单一的值，这里是一个Long类型的主键，
     * 代表被删除的角色的ID。这种方法适用于使用Spring Data Reactor进行数据库操作的情况。
     *
     * @param authority 要删除的角色的权限标识。这是一个字符串参数，用于精确匹配角色的权限。
     * @return 返回一个Mono对象，该对象在成功删除角色后会包含被删除角色的ID。如果删除失败或未找到匹配的角色，
     * Mono可能会触发一个错误事件。这个返回值使得调用者可以以反应式方式处理删除操作的结果。
     */
    Mono<Long> deleteByAuthority(String authority);
}