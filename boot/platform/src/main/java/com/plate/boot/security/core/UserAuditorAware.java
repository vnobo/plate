package com.plate.boot.security.core;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UsersRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implements the {@link ReactiveAuditorAware} interface to provide auditor information
 * about the current user within a reactive context. This class utilizes security details
 * to fetch and wrap them into a {@link UserAuditor} instance, enabling tracking of user actions
 * in an asynchronous environment.
 * <p>
 * The {@link #getCurrentAuditor()} method retrieves the auditor details, mapping them directly
 * from the security context, ensuring that each audit interaction is associated with the correct user.
 *
 * @see ReactiveAuditorAware For the contract on providing auditor information reactively.
 * @see UserAuditor For the structure containing user audit details.
 */
@Log4j2
@Component
public class UserAuditorAware implements ReactiveAuditorAware<UserAuditor> {

    private final UsersRepository usersRepository;
    private final Cache cache;

    public UserAuditorAware(UsersRepository usersRepository, CacheManager cacheManager) {
        var cacheName = this.getClass().getName().concat(".cache");
        this.usersRepository = usersRepository;
        this.cache = cacheManager.getCache(cacheName);
        if (this.cache != null) {
            this.cache.clear();
            log.debug("Initializing UserAuditorAware [{}] cache names: {}",
                    cache.getNativeCache().getClass().getSimpleName(), cache.getName());
        }

    }

    /**
     * Retrieves the current auditor information as a {@link UserAuditor} wrapped in a non-null {@link Mono}.
     * This method accesses the security details from the execution context and maps them
     * to a {@link UserAuditor} instance using the {@link UserAuditor#withDetails(SecurityDetails)} method.
     * It is particularly useful for auditing user actions within a reactive workflow.
     *
     * @return A {@link Mono} emitting the {@link UserAuditor} representing the current auditor details,
     * or an empty Mono if the auditor cannot be determined.
     */
    @Override
    public @NonNull Mono<UserAuditor> getCurrentAuditor() {
        return ContextUtils.securityDetails().map(UserAuditor::withDetails);
    }


    /**
     * Loads a user by their unique code using a cached from.
     * <p>
     * This method retrieves a user from the repository based on the provided code.
     * It utilizes a cached from to improve performance, reducing the need for repeated database hits
     * for the same code. If a user with the given code is found, it is emitted as a single value in a {@link Mono}.
     * If no user is found, an empty {@link Mono} is returned.
     *
     * @param code The unique code used to identify the user.
     * @return A {@link Mono} that emits a single {@link User} object if found, or an empty {@link Mono} if no user matches the code.
     */
    public Mono<UserAuditor> loadByCode(UUID code) {
        UserAuditor userAuditor = this.cache.get(code, () -> null);
        return Mono.justOrEmpty(userAuditor).switchIfEmpty(this.usersRepository.findByCode(code).map(UserAuditor::withUser)
                        .doOnNext(sourceData -> this.cache.put(code, sourceData)));
    }


}