package com.plate.boot.security.core.group.member;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * {@code GroupMembersRepository} is an interface that extends Spring Data R2DBC's {@link R2dbcRepository},
 * specializing in handling database operations for {@link GroupMember} entities. It is designed to provide
 * CRUD functionality for managing group members with the primary key of type {@code Long}.
 * This repository interface enables reactive database access, allowing for efficient, non-blocking operations
 * on group membership data.
 *
 * @see R2dbcRepository
 * @see GroupMember
 */
public interface GroupMembersRepository extends R2dbcRepository<GroupMember, Long> {
}