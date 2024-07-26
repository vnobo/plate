package com.plate.boot.security.core.user;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.BeanUtils;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.query.CriteriaUtils;
import com.plate.boot.commons.utils.query.ParamSql;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
public class UsersService extends AbstractDatabase {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    public Flux<UserResponse> search(UserRequest request, Pageable pageable) {
        var cacheKey = BeanUtils.cacheKey(request, pageable);
        ParamSql paramSql = request.bindParamSql();
        String query = "select * from se_users" + paramSql.whereSql() + CriteriaUtils.applyPage(pageable);
        return super.queryWithCache(cacheKey, query, paramSql.params(), UserResponse.class)
                .flatMapSequential(ContextUtils::serializeUserAuditor);
    }

    public Mono<Page<UserResponse>> page(UserRequest request, Pageable pageable) {
        var searchMono = this.search(request, pageable).collectList();

        var cacheKey = BeanUtils.cacheKey(request);
        ParamSql paramSql = request.bindParamSql();
        String query = "select count(*) from se_users" + paramSql.whereSql();
        var countMono = super.countWithCache(cacheKey, query, paramSql.params());

        return searchMono.zipWith(countMono)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    public Mono<User> loadByCode(String code) {
        var userMono = this.usersRepository.findByCode(code).flux();
        return super.queryWithCache(code, userMono).singleOrEmpty();
    }

    public Mono<User> add(UserRequest request) {
        return this.usersRepository.existsByUsernameIgnoreCase(request.getUsername()).flatMap(exists -> {
            if (exists) {
                return Mono.error(RestServerException.withMsg(417, "User already exists",
                        "Username [" + request.getUsername() + "] already exists!"));
            }
            return this.operate(request);
        });
    }

    public Mono<User> modify(UserRequest request) {
        return this.usersRepository.findByUsername(request.getUsername())
                .switchIfEmpty(Mono.defer(() -> Mono.error(RestServerException.withMsg(417,
                        "User not found!", "User by username [" + request.getUsername() + "] not found!"))))
                .flatMap(user -> {
                    request.setId(user.getId());
                    request.setCode(user.getCode());
                    request.setUsername(user.getUsername());
                    return this.operate(request);
                });
    }

    public Mono<User> operate(UserRequest request) {
        request.setPassword(this.upgradeEncodingIfPassword(request.getPassword()));
        var userMono = this.usersRepository.findByCode(request.getCode()).defaultIfEmpty(request.toUser());
        userMono = userMono.flatMap(user -> {
            BeanUtils.copyProperties(request, user, true);
            return this.save(user);
        });
        return userMono.doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<Void> delete(UserRequest request) {
        return this.usersRepository.delete(request.toUser())
                .doAfterTerminate(() -> this.cache.clear());
    }

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

    private String upgradeEncodingIfPassword(String password) {
        if (StringUtils.hasLength(password) &&
                this.passwordEncoder.upgradeEncoding(password)) {
            return this.passwordEncoder.encode(password);
        }
        return password;
    }

}