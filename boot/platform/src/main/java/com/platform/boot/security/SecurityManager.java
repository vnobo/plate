package com.platform.boot.security;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.security.group.authority.GroupAuthority;
import com.platform.boot.security.group.member.GroupMember;
import com.platform.boot.security.tenant.member.TenantMemberResponse;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Service
@RequiredArgsConstructor
public class SecurityManager extends AbstractDatabase
        implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    private final UsersService usersService;

    @Override
    public Mono<UserDetails> updatePassword(UserDetails user, String newPassword) {
        return Mono.just(withNewPassword(user, newPassword))
                .delayUntil((userDetails) -> this.usersService.changePassword(userDetails.getUsername(), newPassword))
                .doAfterTerminate(() -> this.cache.clear());
    }

    private UserDetails withNewPassword(UserDetails userDetails, String newPassword) {
        SecurityDetails securityDetails = (SecurityDetails) userDetails;
        return securityDetails.password(newPassword);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        var userMono = this.usersService.loadByUsername(username)
                .zipWhen(user -> this.authorities(user.getCode()));

        var tuple2Mono = userMono
                .flatMap(tuple2 -> buildUserDetails(tuple2.getT1(), new HashSet<>(tuple2.getT2())));

        return tuple2Mono.onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(
                throwable.getLocalizedMessage(), throwable)));
    }

    private Mono<UserDetails> buildUserDetails(User user, Set<GrantedAuthority> authorities) {
        // 构建用户详细信息
        SecurityDetails userDetails = SecurityDetails.of(user.getCode(), user.getUsername(), user.getName(),
                user.getPassword(), user.getDisabled(), user.getAccountExpired(),
                user.getAccountLocked(), user.getCredentialsExpired()).authorities(authorities);
        // 使用 Mono.zip 同时加载用户的组和租户信息
        var tuple2Mono = Mono.zip(this.loadGroups(user.getUsername()), this.loadTenants(user.getUsername()));
        // 将组和租户信息设置到用户详细信息中
        return tuple2Mono.flatMap(tuple2 -> {
            userDetails.setGroups(new HashSet<>(tuple2.getT1()));
            userDetails.setTenants(new HashSet<>(tuple2.getT2()));
            return Mono.just(userDetails);
        });
    }

    private Mono<List<GroupMember>> loadGroups(String userCode) {
        String queryGroupMemberSql = """
                select a.*,b.name as group_name,b.extend as group_extend
                from se_group_members a join se_groups b on a.group_code=b.code
                where a.user_code ilike :userCode
                """;
        return this.queryWithCache("USER_GROUPS-" + userCode,
                queryGroupMemberSql, Map.of("userCode", userCode), GroupMember.class).collectList();
    }

    private Mono<List<TenantMemberResponse>> loadTenants(String userCode) {
        String queryGroupMemberSql = """
                select a.*,b.name as tenant_name,b.extend as tenant_extend
                from se_tenant_members a join se_tenants b on a.tenant_code=b.code
                where a.user_code ilike :userCode
                """;
        return this.queryWithCache("USER_TENANTS-" + userCode,
                queryGroupMemberSql, Map.of("userCode", userCode), TenantMemberResponse.class).collectList();
    }

    private Mono<List<GrantedAuthority>> authorities(String userCode) {
        return this.getAuthorities(userCode)
                .concatWith(this.getGroupAuthorities(userCode)).distinct().collectList();
    }

    private Flux<GrantedAuthority> getAuthorities(String userCode) {
        String queryUserAuthoritySql = "select * from se_authorities where user_code = :userCode";
        return this.queryWithCache("USER_AUTHORITIES-" + userCode,
                        queryUserAuthoritySql, Map.of("userCode", userCode), UserAuthority.class)
                .cast(GrantedAuthority.class);
    }

    private Flux<GrantedAuthority> getGroupAuthorities(String userCode) {
        String queryGroupAuthoritySql = """
                select ga.*
                from se_group_authorities ga join se_group_members gm on ga.group_code = gm.group_code
                where gm.user_code = :userCode
                """;
        return this.queryWithCache("GROUP_AUTHORITIES-" + userCode,
                        queryGroupAuthoritySql, Map.of("userCode", userCode), GroupAuthority.class)
                .cast(GrantedAuthority.class);
    }

    public Mono<Void> loginSuccess(String username) {
        return this.entityTemplate.update(User.class)
                .matching(Query.query(Criteria.where("username").is(username)))
                .apply(Update.update("loginTime", LocalDateTime.now()))
                .then();
    }

}