package com.platform.boot.security;

import com.platform.boot.commons.base.AbstractDatabase;
import com.platform.boot.commons.utils.ContextUtils;
import com.platform.boot.security.core.group.authority.GroupAuthority;
import com.platform.boot.security.core.group.member.GroupMemberResponse;
import com.platform.boot.security.core.tenant.member.TenantMemberResponse;
import com.platform.boot.security.core.user.User;
import com.platform.boot.security.core.user.UserRequest;
import com.platform.boot.security.core.user.UsersService;
import com.platform.boot.security.core.user.authority.UserAuthority;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SecurityManager extends AbstractDatabase
        implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
    private final static String QUERY_GROUP_MEMBERS_SQL = """
            select a.*,b.name,b.extend
            from se_group_members a join se_groups b on a.group_code=b.code
            where a.user_code like :userCode
            """;
    private final static String QUERY_TENANT_MEMBERS_SQL = """
            select a.*,b.name ,b.extend
            from se_tenant_members a join se_tenants b on a.tenant_code=b.code
            where a.user_code like :userCode
            """;
    private final static String QUERY_USER_AUTHORITY_SQL = """
            select * from se_authorities where user_code = :userCode
            """;
    private final static String QUERY_GROUP_AUTHORITY_SQL = """
            select ga.*
            from se_group_authorities ga join se_group_members gm on ga.group_code = gm.group_code
            where gm.user_code = :userCode
            """;

    private final UsersService usersService;

    @Override
    public Mono<UserDetails> updatePassword(UserDetails userDetails, String newPassword) {
        SecurityDetails securityDetails = (SecurityDetails) userDetails;
        securityDetails.password(newPassword);
        Query query = Query.query(Criteria.where("username").is(userDetails.getUsername()).ignoreCase(true));
        Update update = Update.update("password", newPassword);
        return this.entityTemplate.update(User.class).matching(query).apply(update)
                .flatMap(result -> Mono.just((UserDetails) securityDetails))
                .doAfterTerminate(() -> this.cache.clear());
    }

    public Mono<SecurityDetails> register(UserRequest request) {
        var userMono = this.usersService.add(request)
                .zipWhen(user -> this.authorities(user.getCode()));

        var userDetailsMono = userMono
                .flatMap(tuple2 -> buildUserDetails(tuple2.getT1(), new HashSet<>(tuple2.getT2())));

        return userDetailsMono
                .onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(
                        throwable.getLocalizedMessage(), throwable)));

    }

    public Mono<User> loadByUsername(String username) {
        Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true));
        var userMono = this.entityTemplate.select(query, User.class);
        return queryWithCache(username, userMono).singleOrEmpty();
    }

    public Mono<User> loadByOauth2(String bindType, String openid) {
        String query = "select * from se_users where extend->'oauth2'->:bindType->>'openid'::varchar = :openid";
        var userMono = this.databaseClient.sql(query)
                .bind("bindType", bindType).bind("openid", openid)
                .map((row, metadata) -> this.r2dbcConverter.read(User.class, row, metadata))
                .all();
        return queryWithCache(bindType + openid, userMono).singleOrEmpty();
    }

    public Mono<SecurityDetails> findByOauth2(String bindType, String openid) {
        return this.loadByOauth2(bindType, openid).map(user ->
                SecurityDetails.of(user.getCode(), user.getUsername(), user.getName(),
                        user.getPassword(), user.getDisabled(), user.getAccountExpired(),
                        user.getAccountLocked(), user.getCredentialsExpired(),
                        Set.of(new SimpleGrantedAuthority("RULE_USER")),
                        Map.of("username", user.getUsername())));
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        var userMono = this.loadByUsername(username)
                .zipWhen(user -> this.authorities(user.getCode()));

        var userDetailsMono = userMono
                .flatMap(tuple2 -> buildUserDetails(tuple2.getT1(), new HashSet<>(tuple2.getT2())));

        return userDetailsMono.cast(UserDetails.class)
                .onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(
                        throwable.getLocalizedMessage(), throwable)));
    }

    private Mono<SecurityDetails> buildUserDetails(User user, Set<GrantedAuthority> authorities) {
        // 构建用户详细信息
        SecurityDetails userDetails = SecurityDetails.of(user.getCode(), user.getUsername(), user.getName(),
                user.getPassword(), user.getDisabled(), user.getAccountExpired(),
                user.getAccountLocked(), user.getCredentialsExpired(), authorities,
                Map.of("username", user.getUsername()));
        // 使用 Mono.zip 同时加载用户的组和租户信息
        var tuple2Mono = Mono.zip(this.loadGroups(user.getCode()), this.loadTenants(user.getCode()));
        // 将组和租户信息设置到用户详细信息中
        return tuple2Mono.flatMap(tuple2 -> {
            userDetails.setGroups(new HashSet<>(tuple2.getT1()));
            userDetails.setTenants(new HashSet<>(tuple2.getT2()));
            return Mono.just(userDetails);
        });
    }

    private Mono<List<GroupMemberResponse>> loadGroups(String userCode) {
        return this.queryWithCache("USER_GROUPS-" + userCode,
                        QUERY_GROUP_MEMBERS_SQL, Map.of("userCode", userCode), GroupMemberResponse.class)
                .flatMap(ContextUtils::serializeUserAuditor).collectList();
    }

    private Mono<List<TenantMemberResponse>> loadTenants(String userCode) {
        return this.queryWithCache("USER_TENANTS-" + userCode,
                        QUERY_TENANT_MEMBERS_SQL, Map.of("userCode", userCode), TenantMemberResponse.class)
                .flatMap(ContextUtils::serializeUserAuditor).collectList();
    }

    private Mono<List<GrantedAuthority>> authorities(String userCode) {
        return this.getAuthorities(userCode)
                .concatWith(this.getGroupAuthorities(userCode))
                .flatMap(ContextUtils::serializeUserAuditor).distinct().collectList();
    }

    private Flux<GrantedAuthority> getAuthorities(String userCode) {
        return this.queryWithCache("USER_AUTHORITIES-" + userCode,
                        QUERY_USER_AUTHORITY_SQL, Map.of("userCode", userCode), UserAuthority.class)
                .cast(GrantedAuthority.class);
    }

    private Flux<GrantedAuthority> getGroupAuthorities(String userCode) {
        return this.queryWithCache("GROUP_AUTHORITIES-" + userCode,
                        QUERY_GROUP_AUTHORITY_SQL, Map.of("userCode", userCode), GroupAuthority.class)
                .cast(GrantedAuthority.class);
    }

    public Mono<Void> loginSuccess(String username) {
        Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true));
        Update update = Update.update("loginTime", LocalDateTime.now());
        return this.entityTemplate.update(User.class).matching(query).apply(update).then();
    }

}