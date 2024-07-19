package com.plate.authorization.security.core.user;

import com.plate.authorization.commons.base.AbstractDatabase;
import com.plate.authorization.commons.exception.RestServerException;
import com.plate.authorization.commons.utils.BeanUtils;
import com.plate.authorization.commons.utils.query.CriteriaUtils;
import com.plate.authorization.commons.utils.query.ParamSql;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class UsersService extends AbstractDatabase {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    @Cacheable("users")
    public List<UserResponse> search(UserRequest request, Pageable pageable) {
        ParamSql paramSql = request.bindParamSql();
        String query = "select * from se_users" + paramSql.whereSql() + CriteriaUtils.applyPage(pageable);
        return super.jdbcClient.sql(query).params(paramSql.params())
                .query(UserResponse.class).list();
    }

    @Cacheable("users")
    public Page<UserResponse> page(UserRequest request, Pageable pageable) {
        var searchList = this.search(request, pageable);
        ParamSql paramSql = request.bindParamSql();
        String query = "select count(*) from se_users" + paramSql.whereSql();
        var countMono = super.jdbcClient.sql(query).params(paramSql.params()).query(Long.class)
                .optional().orElse(0L);
        return new PageImpl<>(searchList, pageable, countMono);
    }

    public User loadByCode(String code) {
        var userMono = this.usersRepository.findByCode(code);
        return userMono.orElse(null);
    }

    @CachePut("users")
    public User add(UserRequest request) {
        Boolean exists = this.usersRepository.existsByUsernameIgnoreCase(request.getUsername());
        if (exists) {
            throw RestServerException.withMsg(417, "User already exists",
                    "Username [" + request.getUsername() + "] already exists!");
        }
        return this.operate(request);
    }

    @CachePut("users")
    public User modify(UserRequest request) {
        User user = this.usersRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> RestServerException.withMsg(404, "User not found!",
                        "User by username [" + request.getUsername() + "] not found!"));
        request.setId(user.getId());
        request.setCode(user.getCode());
        request.setUsername(user.getUsername());
        return this.operate(request);
    }

    @CachePut("users")
    public User operate(UserRequest request) {
        request.setPassword(this.upgradeEncodingIfPassword(request.getPassword()));
        var user = this.usersRepository.findByCode(request.getCode()).orElse(request.toUser());
        BeanUtils.copyProperties(request, user, true);
        return this.save(user);
    }

    @CacheEvict("users")
    public void delete(UserRequest request) {
        this.usersRepository.delete(request.toUser());
    }

    public User save(User user) {
        if (!user.isNew()) {
            assert user.getId() != null;
            User old = this.usersRepository.findById(user.getId())
                    .orElseThrow(() -> RestServerException.withMsg(404, "User not found",
                            "User by id [" + user.getId() + "] not found!"));
            user.setCreatedTime(old.getCreatedTime());
            user.setPassword(old.getPassword());
            user.setAccountExpired(old.getAccountExpired());
            user.setAccountLocked(old.getAccountLocked());
            user.setCredentialsExpired(old.getCredentialsExpired());
        }
        return this.usersRepository.save(user);
    }

    private String upgradeEncodingIfPassword(String password) {
        if (StringUtils.hasLength(password) &&
                this.passwordEncoder.upgradeEncoding(password)) {
            return this.passwordEncoder.encode(password);
        }
        return password;
    }

}