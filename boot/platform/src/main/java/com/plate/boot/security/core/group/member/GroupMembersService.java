package com.plate.boot.security.core.group.member;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
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

    public Flux<GroupMemberResp> search(GroupMemberReq request, Pageable pageable) {
        QueryFragment fragment = request.toParamSql();
        QueryFragment queryFragment = QueryFragment.withColumns("a.*", "b.name as group_name", "b.extend as group_extend",
                        "c.name as login_name", "c.username")
                .from("se_group_members a",
                        "inner join se_groups b on a.group_code = b.code",
                        "inner join se_users c on c.code = a.user_code")
                .where(fragment.getWhere().toString());
        queryFragment.putAll(fragment);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), queryFragment.querySql(),
                queryFragment, GroupMemberResp.class);
    }

    public Mono<Page<GroupMemberResp>> page(GroupMemberReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment fragment = request.toParamSql();
        QueryFragment queryFragment = QueryFragment.withColumns("*")
                .from("se_group_members a",
                        "inner join se_groups b on a.group_code = b.code",
                        "inner join se_users c on c.code = a.user_code")
                .where(fragment.getWhere().toString());
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), queryFragment.countSql(), queryFragment);
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<GroupMember> operate(GroupMemberReq request) {
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

    public Mono<Void> delete(GroupMemberReq request) {
        return this.memberRepository.delete(request.toGroupMember()).doAfterTerminate(() -> this.cache.clear());
    }
}