package com.plate.boot.security.core.user;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.query.QueryFragment;
import com.plate.boot.commons.utils.query.QueryHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class UsersService extends AbstractDatabase {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    public Flux<UserResponse> search(UserRequest request, Pageable pageable) {
        QueryFragment QueryFragment = request.querySql(List.of());
        String query = "select * from se_users" + QueryFragment.whereSql() + QueryHelper.applyPage(pageable);
        return super.queryWithCache(BeanUtils.cacheKey(request, pageable), query,
                QueryFragment.params(), UserResponse.class);
    }

    public Mono<Page<UserResponse>> page(UserRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();
        QueryFragment QueryFragment = request.querySql(List.of());
        String query = "select count(*) from se_users" + QueryFragment.whereSql();
        var countMono = super.countWithCache(BeanUtils.cacheKey(request), query, QueryFragment.params());
        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<User> loadByCode(String code) {
        var userMono = this.usersRepository.findByCode(code).flux();
        return super.queryWithCache(code, userMono).singleOrEmpty();
    }

    /**
     * Adds a new user based on the provided UserRequest.
     *
     * <p>This method first checks if a user with the same username already exists in the database.
     * If an existing user is found, a {@link RestServerException} is thrown indicating that the
     * user already exists. If no existing user is found, the request is processed through the
     * {@link #operate(UserRequest)} method to create and save the new user entity.</p>
     *
     * @param request A UserRequest object containing the details for the new user, including the username which must be unique.
     * @return A Mono that, when subscribed to, emits the newly created User entity upon successful addition,
     * or errors with a RestServerException if the user already exists.
     */
    public Mono<User> add(UserRequest request) {
        return this.usersRepository.existsByUsernameIgnoreCase(request.getUsername()).flatMap(exists -> {
            if (exists) {
                return Mono.error(RestServerException.withMsg("User already exists",
                        "Username [" + request.getUsername() + "] already exists!"));
            }
            return this.operate(request);
        });
    }

    /**
     * Modifies an existing user based on the provided UserRequest.
     * <p>
     * This method first attempts to find a user by their username in the repository. If the user is not found,
     * a {@link RestServerException} is thrown indicating the absence of the user. Upon finding the user, the
     * request's ID, code, and username are set to match the found user's details (though these typically would
     * be redundant operations given the nature of the method). The modified user is then processed through the
     * {@link #operate(UserRequest)} method to apply any necessary updates defined in the request.
     *
     * @param request A UserRequest object containing the updated information for the user, primarily identified by their username.
     * @return A Mono that, when subscribed to, emits the updated User entity after modification or throws an exception if the user was not found.
     */
    public Mono<User> modify(UserRequest request) {
        return this.usersRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.defer(() -> Mono.error(RestServerException.withMsg(
                        "User not found!", "User by username [" + request.getUsername() + "] not found!"))))
                .flatMap(user -> {
                    request.setId(user.getId());
                    request.setCode(user.getCode());
                    request.setUsername(user.getUsername());
                    return this.operate(request);
                });
    }

    /**
     * Operates on a UserRequest to process and persist user data.
     *
     * <p>This method enhances the incoming UserRequest by upgrading the password encoding if necessary.
     * It then attempts to find an existing user by code from the repository. If the user does not exist,
     * the request is converted into a new User entity. Subsequently, the request's data is copied onto
     * the found or created user, and the user is saved. Finally, the operation triggers a cache clearance
     * to ensure data consistency.</p>
     *
     * @param request The UserRequest containing the data to operate on, including the code for user identification
     *                and the password that may require encoding upgrade.
     *
     * @return A Mono emitting the updated or newly created User after the operation is completed.
     */
    public Mono<User> operate(UserRequest request) {
        request.setPassword(this.upgradeEncodingIfPassword(request.getPassword()));
        var userMono = this.usersRepository.findByCode(request.getCode()).defaultIfEmpty(request.toUser());
        userMono = userMono.flatMap(user -> {
            BeanUtils.copyProperties(request, user, true);
            return this.save(user);
        });
        return userMono.doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Deletes a user based on the provided UserRequest.
     *
     * @param request A UserRequest object encapsulating the details necessary to identify the user for deletion.
     * @return A Mono<Void> which upon subscription initiates the deletion process asynchronously.
     *         The Mono will complete empty when the deletion is successful, or error if the operation fails.
     */
    public Mono<Void> delete(UserRequest request) {
        return this.usersRepository.delete(request.toUser())
                .doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Saves a user entity. Determines whether the user is new or existing and handles the save operation accordingly.
     * For new users, it directly saves the entity. For existing users, it fetches the old entity, merges selected fields,
     * and then saves the updated entity.
     *
     * @param user The user entity to be saved. Must not be null.
     * @return A Mono emitting the saved User entity after the operation completes successfully.
     *         If the user is not found during update, a Mono error with RestServerException is returned.
     */
    public Mono<User> save(User user) {
        if (user.isNew()) {
            return this.usersRepository.save(user);
        } else {
            assert user.getId() != null;
            return this.usersRepository.findById(user.getId())
                    .switchIfEmpty(Mono.error(RestServerException.withMsg("User not found",
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

    /**
     * Checks if the provided password requires an upgrade in its encoding and upgrades it if necessary.
     * This method utilizes an instance of a PasswordEncoder to determine if the encoding upgrade is needed.
     * If the password is non-empty and the encoder indicates an upgrade is required, the password is re-encoded
     * before being returned. Otherwise, the original password is returned as is.
     *
     * @param password The password string that may need its encoding upgraded.
     * @return The upgraded password if an upgrade was necessary; otherwise, the original password.
     */
    private String upgradeEncodingIfPassword(String password) {
        if (StringUtils.hasLength(password) &&
                this.passwordEncoder.upgradeEncoding(password)) {
            return this.passwordEncoder.encode(password);
        }
        return password;
    }

}