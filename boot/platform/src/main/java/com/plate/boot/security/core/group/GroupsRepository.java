package com.plate.boot.security.core.group;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupsRepository extends R2dbcRepository<Group, Integer> {

    /**
     * 根据组代码查询组信息。
     * <p>
     * 本方法旨在通过提供的组代码，从数据库或其他数据源中检索对应的组信息。组代码是唯一标识一个组的标识符。
     *
     * @param code 组代码，作为查询的依据。
     * @return 匹配组代码的组信息的Mono对象。Mono是一个表示单个值（或无值）的Reactive类型，这里用于异步返回组信息。
     */
    Mono<Group> findByCode(String code);
}