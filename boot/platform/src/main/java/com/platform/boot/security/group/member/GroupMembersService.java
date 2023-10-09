package com.platform.boot.security.group.member;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.commons.utils.CriteriaUtils;
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
public class GroupMembersService extends AbstractDatabase {

    private final GroupMembersRepository memberRepository;

    public Flux<GroupMemberResponse> search(GroupMemberRequest request, Pageable pageable) {
        String cacheKey = ContextUtils.cacheKey(request, pageable);
        String query = request.querySql() + request.buildWhereSql() + CriteriaUtils.applyPage(pageable);
        return super.queryWithCache(cacheKey, query,
                BeanUtils.beanToMap(request, true), GroupMemberResponse.class);
    }

    public Mono<Page<GroupMemberResponse>> page(GroupMemberRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();

        String cacheKey = ContextUtils.cacheKey(request);
        String query = request.countSql() + request.buildWhereSql();
        var countMono = this.countWithCache(cacheKey, query, BeanUtils.beanToMap(request, true));

        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
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

    /**
     * Deletes a tenant.
     *
     * @param request the tenant request
     * @return a Mono of void
     */
    public Mono<Void> delete(GroupMemberRequest request) {
        return this.memberRepository.delete(request.toGroupMember()).doAfterTerminate(() -> this.cache.clear());
    }
}