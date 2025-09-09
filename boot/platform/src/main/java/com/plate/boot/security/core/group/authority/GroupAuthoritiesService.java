package com.plate.boot.security.core.group.authority;


import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Group Authorities Service
 * Provides core business functions for managing group authorities including search, operate (create or update),
 * save, and delete operations with reactive support. This service utilizes caching mechanisms to improve
 * performance on frequently accessed data.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class GroupAuthoritiesService extends AbstractCache {

    private final GroupAuthoritiesRepository authoritiesRepository;

    /**
     * Search for group authorities based on the provided request criteria and pagination information
     *
     * @param request  The search criteria for group authorities
     * @param pageable Pagination information
     * @return A Flux of GroupAuthority objects matching the search criteria
     */
    public Flux<GroupAuthority> search(GroupAuthorityReq request, Pageable pageable) {
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), query, GroupAuthority.class);
    }

    /**
     * Operate on a group authority (create or update based on existence)
     *
     * @param request The group authority operation request
     * @return A Mono containing the operated GroupAuthority
     */
    public Mono<GroupAuthority> operate(GroupAuthorityReq request) {
        return this.authoritiesRepository
                .findByGroupCodeAndAuthority(request.getGroupCode(), request.getAuthority())
                .switchIfEmpty(Mono.defer(() -> this.save(request.toGroupAuthority())))
                .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Save a group authority, handling both creation and update scenarios
     *
     * @param groupAuthority The group authority to save
     * @return A Mono containing the saved GroupAuthority
     */
    public Mono<GroupAuthority> save(GroupAuthority groupAuthority) {
        // Create new group authority
        if (groupAuthority.isNew()) {
            return this.authoritiesRepository.save(groupAuthority);
        } else {
            // Update existing group authority
            assert groupAuthority.getId() != null;
            return this.authoritiesRepository.findById(groupAuthority.getId())
                    .flatMap(old -> this.authoritiesRepository.save(groupAuthority));
        }
    }

    /**
     * Delete a group authority based on the provided request
     *
     * @param request The group authority deletion request
     * @return A Mono representing completion of the deletion operation
     */
    public Mono<Void> delete(GroupAuthorityReq request) {
        return this.authoritiesRepository.delete(request.toGroupAuthority())
                .doAfterTerminate(() -> this.cache.clear());
    }
}