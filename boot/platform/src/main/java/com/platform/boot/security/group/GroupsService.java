package com.platform.boot.security.group;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.ContextUtils;
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
public class GroupsService extends AbstractDatabase {

    private final GroupsRepository groupsRepository;

    public Flux<Group> search(GroupRequest request, Pageable pageable) {
        var cacheKey = ContextUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(cacheKey, query, Group.class)
                .flatMap(ContextUtils::userAuditorSerializable);
    }

    public Mono<Page<Group>> page(GroupRequest request, Pageable pageable) {
        var cacheKey = ContextUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var searchMono = this.search(request, pageable).collectList();
        var countMono = this.countWithCache(cacheKey, query, Group.class);
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<Group> operate(GroupRequest request) {
        var dataMono = this.groupsRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toGroup());
        dataMono = dataMono.flatMap(data -> {
            BeanUtils.copyProperties(request, data);
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