package com.platform.boot.security;

import com.platform.boot.commons.base.DatabaseService;
import com.platform.boot.security.group.authority.GroupAuthority;
import com.platform.boot.security.group.member.GroupMember;
import com.platform.boot.security.tenant.member.TenantMember;
import com.platform.boot.security.user.User;
import com.platform.boot.security.user.UsersService;
import com.platform.boot.security.user.authority.UserAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class SecurityManager extends DatabaseService
        implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UsersService usersService;

    @Override
    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        return Mono.just(withNewPassword(user, newPassword))
                .delayUntil((userDetails) -> this.usersService.changePassword(userDetails.getUsername(), newPassword))
                .doAfterTerminate(() -> this.cache.clear());
    }

    private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
        return withUserDetails(userDetails).password(newPassword);
    }

    public static SecurityDetails withUserDetails(UserDetails userDetails) {
        return SecurityDetails.of(userDetails.getUsername(), userDetails.getPassword(), !userDetails.isEnabled(),
                        !userDetails.isAccountNonExpired(),
                        !userDetails.isAccountNonLocked(),
                        !userDetails.isCredentialsNonExpired())
                .authorities(new HashSet<>(userDetails.getAuthorities()));
    }

    /**
     * this login user security details and authorities.
     *
     * @param username 用户名
     * @return Mono<UserDetails> 用户详细信息
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        // 加载用户信息
        var userMono = this.usersService.loadByUsername(username);
        // 加载用户权限信息
        var authoritiesMono = this.authorities(username);
        // 使用 Mono.zip 同时加载用户信息和用户权限信息，并构建用户详细信息
        var tuple2Mono = Mono.zip(userMono, authoritiesMono)
                .flatMap(tuple2 -> buildUserDetails(tuple2.getT1(), new HashSet<>(tuple2.getT2())));
        // 如果出现错误，则抛出 AuthenticationServiceException 异常
        return tuple2Mono.onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(
                throwable.getLocalizedMessage(), throwable)));
    }

    /**
     * Build user details
     *
     * @param user        user
     * @param authorities user authority
     * @return Mono<UserDetails> 用户详细信息
     */
    public Mono<UserDetails> buildUserDetails(User user, Set<GrantedAuthority> authorities) {
        // 构建用户详细信息
        SecurityDetails userDetails = this.withUser(user).authorities(authorities);
        // 使用 Mono.zip 同时加载用户的组和租户信息
        var tuple2Mono = Mono.zip(this.loadGroups(user.getUsername()), this.loadTenants(user.getUsername()));
        // 将组和租户信息设置到用户详细信息中
        return tuple2Mono.flatMap(tuple2 -> {
            userDetails.setGroups(new HashSet<>(tuple2.getT1()));
            userDetails.setTenants(new HashSet<>(tuple2.getT2()));
            return Mono.just(userDetails);
        });
    }

    /**
     * Constructor to create a new SecurityUserDetails instance
     *
     * @param user user
     * @return a new SecurityUserDetails instance
     */
    private SecurityDetails withUser(User user) {
        Assert.isTrue(StringUtils.hasLength(user.getUsername())
                        && StringUtils.hasLength(user.getPassword()) && !ObjectUtils.isEmpty(user.getDisabled()),
                "Cannot pass null or empty values to constructor");
        return SecurityDetails.of(user.getUsername(), user.getPassword(), user.getDisabled(), user.getAccountExpired(),
                user.getAccountLocked(), user.getCredentialsExpired());
    }

    private Mono<List<GroupMember>> loadGroups(String username) {
        String queryGroupMemberSql = """
                select a.id,a.group_code,a.username,b.name as group_name,b.extend as group_extend
                from se_group_members a join se_groups b on a.group_code=b.code
                where a.username ilike :username
                """;
        return this.queryWithCache(Objects.hash("USER_GROUPS", username),
                queryGroupMemberSql, Map.of("username", username), GroupMember.class).collectList();
    }

    private Mono<List<TenantMember>> loadTenants(String username) {
        String queryGroupMemberSql = """
                select a.id,a.tenant_code,a.username,b.name as tenant_name,b.extend as tenant_extend
                from se_tenant_members a join se_tenants b on a.tenant_code=b.code
                where a.username ilike :username
                """;
        return this.queryWithCache(Objects.hash("USER_TENANTS", username),
                queryGroupMemberSql, Map.of("username", username), TenantMember.class).collectList();
    }

    private Mono<List<GrantedAuthority>> authorities(String username) {
        return this.getAuthorities(username)
                .concatWith(this.getGroupAuthorities(username)).distinct().collectList();
    }

    private Flux<GrantedAuthority> getAuthorities(String username) {
        String queryUserAuthoritySql = "select * from se_authorities where username ilike :username";
        return this.queryWithCache(Objects.hash("USER_AUTHORITIES", username),
                        queryUserAuthoritySql, Map.of("username", username), UserAuthority.class)
                .cast(GrantedAuthority.class);
    }


    private Flux<GrantedAuthority> getGroupAuthorities(String username) {
        String queryGroupAuthoritySql = """
                select ga.id,ga.group_code,ga.authority
                from se_group_authorities ga join se_group_members gm on ga.group_code = gm.group_code
                where gm.username ilike :username
                """;
        return this.queryWithCache(Objects.hash("GROUP_AUTHORITIES", username),
                        queryGroupAuthoritySql, Map.of("username", username), GroupAuthority.class)
                .cast(GrantedAuthority.class);
    }

    public Mono<Void> loginSuccess(String username) {
        return this.entityTemplate.update(User.class)
                .matching(Query.query(Criteria.where("username").is(username)))
                .apply(Update.update("loginTime", LocalDateTime.now()))
                .then();
    }

}