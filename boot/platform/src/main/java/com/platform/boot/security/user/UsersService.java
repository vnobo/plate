package com.platform.boot.security.user;

import com.platform.boot.commons.annotation.exception.RestServerException;
import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.commons.utils.BeanUtils;
import com.platform.boot.commons.utils.ContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class UsersService extends DatabaseService {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    public Flux<User> search(UserRequest request, Pageable pageable) {
        String cacheKey = ContextUtils.cacheKey(request, pageable);
        Query query = Query.query(request.toCriteria()).with(pageable);
        return super.queryWithCache(cacheKey, query, User.class)
                .flatMapSequential(ContextUtils::userAuditorSerializable);
    }

    public Mono<Page<User>> page(UserRequest request, Pageable pageable) {
        String cacheKey = ContextUtils.cacheKey(request);
        Query query = Query.query(request.toCriteria());
        var searchMono = this.search(request, pageable).collectList();
        var countMono = super.countWithCache(cacheKey, query, User.class);
        return Mono.zip(searchMono, countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<User> loadByUsername(String username) {
        var userMono = this.usersRepository.findByUsernameIgnoreCase(username).flux();
        return queryWithCache(username, userMono).next();
    }

    /**
     * Add a user.
     *
     * @param request the request of user.
     * @return a mono of the User
     */
    public Mono<User> add(UserRequest request) {
        return this.usersRepository.existsByUsernameIgnoreCase(request.getUsername()).flatMap(exists -> {
            if (exists) {
                return Mono.error(RestServerException.withMsg(1101, "User already exists",
                        "Username [" + request.getUsername() + "] already exists!"));
            }
            return this.operate(request);
        });
    }

    /**
     * Operates on the given {@link UserRequest} and returns the {@link Mono} of {@link User}.
     * If the request contains a password, it is encoded before being saved.
     * The cache is also cleared after the operation is finished.
     *
     * @param request The {@link UserRequest} to be operated on
     * @return A {@link Mono} of the saved {@link User}
     */
    public Mono<User> operate(UserRequest request) {
        request.setPassword(this.upgradeEncodingIfPassword(request.getPassword()));
        var userMono = this.usersRepository.findByCode(request.getCode())
                .defaultIfEmpty(request.toUser());
        userMono = userMono.flatMap(user -> {
            BeanUtils.copyProperties(request, user);
            return this.save(user);
        });
        return userMono.doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Void> delete(UserRequest request) {
        return this.usersRepository.delete(request.toUser())
                .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * This method is used to change the password of the user with the given username to the given new password. It will also
     * invalidate the cache once the password has been changed.
     *
     * @param username    The username of the user whose password is to be changed.
     * @param newPassword The new password to be set for the given user.
     * @return A {@link Mono} indicating the completion of the operation.
     */
    public Mono<Void> changePassword(String username, String newPassword) {
        return this.usersRepository.changePassword(username, newPassword).then()
                .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Saves the given User entity.
     * If the user is new, it is inserted into the repository.
     * If the user is existing, it is updated in the repository.
     * Properties such as createdTime and password will be ignored during the update.
     *
     * @param user The User entity to be saved.
     * @return A Mono containing the saved user.
     */
    public Mono<User> save(User user) {
        if (user.isNew()) {
            return this.usersRepository.save(user);
        } else {
            assert user.getId() != null;
            return this.usersRepository.findById(user.getId())
                    .switchIfEmpty(Mono.error(RestServerException.withMsg(1404, "User not found",
                            "User by id [" + user.getId() + "] not found!")))
                    .flatMap(old -> {
                        user.setCreatedTime(old.getCreatedTime());
                        user.setPassword(old.getPassword());
                        user.setAccountExpired(old.getAccountExpired());
                        user.setAccountLocked(old.getAccountLocked());
                        user.setCredentialsExpired(old.getCredentialsExpired());
                        return this.usersRepository.save(user);
                    });
        }
    }

    /* This method upgrades the encoding of the user password if necessary.
     *
     * @param request The user request containing the authentication data.
     */
    private String upgradeEncodingIfPassword(String password) {
        // Check if password exists and the encoding needs to be upgraded
        if (StringUtils.hasLength(password) &&
                this.passwordEncoder.upgradeEncoding(password)) {
            // Encode the password
            return this.passwordEncoder.encode(password);
        }
        return password;
    }

}