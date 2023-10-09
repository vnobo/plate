package com.platform.boot.security.user.authority;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class UserAuthoritiesService extends AbstractDatabase {

    private final UserAuthoritiesRepository userAuthoritiesRepository;

    public Flux<UserAuthority> search(UserAuthorityRequest request) {
        String cacheKey = ContextUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        return super.queryWithCache(cacheKey, query, UserAuthority.class);
    }

    public Mono<UserAuthority> operate(UserAuthorityRequest request) {
        var dataMono = this.entityTemplate.selectOne(Query.query(request.toCriteria()), UserAuthority.class);
        return dataMono.switchIfEmpty(this.save(request.toAuthority())).doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Void> delete(UserAuthorityRequest request) {
        return this.userAuthoritiesRepository.delete(request.toAuthority()).doAfterTerminate(() -> this.cache.clear());
    }

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