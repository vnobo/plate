package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.group.GroupEvent;
import com.plate.boot.security.core.user.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing group members, providing operations to search, page, operate (update or create),
 * save, and delete group members with reactive support.
 * This service utilizes caching mechanisms to improve performance on frequently accessed data and
 * leverages SQL query fragments for dynamic querying capabilities.
 *
 * @see GroupMembersRepository
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GroupMembersService extends AbstractCache {

    private final GroupMembersRepository memberRepository;

    /**
     * Search for group members based on the provided request criteria and pagination information.
     *
     * @param request  The search criteria for group members
     * @param pageable Pagination information
     * @return A Flux of GroupMemberRes objects matching the search criteria
     */
    public Flux<GroupMemberRes> search(GroupMemberReq request, Pageable pageable) {
        QueryFragment fragment = request.toParamSql().pageable(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), fragment.querySql(),
                fragment, GroupMemberRes.class);
    }

    /**
     * Retrieve group members with pagination support.
     *
     * @param request  The search criteria for group members
     * @param pageable Pagination information
     * @return A Mono containing a Page of GroupMemberRes objects with pagination metadata
     */
    public Mono<Page<GroupMemberRes>> page(GroupMemberReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment fragment = request.toParamSql();
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), fragment.countSql(), fragment);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Operate on a group member (create or update based on existence).
     *
     * @param request The group member operation request
     * @return A Mono containing the operated GroupMember
     */
    public Mono<GroupMember> operate(GroupMemberReq request) {
        var dataMono = DatabaseUtils.ENTITY_TEMPLATE.selectOne(Query.query(request.toCriteria()), GroupMember.class)
                .defaultIfEmpty(request.toGroupMember());
        return dataMono.flatMap(this::save).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Save a group member, handling both creation and update scenarios.
     *
     * @param groupMember The group member to save
     * @return A Mono containing the saved GroupMember
     */
    public Mono<GroupMember> save(GroupMember groupMember) {
        // Create new group member
        if (groupMember.isNew()) {
            return this.memberRepository.save(groupMember);
        } else {
            // Update existing group member
            assert groupMember.getId() != null;
            return this.memberRepository.findById(groupMember.getId())
                    .flatMap(old -> this.memberRepository.save(groupMember));
        }
    }

    /**
     * Delete a group member based on the provided request.
     *
     * @param request The group member deletion request
     * @return A Mono representing completion of the deletion operation
     */
    public Mono<Void> delete(GroupMemberReq request) {
        return this.memberRepository.delete(request.toGroupMember()).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Event listener for user deletion events.
     * Automatically removes group members when a user is deleted.
     *
     * @param event The user deletion event
     */
    @EventListener(value = UserEvent.class, condition = "#event.kind.name() == 'DELETE'")
    public void onUserDeletedEvent(UserEvent event) {
        this.memberRepository.deleteByUserCode(event.entity().getCode())
                .doAfterTerminate(() -> this.cache.clear())
                .subscribe(result -> log.info("Deleted user group for user code: {}," +
                                "result count: {}.", event.entity().getCode(), result),
                        throwable -> log.error("Failed to delete user group for user code: {}",
                                event.entity().getCode(), throwable));
    }

    @EventListener(value = GroupEvent.class, condition = "#event.kind.name() == 'DELETE'")
    public void onUserDeletedEvent(GroupEvent event) {
        this.memberRepository.deleteByGroupCode(event.entity().getCode())
                .doAfterTerminate(() -> this.cache.clear())
                .subscribe(result -> log.info("Deleted group members for group coe: {}," +
                        " result count: {}.", event.entity().getCode(), result));
    }
}