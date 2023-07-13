package com.platform.boot.security.group.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
public interface GroupMembersRepository extends R2dbcRepository<GroupMember, Long> {

}