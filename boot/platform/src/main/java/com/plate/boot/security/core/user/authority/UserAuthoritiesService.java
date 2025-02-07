package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for managing user authorities.
 * This class provides methods for searching, operating, deleting, and saving user authorities.
 * It uses reactive programming with Project Reactor.
 * <p>
 * The class is annotated with \@Service to indicate that it's a service component in the Spring context.
 * It is also annotated with \@RequiredArgsConstructor to generate a constructor with required arguments.
 * <p>
 * \@author
 * <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class UserAuthoritiesService extends AbstractDatabase {

    private final UserAuthoritiesRepository userAuthoritiesRepository;

    /**
     * Searches for user authorities based on the given request parameters.
     *
     * @param request the user authority request containing search criteria
     * @return a Flux emitting the user authorities that match the search criteria
     */
    public Flux<UserAuthority> search(UserAuthorityReq request) {
        Query query = Query.query(request.toCriteria()).sort(Sort.by("id").descending());
        return super.queryWithCache(BeanUtils.cacheKey(request), query, UserAuthority.class);
    }

    /**
     * Operates on a user authority based on the given request.
     * If the user authority exists, it updates the user authority; otherwise, it creates a new user authority.
     *
     * @param request the user authority request containing user authority information
     * @return a Mono emitting the operated user authority
     */
    public Mono<UserAuthority> operate(UserAuthorityReq request) {
        var dataMono = this.entityTemplate.selectOne(Query.query(request.toCriteria()), UserAuthority.class);
        dataMono = dataMono.switchIfEmpty(Mono.defer(() -> this.save(request.toAuthority())));
        return dataMono.doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Deletes a user authority based on the given request.
     *
     * @param request the user authority request containing user authority information
     * @return a Mono indicating when the deletion is complete
     */
    public Mono<Void> delete(UserAuthorityReq request) {
        return this.userAuthoritiesRepository.delete(request.toAuthority())
                .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Saves a user authority.
     * If the user authority is new, it creates a new user authority; otherwise, it updates the existing user authority.
     *
     * @param userAuthority the user authority to be saved
     * @return a Mono emitting the saved user authority
     */
    public Mono<UserAuthority> save(UserAuthority userAuthority) {
        if (userAuthority.isNew()) {
            return this.userAuthoritiesRepository.save(userAuthority);
        } else {
            assert userAuthority.getId() != null;
            return this.userAuthoritiesRepository.findById(userAuthority.getId())
                    .flatMap(old -> this.userAuthoritiesRepository.save(userAuthority));
        }
    }

}