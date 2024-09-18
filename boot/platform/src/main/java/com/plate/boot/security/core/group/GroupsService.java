package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class GroupsService extends AbstractDatabase {

    private final GroupsRepository groupsRepository;

    public Flux<Group> search(GroupRequest request, Pageable pageable) {
        QueryFragment QueryFragment = request.qSql();
        String query = "select * from se_groups" + QueryFragment.whereSql() + QueryHelper.applyPage(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), query, QueryFragment.params(), Group.class);
    }

    public Mono<Page<Group>> page(GroupRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment QueryFragment = request.qSql();
        String query = "select count(*) from se_groups" + QueryFragment.whereSql();
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), query, QueryFragment.params());

        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<Group> operate(GroupRequest request) {
        var dataMono = this.groupsRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toGroup());
        dataMono = dataMono.flatMap(data -> {
            BeanUtils.copyProperties(request, data, true);
            return this.save(data);
        });
        return dataMono.doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Void> delete(GroupRequest request) {
        return this.groupsRepository.delete(request.toGroup());
    }

    public Mono<Group> save(Group group) {
        if (group.isNew()) {
            return this.groupsRepository.save(group);
        } else {
            assert group.getId() != null;
            return this.groupsRepository.findById(group.getId()).flatMap(old -> {
                group.setCreatedTime(old.getCreatedTime());
                group.setCode(old.getCode());
                return this.groupsRepository.save(group);
            });
        }
    }

}