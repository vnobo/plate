package com.plate.boot.security;

import com.plate.boot.commons.base.AbstractDatabase;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.core.group.authority.GroupAuthority;
import com.plate.boot.security.core.group.member.GroupMemberResponse;
import com.plate.boot.security.core.tenant.member.TenantMemberResponse;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserRequest;
import com.plate.boot.security.core.user.UsersService;
import com.plate.boot.security.core.user.authority.UserAuthority;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
            select ga.* from se_group_authorities ga
            join se_group_members gm on ga.group_code = gm.group_code
            join se_users su on gm.user_code = su.code
            join se_groups sg on gm.group_code = sg.code and sg.tenant_code = su.tenant_code
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

    public Mono<User> registerOrModifyUser(UserRequest request) {
        if (StringUtils.hasLength(request.getCode())) {
            return this.usersService.operate(request);
        }
        return this.usersService.add(request);
    }

    public Mono<User> loadByOauth2(String bindType, String openid) {
        String query = "select * from se_users where extend->'oauth2'->:bindType->>'openid'::varchar = :openid";
        var userMono = this.databaseClient.sql(query)
                .bind("bindType", bindType).bind("openid", openid)
                .map((row, metadata) -> this.r2dbcConverter.read(User.class, row, metadata))
                .all();
        return this.queryWithCache(bindType + openid, userMono).singleOrEmpty();
    }

    public Mono<User> loadByUsername(String username) {
        Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true));
        var userMono = this.entityTemplate.select(query, User.class);
        return this.queryWithCache(username, userMono).singleOrEmpty()
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("登录用户名不存在!")));
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        Mono<Tuple2<User, List<GrantedAuthority>>> userMono = this.loadByUsername(username)
                .zipWhen(user -> this.authorities(user.getCode()));
        Mono<SecurityDetails> userDetailsMono = userMono
                .flatMap(tuple2 -> buildUserDetails(tuple2.getT1(), new HashSet<>(tuple2.getT2())));
        return userDetailsMono.cast(UserDetails.class)
                .onErrorResume(throwable -> Mono.error(new AuthenticationServiceException(
                        throwable.getLocalizedMessage(), throwable)))
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(securityDetails -> this.loginSuccess(securityDetails.getUsername())
                        .subscribe(res -> log.debug("登录成功! 登录信息修改: {}", res)));
    }

    private Mono<SecurityDetails> buildUserDetails(User user, Set<GrantedAuthority> authorities) {
        SecurityDetails userDetails = SecurityDetails.of(user.getCode(), user.getUsername(), user.getName(),
                user.getPassword(), user.getDisabled(), user.getAccountExpired(),
                user.getAccountLocked(), user.getCredentialsExpired(), authorities,
                Map.of("username", user.getUsername()), "username");
        Mono<Tuple2<List<GroupMemberResponse>, List<TenantMemberResponse>>> groupsAndTenantsMono =
                Mono.zipDelayError(this.loadGroups(user.getCode()), this.loadTenants(user.getCode()));
        return groupsAndTenantsMono.doOnNext(tuple2 -> {
            userDetails.setGroups(new HashSet<>(tuple2.getT1()));
            userDetails.setTenants(new HashSet<>(tuple2.getT2()));
        }).then(Mono.just(userDetails));
    }

    private Mono<List<GroupMemberResponse>> loadGroups(String userCode) {
        return this.queryWithCache("USER_GROUPS-" + userCode,
                        QUERY_GROUP_MEMBERS_SQL, Map.of("userCode", userCode), GroupMemberResponse.class)
                .flatMap(ContextUtils::serializeUserAuditor).collectSortedList();
    }

    private Mono<List<TenantMemberResponse>> loadTenants(String userCode) {
        return this.queryWithCache("USER_TENANTS-" + userCode,
                        QUERY_TENANT_MEMBERS_SQL, Map.of("userCode", userCode), TenantMemberResponse.class)
                .flatMap(ContextUtils::serializeUserAuditor).collectSortedList();
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

    private Mono<Long> loginSuccess(String username) {
        Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true));
        Update update = Update.update("loginTime", LocalDateTime.now());
        return this.entityTemplate.update(User.class).matching(query).apply(update);
    }

}