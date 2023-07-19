package com.platform.boot.security.group.authority;


import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.BeanUtils;
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
public class GroupAuthoritiesService extends DatabaseService {

    private final GroupAuthoritiesRepository authoritiesRepository;

    public Flux<GroupAuthority> search(GroupAuthorityRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(cacheKey, query, GroupAuthority.class);
    }

    public Mono<Page<GroupAuthority>> page(GroupAuthorityRequest request, Pageable pageable) {
        String cacheKey = BeanUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var searchMono = this.search(request, pageable).collectList();
        var countMono = super.countWithCache(cacheKey, query, GroupAuthority.class);
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
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