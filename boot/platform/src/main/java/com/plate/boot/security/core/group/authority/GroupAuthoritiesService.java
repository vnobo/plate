package com.plate.boot.security.core.group.authority;


import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class GroupAuthoritiesService extends AbstractCache {

    private final GroupAuthoritiesRepository authoritiesRepository;

    public Flux<GroupAuthority> search(GroupAuthorityReq request, Pageable pageable) {
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), query, GroupAuthority.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Integer> batch(GroupAuthorityReq request) {

        Assert.notNull(request.getGroupCode(), "Authoring Group rules [groupCode] cannot be null");
        var dataMono = this.authoritiesRepository.findByGroupCode(request.getGroupCode()).collectList();

        var nextDataFlux = dataMono.flatMapMany(data -> {

            Set<String> requestAuthorities = request.getAuthorities();
            Map<Boolean, List<GroupAuthority>> groupedData = data.stream()
                    .collect(Collectors.partitioningBy(a -> requestAuthorities.contains(a.getAuthority())));
            List<GroupAuthority> deleteData = groupedData.get(false);

            Set<GroupAuthority> saveData = request.getAuthorities().stream()
                            .filter(a -> data.stream().noneMatch(b -> b.getAuthority().equalsIgnoreCase(a)))
                    .map(a -> new GroupAuthority(request.getGroupCode(), a))
                    .collect(Collectors.toSet());

            var deleteAllMono = deleteData.isEmpty() ? Mono.empty() : this.authoritiesRepository.deleteAll(deleteData);
            var saveAllMono = saveData.isEmpty() ? Mono.empty() : this.authoritiesRepository.saveAll(saveData).then();
            return Flux.concatDelayError(deleteAllMono, saveAllMono);
        });

        return nextDataFlux.then(Mono.fromRunnable(() -> this.cache.clear()))
                .thenReturn(200)
                .publishOn(Schedulers.boundedElastic());
    }

    public Mono<GroupAuthority> operate(GroupAuthorityReq request) {
        var dataMono = DatabaseUtils.ENTITY_TEMPLATE.selectOne(Query.query(request.toCriteria()), GroupAuthority.class)
                .defaultIfEmpty(request.toGroupAuthority());
        return dataMono.flatMap(this::save).doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<GroupAuthority> save(GroupAuthority groupAuthority) {
        if (groupAuthority.isNew()) {
            return this.authoritiesRepository.save(groupAuthority);
        } else {
            assert groupAuthority.getId() != null;
            return this.authoritiesRepository.findById(groupAuthority.getId())
                    .flatMap(old -> this.authoritiesRepository.save(groupAuthority));
        }
    }

    public Mono<Void> delete(GroupAuthorityReq request) {
        return this.authoritiesRepository.delete(request.toGroupAuthority())
                .doAfterTerminate(() -> this.cache.clear());
    }
}