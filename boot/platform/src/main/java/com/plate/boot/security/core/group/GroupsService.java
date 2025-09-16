package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Group Service Class
 * Provides core business functions such as create, delete, update, query, pagination query,
 * and conditional search for user groups
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class GroupsService extends AbstractCache {

    private final GroupsRepository groupsRepository;

    /**
     * Search for group information based on conditions
     *
     * @param request  Search condition request object
     * @param pageable Pagination parameters
     * @return Stream of group information that meets the conditions
     */
    public Flux<Group> search(GroupReq request, Pageable pageable) {
        QueryFragment queryFragment = request.query().pageable(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable),
                queryFragment.querySql(), queryFragment, Group.class);
    }

    /**
     * Paginated query of group information
     *
     * @param request  Query condition request object
     * @param pageable Pagination parameters
     * @return Paginated results containing group information
     */
    public Mono<Page<Group>> page(GroupReq request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment queryFragment = request.query();
        var countMono = this.countWithCache(BeanUtils.cacheKey(request), queryFragment.countSql(), queryFragment);

        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    /**
     * Operate on group (create or update)
     *
     * @param request Group operation request object
     * @return Operated group information
     */
    public Mono<Group> operate(GroupReq request) {
        var dataMono = this.groupsRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toGroup());
        dataMono = dataMono.flatMap(data -> {
            BeanUtils.copyProperties(request, data, true);
            return this.save(data);
        });
        return dataMono.doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Delete group
     *
     * @param request Group deletion request object
     * @return Asynchronous response with empty result
     */
    public Mono<Void> delete(GroupReq request) {
        return this.groupsRepository.findByCode(request.getCode())
                .doOnNext(res -> ContextUtils.eventPublisher(GroupEvent.delete(res)))
                .then()
                //.flatMap(this.groupsRepository::delete)
                .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Save group information
     *
     * @param group Group object
     * @return Saved group information
     */
    public Mono<Group> save(Group group) {
        // Add new group
        if (group.isNew()) {
            return this.groupsRepository.save(group)
                    .doOnNext(res -> ContextUtils.eventPublisher(GroupEvent.insert(res)));
        } else {
            // Update group, preserving creation time and code information
            assert group.getId() != null;
            return this.groupsRepository.findById(group.getId()).flatMap(old -> {
                        group.setCreatedAt(old.getCreatedAt());
                        group.setCode(old.getCode());
                        return this.groupsRepository.save(group);
                    })
                    .doOnNext(res -> ContextUtils.eventPublisher(GroupEvent.update(res)));
        }
    }

}