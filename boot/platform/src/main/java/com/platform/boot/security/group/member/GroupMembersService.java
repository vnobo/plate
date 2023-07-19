package com.platform.boot.security.group.member;

import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.security.group.GroupsRepository;
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
public class GroupMembersService extends DatabaseService {

    private final GroupMembersRepository memberRepository;

    private final GroupsRepository groupsRepository;

    public Flux<GroupMemberOnly> search(GroupMemberRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(cacheKey, query, GroupMember.class)
                .flatMap(this::serializeOnly);
    }

    public Mono<Page<GroupMemberOnly>> page(GroupMemberRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var searchMono = this.search(request, pageable).collectList();
        var countMono = this.countWithCache(cacheKey, query, GroupMember.class);
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    private Mono<GroupMemberOnly> serializeOnly(GroupMember groupMember) {
        return groupsRepository.findByCode(groupMember.getGroupCode())
                .map(group -> GroupMemberOnly.withGroupMember(groupMember).group(group));
    }

    public Mono<GroupMember> operate(GroupMemberRequest request) {
        var dataMono = this.entityTemplate.selectOne(Query.query(request.toCriteria()), GroupMember.class)
                .defaultIfEmpty(request.toGroupMember());
        return dataMono.flatMap(this::save).doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<GroupMember> save(GroupMember groupMember) {
        if (groupMember.isNew()) {
            return this.memberRepository.save(groupMember);
        } else {
            assert groupMember.getId() != null;
            return this.memberRepository.findById(groupMember.getId())
                    .flatMap(old -> this.memberRepository.save(groupMember));
        }
    }

}