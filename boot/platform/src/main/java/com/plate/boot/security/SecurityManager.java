package com.plate.boot.security;

import com.plate.boot.commons.base.AbstractCache;
import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.group.authority.GroupAuthority;
import com.plate.boot.security.core.group.member.GroupMemberRes;
import com.plate.boot.security.core.tenant.member.TenantMemberRes;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import com.plate.boot.security.core.user.UsersService;
import com.plate.boot.security.core.user.authority.UserAuthority;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * SecurityManager is a service class that extends the functionality of AbstractDatabase
 * to provide security-related operations, such as user details management, password updates,
 * and authority handling for reactive applications. It integrates with database operations
 * asynchronously and supports caching mechanisms for improved performance.
 * This class also serves as a ReactiveUserDetailsService and ReactiveUserDetailsPasswordService,
 * implementing methods to fetch user details by username and update user passwords respectively.
 * <p>
 * Key Responsibilities:
 * - Manage user registration and modification through integration with UsersService.
 * - Retrieve user information based on OAuth2 bindings for authentication purposes.
 * - Load user details by username, including associated roles and permissions.
 * - Cache from results to minimize direct database hits for frequently accessed data.
 * - Handle password updates in a secure manner, clearing related caches post-update.
 * - Populate SecurityDetails objects with comprehensive user and authorization data.
 * <p>
 * Dependencies:
 * - UsersService: For CRUD operations on user entities.
 * - R2DBC (reactive database access): To execute SQL queries asynchronously.
 * - Cache: Utilized for storing from results to improve response times on subsequent requests.
 * <p>
 * Note: The class uses reactive types (Mono, Flux) to facilitate non-blocking, asynchronous processing.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SecurityManager extends AbstractCache implements ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {

    /**
     * OAuth2 bind type pattern for validation
     */
    private static final Pattern OAUTH2_BIND_TYPE_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * Cache key prefixes
     */
    private static final String CACHE_KEY_OAUTH2_PREFIX = "OAUTH2_";
    private static final String CACHE_KEY_USER_GROUPS_PREFIX = "USER_GROUPS-";
    private static final String CACHE_KEY_USER_TENANTS_PREFIX = "USER_TENANTS-";
    private static final String CACHE_KEY_USER_AUTHORITIES_PREFIX = "USER_AUTHORITIES-";
    private static final String CACHE_KEY_GROUP_AUTHORITIES_PREFIX = "GROUP_AUTHORITIES-";

    /**
     * Default cache duration
     */
    private static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(10);

    /**
     * Creates a new QueryFragment for querying group members
     */
    private QueryFragment createGroupMembersFragment() {
        return QueryFragment.from("se_group_members a", "join se_groups b on a.group_code=b.code")
                .column("a.*", "b.name", "b.extend").where("a.user_code = :userCode");
    }

    /**
     * Creates a new QueryFragment for querying tenant members
     */
    private QueryFragment createTenantMembersFragment() {
        return QueryFragment.from("se_tenant_members a", "join se_tenants b on a.tenant_code=b.code")
                .column("a.*", "b.name", "b.extend").where("a.user_code = :userCode");
    }

    /**
     * Creates a new QueryFragment for querying user authorities
     */
    private QueryFragment createUserAuthorityFragment() {
        return QueryFragment.from("se_authorities").column("*")
                .where("user_code = :userCode");
    }

    /**
     * Creates a new QueryFragment for querying group authorities
     */
    private QueryFragment createGroupAuthorityFragment() {
        return QueryFragment.from("se_group_authorities ga",
                        "join se_group_members gm on ga.group_code = gm.group_code",
                        "join se_users su on gm.user_code = su.code",
                        "join se_groups sg on gm.group_code = sg.code and sg.tenant_code = su.tenant_code")
                .column("ga.*").where("gm.user_code = :userCode");
    }

    /**
     * Represents the service layer for handling user-related operations.
     * This field provides access to methods responsible for CRUD operations,
     * authentication, and other business logic associated with users.
     */
    private final UsersService usersService;

    /**
     * Updates the password for a given user's UserDetails.
     *
     * @param userDetails The current UserDetails of the user whose password is to be updated.
     * @param newPassword The new password that will replace the existing one.
     * @return A Mono emitting the updated UserDetails with the new password set.
     */
    @Override
    public @NonNull Mono<@NonNull UserDetails> updatePassword(UserDetails userDetails, String newPassword) {
        Query query = Query.query(Criteria.where("username").is(userDetails.getUsername()).ignoreCase(true));
        Update update = Update.update("password", newPassword);
        return DatabaseUtils.ENTITY_TEMPLATE.update(query, update, User.class)
                .flatMap(_ -> Mono.just(userDetails)).doAfterTerminate(() -> this.cache.clear());
    }

    /**
     * Registers a new user or modifies an existing one based on the provided user request.
     *
     * @param request A UserReq object containing the details necessary to register or update a user.
     *                If the request contains a non-empty 'code', it will be treated as a modification request.
     * @return A Mono emitting the updated or newly registered User instance upon successful operation.
     */
    public Mono<@NonNull User> registerOrModifyUser(UserReq request) {
        if (ObjectUtils.isEmpty(request.getCode())) {
            return this.usersService.add(request);
        }
        return this.usersService.operate(request);
    }

    /**
     * Loads a User by OAuth2 binding information.
     * <p>
     * This method queries the database for a user record based on the provided OAuth2 binding type and the corresponding openid.
     * It leverages R2DBC for reactive SQL operations and employs caching to optimize subsequent identical requests.
     *
     * @param bindType The type of OAuth2 binding used (e.g., 'google', 'facebook').
     * @param openid   The unique identifier provided by the OAuth2 provider for the user.
     * @return A Mono emitting the User if found, or an empty Mono if no user matches the given OAuth2 binding data.
     */
    public Mono<@NonNull User> loadByOauth2(String bindType, String openid) {
        if (!StringUtils.hasText(bindType) || !OAUTH2_BIND_TYPE_PATTERN.matcher(bindType).matches()
                || !StringUtils.hasText(openid)) {
            return Mono.error(new IllegalArgumentException("Invalid bindType or openid parameter"));
        }
        QueryFragment queryFragment = QueryFragment.from("se_users").column("*")
                .where("extend->'oauth2'->:bindType->>'openid'::varchar = :openid");
        queryFragment.put("bindType", bindType);
        queryFragment.put("openid", openid);
        var userFlux = DatabaseUtils.query(queryFragment.querySql(), queryFragment, User.class);
        String cacheKey = CACHE_KEY_OAUTH2_PREFIX + bindType + "_" + openid;
        return this.queryWithCache(cacheKey, userFlux).singleOrEmpty();
    }

    /**
     * Loads a user by their username in a case-insensitive manner.
     * Utilizes reactive programming to asynchronously fetch the user details.
     *
     * @param username The username of the user to be loaded.
     * @return A Mono emitting the User object if found, or an empty Mono otherwise.
     */
    public Mono<@NonNull User> loadByUsername(String username) {
        Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true));
        var userFlux = DatabaseUtils.query(query, User.class);
        return this.queryWithCache(username, userFlux).singleOrEmpty().switchIfEmpty(Mono.defer(() ->
                Mono.error(new UsernameNotFoundException("Login user does not exist, username: " + username))));
    }

    /**
     * Locates a user based on the provided username.
     * This method is an override that enhances the original functionality by fetching user details
     * and populating them with granted authorities, error handling, threading, and logging upon success.
     *
     * @param username The unique username of the user to be located.
     * @return A Mono emitting the UserDetails object representing the found user, or an error signal if not found.
     * The emitted object includes user information along with their granted authorities.
     * In case of failure, an AuthenticationServiceException is propagated with a relevant message.
     */
    @Override
    public @NonNull Mono<@NonNull UserDetails> findByUsername(@NonNull String username) {
        var userMono = this.loadByUsername(username).zipWhen(user -> this.authorities(user.getCode()));
        var userDetailsMono = userMono.flatMap(tuple2 ->
                buildUserDetails(tuple2.getT1(), new HashSet<>(tuple2.getT2())));
        return userDetailsMono.cast(UserDetails.class).onErrorResume(UsernameNotFoundException.class, Mono::error)
                .onErrorResume(throwable -> Mono.defer(() ->
                        Mono.error(new BadCredentialsException(throwable.getMessage(), throwable))))
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(details -> this.loginSuccess(details.getUsername())
                        .subscribe(res -> log.debug("Login successful! Login info modified: {}", res)))
                .doOnError(throwable -> log.error("User {} login failed: {}", username, throwable.getMessage()));
    }

    /**
     * Constructs a SecurityDetails object based on the provided User and GrantedAuthority set.
     * Additionally, asynchronously loads and attaches the user's group and tenant memberships.
     *
     * @param user        The User entity containing details necessary for security context construction.
     * @param authorities A set of GrantedAuthority representing the user's permissions.
     * @return A Mono emitting the fully constructed SecurityDetails object, including group and tenant memberships.
     */
    private Mono<@NonNull SecurityDetails> buildUserDetails(User user, Set<GrantedAuthority> authorities) {
        if (ObjectUtils.isEmpty(user.getCode())) {
            return Mono.error(new BadCredentialsException("Invalid user code!"));
        }
        SecurityDetails userDetails = SecurityDetails.of(user, authorities, Map.of("username", user.getUsername(),
                "userCode", user.getCode()));
        var groupsAndTenantsMono = Mono.zipDelayError(this.loadGroups(user.getCode()), this.loadTenants(user.getCode()));
        return groupsAndTenantsMono.doOnNext(tuple2 -> {
            userDetails.setGroups(new HashSet<>(tuple2.getT1()));
            userDetails.setTenants(new HashSet<>(tuple2.getT2()));
        }).then(Mono.just(userDetails));
    }

    /**
     * Loads a list of group members associated with a given user code asynchronously.
     * Utilizes caching to enhance performance on subsequent calls with the same user code.
     * The data is fetched from the database using a predefined SQL from and then serialized
     * with user auditor context before being collected and sorted into a list.
     *
     * @param userCode The unique code identifying the user whose groups are to be loaded.
     * @return A Mono emitting a sorted list of {@link GroupMemberRes} representing the user's group memberships,
     * once the asynchronous operation completes successfully.
     */
    private Mono<@NonNull List<GroupMemberRes>> loadGroups(UUID userCode) {
        QueryFragment fragment = createGroupMembersFragment();
        fragment.put("userCode", userCode);
        String cacheKey = CACHE_KEY_USER_GROUPS_PREFIX + userCode;
        return this.queryWithCache(cacheKey, fragment.querySql(), fragment, GroupMemberRes.class).collectSortedList();
    }

    /**
     * Loads a list of tenant members associated with a given user code.
     * The data is fetched using a cached from with specific SQL and parameters,
     * then serialized with user auditor context, and finally collected into a sorted list.
     *
     * @param userCode The unique code identifying the user whose tenant members are to be loaded.
     * @return A Mono that, when subscribed to, emits a sorted list of {@link TenantMemberRes} objects representing the tenant members.
     */
    private Mono<@NonNull List<TenantMemberRes>> loadTenants(UUID userCode) {
        QueryFragment fragment = createTenantMembersFragment();
        fragment.put("userCode", userCode);
        String cacheKey = CACHE_KEY_USER_TENANTS_PREFIX + userCode;
        return this.queryWithCache(cacheKey, fragment.querySql(), fragment, TenantMemberRes.class).collectSortedList();
    }

    /**
     * Retrieves a combined list of GrantedAuthority for a given user code.
     * This method fetches the user's direct authorities and then concatenates them with
     * the authorities derived from the user's groups, ensuring no duplicates.
     *
     * @param userCode The unique identifier for the user whose authorities are to be fetched.
     * @return A Mono that, when subscribed to, emits a List containing all distinct GrantedAuthority
     * instances granted to the user, including both individual and group authorities.
     */
    private Mono<@NonNull List<GrantedAuthority>> authorities(UUID userCode) {
        return this.getAuthorities(userCode).concatWith(this.getGroupAuthorities(userCode)).distinct().collectList();
    }

    /**
     * Retrieves a flux of GrantedAuthority objects for a given user code.
     *
     * @param userCode The unique identifier for the user whose authorities are to be fetched.
     * @return A Flux emitting GrantedAuthority instances representing the authorities assigned to the user.
     */
    private Flux<@NonNull GrantedAuthority> getAuthorities(UUID userCode) {
        QueryFragment fragment = createUserAuthorityFragment();
        fragment.put("userCode", userCode);
        String cacheKey = CACHE_KEY_USER_AUTHORITIES_PREFIX + userCode;
        return this.queryWithCache(cacheKey, fragment.querySql(), fragment, UserAuthority.class).cast(GrantedAuthority.class);
    }

    /**
     * Retrieves a stream of authorities associated with the specified user group code.
     *
     * @param userCode The unique code identifying the user's group.
     * @return A {@link Flux} emitting {@link GrantedAuthority} instances representing the authorities granted to the group.
     */
    private Flux<@NonNull GrantedAuthority> getGroupAuthorities(@NonNull UUID userCode) {
        QueryFragment fragment = createGroupAuthorityFragment();
        fragment.put("userCode", userCode);
        String cacheKey = CACHE_KEY_GROUP_AUTHORITIES_PREFIX + userCode;
        return this.queryWithCache(cacheKey, fragment.querySql(), fragment, GroupAuthority.class).cast(GrantedAuthority.class);
    }

    /**
     * Logs a successful login attempt by updating the user's login time.
     *
     * @param username The username of the user who successfully logged in.
     * @return A Mono emitting the count of documents updated, which should be 1 if the update was successful.
     */
    private Mono<@NonNull Long> loginSuccess(String username) {
        Query query = Query.query(Criteria.where("username").is(username).ignoreCase(true));
        Update update = Update.update("loginTime", LocalDateTime.now());
        return DatabaseUtils.ENTITY_TEMPLATE.update(query, update, User.class)
                .doOnSuccess(_ -> log.info("User {} logged in successfully, updating login time", username))
                .doOnError(error -> log.warn("Failed to update login time for user {}: {}", username, error.getMessage()));
    }

}