package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.relational.menus.MenuEvent;
import com.plate.boot.security.core.user.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

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
@Log4j2
@Service
@RequiredArgsConstructor
public class UserAuthoritiesService {

    private final UserAuthoritiesRepository authoritiesRepository;

    /**
     * Searches for user authorities based on the given request parameters.
     *
     * @param request the user authority request containing search criteria
     * @return a Flux emitting the user authorities that match the search criteria
     */
    @Cacheable(cacheNames = "user-authorities", key = "T(com.plate.boot.commons.utils.BeanUtils).cacheKey(#request)")
    public Flux<UserAuthority> search(UserAuthorityReq request) {
        Query query = Query.query(request.toCriteria()).sort(Sort.by("id").descending());
        return DatabaseUtils.query(query, UserAuthority.class);
    }

    /**
     * Operates on a user authority based on the given request.
     * If an authority with the same {@code userCode} and {@code authority} already exists,
     * it is returned unchanged; otherwise a new user authority is created via {@link #save(UserAuthority)}.
     *
     * @param request the user authority request containing user authority information
     * @return a Mono emitting the existing or newly created user authority
     */
    @CacheEvict(cacheNames = "user-authorities", allEntries = true)
    public Mono<UserAuthority> operate(UserAuthorityReq request) {
        return this.authoritiesRepository
                .findByUserCodeAndAuthority(request.getUserCode(), request.getAuthority())
                .switchIfEmpty(Mono.defer(() -> this.save(request.toAuthority())));
    }

    /**
     * Deletes a user authority based on the given request.
     *
     * @param request the user authority request containing user authority information
     * @return a Mono indicating when the deletion is complete
     */
    @CacheEvict(cacheNames = "user-authorities", allEntries = true)
    public Mono<Void> delete(UserAuthorityReq request) {
        return this.authoritiesRepository.findById(request.getId())
                .flatMap(this.authoritiesRepository::delete);
    }

    /**
     * Saves a user authority.
     * If the user authority is new, it creates a new user authority; otherwise, it updates the existing user authority.
     *
     * @param userAuthority the user authority to be saved
     * @return a Mono emitting the saved user authority
     */
    @CacheEvict(cacheNames = "user-authorities", allEntries = true)
    public Mono<UserAuthority> save(UserAuthority userAuthority) {
        if (userAuthority.isNew()) {
            return this.authoritiesRepository.save(userAuthority);
        } else {
            if (userAuthority.getId() == null) {
                return Mono.error(RestServerException.withMsg("Id must not be null!",
                        new IllegalArgumentException("User authority ID must not be null for existing entities")));
            }
            return this.authoritiesRepository.findById(userAuthority.getId())
                    .flatMap(old -> {
                        userAuthority.setCode(old.getCode());
                        userAuthority.setCreatedAt(old.getCreatedAt());
                        userAuthority.setCreatedBy(old.getCreatedBy());
                        return this.authoritiesRepository.save(userAuthority);
                    });
        }
    }

    /**
     * Event listener method that is triggered when a user is deleted.
     * It deletes all user authorities associated with the deleted user.
     *
     * @param event the user event containing information about the deleted user
     */
    @EventListener(value = UserEvent.class, condition = "#event.kind.name() == 'DELETE'")
    @CacheEvict(cacheNames = "user-authorities", allEntries = true)
    public void onUserDeletedEvent(UserEvent event) {
        this.authoritiesRepository.deleteByUserCode(event.getEntity().getCode())
                .subscribe(result -> log.info("Deleted user authorities for user code: {}," +
                                "result count: {}.", event.getEntity().getCode(), result),
                        throwable -> log.error("Failed to delete user authorities for user code: {}",
                                event.getEntity().getCode(), throwable));
    }

    /**
     * Event listener triggered when a menu is deleted.
     * It removes every user authority whose {@code authority} matches the deleted menu's authority,
     * and clears the {@code user-authorities} cache.
     *
     * @param event the menu event carrying the deleted menu's authority
     */
    @EventListener(value = MenuEvent.class, condition = "#event.kind.name() == 'DELETE'")
    @CacheEvict(cacheNames = "user-authorities", allEntries = true)
    public void onMenuDeletedEvent(MenuEvent event) {
        this.authoritiesRepository.deleteByAuthorityIn(Set.of(event.getEntity().getAuthority()))
                .subscribe(res -> log.debug("Deleted user authorities by authority [{}], " +
                        "result count [{}].", event.getEntity().getCode(), res));
    }
}