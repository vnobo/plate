package com.platform.boot.security.group.authority;


import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class GroupAuthoritiesService extends AbstractDatabase {

    private final GroupAuthoritiesRepository authoritiesRepository;

    public Flux<GroupAuthority> search(GroupAuthorityRequest request, Pageable pageable) {

        var cacheKey = ContextUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);

        return super.queryWithCache(cacheKey, query, GroupAuthority.class)
                .flatMap(ContextUtils::userAuditorSerializable);
    }

    public Mono<Page<GroupAuthority>> page(GroupAuthorityRequest request, Pageable pageable) {

        var searchMono = this.search(request, pageable).collectList();

        var cacheKey = ContextUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var countMono = super.countWithCache(cacheKey, query, GroupAuthority.class);

        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Integer> batch(GroupAuthorityRequest request) {

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

    public Mono<GroupAuthority> operate(GroupAuthorityRequest request) {
        var dataMono = this.entityTemplate.selectOne(Query.query(request.toCriteria()), GroupAuthority.class)
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

    public Mono<Void> delete(GroupAuthorityRequest request) {
        return this.authoritiesRepository.delete(request.toGroupAuthority())
                .doAfterTerminate(() -> this.cache.clear());
    }
}