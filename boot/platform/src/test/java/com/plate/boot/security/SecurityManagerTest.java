package com.plate.boot.security;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import com.plate.boot.security.core.user.UsersService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityManager} (no Spring / R2DBC connection required).
 * <p>
 * The static {@link DatabaseUtils} collaborators ({@code ENTITY_TEMPLATE},
 * {@code DATABASE_CLIENT}, {@code MAX_IN_MEMORY_SIZE}) and {@link ContextUtils#CACHE_MANAGER}
 * are mocked per test and restored afterwards.
 */
class SecurityManagerTest {

    private UsersService usersService;
    private SecurityManager manager;

    private R2dbcEntityTemplate entityTemplate;
    private DatabaseClient databaseClient;
    private DatabaseClient.GenericExecuteSpec executeSpec;
    private RowsFetchSpec<Object> rowsFetchSpec;

    private R2dbcEntityTemplate savedTemplate;
    private DatabaseClient savedClient;
    private DataSize savedMaxSize;
    private CacheManager savedCacheManager;
    private JsonMapper savedObjectMapper;

    @BeforeEach
    void setUp() {
        savedTemplate = DatabaseUtils.ENTITY_TEMPLATE;
        savedClient = DatabaseUtils.DATABASE_CLIENT;
        savedMaxSize = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        savedCacheManager = ContextUtils.CACHE_MANAGER;
        savedObjectMapper = ContextUtils.OBJECT_MAPPER;

        ContextUtils.CACHE_MANAGER = null;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();

        usersService = mock(UsersService.class);
        manager = new SecurityManager(usersService);
        manager.afterPropertiesSet();

        entityTemplate = mock(R2dbcEntityTemplate.class);
        databaseClient = mock(DatabaseClient.class);
        executeSpec = mock(DatabaseClient.GenericExecuteSpec.class);
        rowsFetchSpec = mock(RowsFetchSpec.class);

        DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
        DatabaseUtils.DATABASE_CLIENT = databaseClient;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(10);

        doReturn(executeSpec).when(databaseClient).sql(any(Supplier.class));
        doReturn(executeSpec).when(executeSpec).bindValues(any(Map.class));
        doReturn(rowsFetchSpec).when(executeSpec).map(any(BiFunction.class));
    }

    @AfterEach
    void tearDown() {
        DatabaseUtils.ENTITY_TEMPLATE = savedTemplate;
        DatabaseUtils.DATABASE_CLIENT = savedClient;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMaxSize;
        ContextUtils.CACHE_MANAGER = savedCacheManager;
        ContextUtils.OBJECT_MAPPER = savedObjectMapper;
    }

    private static User user(String username) {
        User user = new User();
        user.setCode(UUID.randomUUID());
        user.setUsername(username);
        user.setPassword("{bcrypt}secret");
        user.setDisabled(false);
        user.setAccountExpired(false);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
        return user;
    }

    @Test
    void registerOrModifyUserAddsWhenCodeAbsent() {
        UserReq request = new UserReq();
        User saved = user("admin");
        when(usersService.add(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(manager.registerOrModifyUser(request))
                .expectNext(saved)
                .verifyComplete();

        verify(usersService).add(request);
        verify(usersService, never()).operate(any(UserReq.class));
    }

    @Test
    void registerOrModifyUserOperatesWhenCodePresent() {
        UserReq request = new UserReq();
        request.setCode(UUID.randomUUID());
        User saved = user("admin");
        when(usersService.operate(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(manager.registerOrModifyUser(request))
                .expectNext(saved)
                .verifyComplete();

        verify(usersService).operate(request);
        verify(usersService, never()).add(any(UserReq.class));
    }

    @Test
    void updatePasswordDelegatesToTemplateAndReturnsUserDetails() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("admin");
        doReturn(Mono.just(1L)).when(entityTemplate)
                .update(any(Query.class), any(Update.class), any(Class.class));

        StepVerifier.create(manager.updatePassword(userDetails, "newPassword"))
                .expectNext(userDetails)
                .verifyComplete();
    }

    @Test
    void loadByOauth2RejectsInvalidParameters() {
        StepVerifier.create(manager.loadByOauth2(null, "openid"))
                .expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(manager.loadByOauth2("bad type!", "openid"))
                .expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(manager.loadByOauth2("github", null))
                .expectError(IllegalArgumentException.class).verify();
    }

    @Test
    void loadByOauth2ReturnsUser() {
        User saved = user("admin");
        doReturn(Flux.just(saved)).when(rowsFetchSpec).all();

        StepVerifier.create(manager.loadByOauth2("github", "openid-123"))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void loadByUsernameReturnsUserWhenFound() {
        User saved = user("admin");
        doReturn(Flux.just(saved)).when(entityTemplate).select(any(Query.class), any(Class.class));

        StepVerifier.create(manager.loadByUsername("admin"))
                .expectNext(saved)
                .verifyComplete();
    }

    @Test
    void loadByUsernameErrorsWhenNotFound() {
        doReturn(Flux.empty()).when(entityTemplate).select(any(Query.class), any(Class.class));

        StepVerifier.create(manager.loadByUsername("ghost"))
                .expectError(UsernameNotFoundException.class)
                .verify();
    }

    @Test
    void findByUsernameReturnsSecurityDetails() {
        User saved = user("admin");
        doReturn(Flux.just(saved)).when(entityTemplate).select(any(Query.class), any(Class.class));
        doReturn(Flux.empty()).when(rowsFetchSpec).all();
        doReturn(Mono.just(1L)).when(entityTemplate)
                .update(any(Query.class), any(Update.class), any(Class.class));

        StepVerifier.create(manager.findByUsername("admin"))
                .assertNext(details -> {
                    assertThat(details).isInstanceOf(SecurityDetails.class);
                    assertThat(details.getUsername()).isEqualTo("admin");
                    assertThat(((SecurityDetails) details).getCode()).isEqualTo(saved.getCode());
                })
                .verifyComplete();
    }

    @Test
    void findByUsernameWrapsNotFoundAsBadCredentials() {
        doReturn(Flux.empty()).when(entityTemplate).select(any(Query.class), any(Class.class));

        StepVerifier.create(manager.findByUsername("ghost"))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(BadCredentialsException.class)
                            .hasCauseInstanceOf(UsernameNotFoundException.class);
                })
                .verify();
    }

    @Test
    void findByUsernameRejectsUserWithEmptyCode() {
        User saved = new User();
        saved.setUsername("admin");
        doReturn(Flux.just(saved)).when(entityTemplate).select(any(Query.class), any(Class.class));
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(manager.findByUsername("admin"))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void findByUsernamePropagatesLoginTimeUpdateFailure() {
        User saved = user("admin");
        doReturn(Flux.just(saved)).when(entityTemplate).select(any(Query.class), any(Class.class));
        doReturn(Flux.empty()).when(rowsFetchSpec).all();
        doReturn(Mono.error(new RuntimeException("db down"))).when(entityTemplate)
                .update(any(Query.class), any(Update.class), any(Class.class));

        StepVerifier.create(manager.findByUsername("admin"))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(RuntimeException.class)
                        .hasMessage("db down"))
                .verify();
    }
}
