package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.relational.menus.Menu;
import com.plate.boot.relational.menus.MenuEvent;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserAuthoritiesService}.
 * <p>
 * Covers {@code search}, {@code operate}, {@code delete}, {@code save}, and the two
 * {@code @EventListener} methods. The reactive database access is mocked: direct
 * repository calls through the {@code @Mock UserAuthoritiesRepository}, and the
 * {@code search} path through the static {@link DatabaseUtils#ENTITY_TEMPLATE}.
 */
@ExtendWith(MockitoExtension.class)
class UserAuthoritiesServiceTest {

    @Mock
    private UserAuthoritiesRepository repository;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    @InjectMocks
    private UserAuthoritiesService service;

    private R2dbcEntityTemplate savedEntityTemplate;

    @BeforeEach
    void setUp() {
        savedEntityTemplate = DatabaseUtils.ENTITY_TEMPLATE;
        DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
    }

    @AfterEach
    void tearDown() {
        DatabaseUtils.ENTITY_TEMPLATE = savedEntityTemplate;
    }

    @Test
    void searchShouldQueryByCriteria() {
        UserAuthority entity = new UserAuthority();
        entity.setAuthority("ROLE_USER");

        UserAuthorityReq request = new UserAuthorityReq();
        request.setUserCode(UUID.randomUUID());
        request.setAuthority("ROLE_USER");
        request.setTenantCode(UUID.randomUUID());

        when(entityTemplate.select(any(Query.class), eq(UserAuthority.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(service.search(request))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void searchWithEmptyRequestBuildsEmptyCriteria() {
        UserAuthority entity = new UserAuthority();

        when(entityTemplate.select(any(Query.class), eq(UserAuthority.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(service.search(new UserAuthorityReq()))
                .expectNext(entity)
                .verifyComplete();
    }

    @Test
    void operateReturnsExistingWhenFound() {
        UUID userCode = UUID.randomUUID();
        UserAuthorityReq request = new UserAuthorityReq();
        request.setUserCode(userCode);
        request.setAuthority("ROLE_USER");

        UserAuthority existing = new UserAuthority();
        existing.setId(1);

        when(repository.findByUserCodeAndAuthority(userCode, "ROLE_USER")).thenReturn(Mono.just(existing));

        StepVerifier.create(service.operate(request))
                .expectNext(existing)
                .verifyComplete();

        verify(repository, never()).save(any(UserAuthority.class));
    }

    @Test
    void operateCreatesNewAuthorityWhenNotFound() {
        UUID userCode = UUID.randomUUID();
        UserAuthorityReq request = new UserAuthorityReq();
        request.setUserCode(userCode);
        request.setAuthority("ROLE_USER");

        when(repository.findByUserCodeAndAuthority(userCode, "ROLE_USER")).thenReturn(Mono.empty());

        UserAuthority saved = new UserAuthority();
        when(repository.save(any(UserAuthority.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request))
                .expectNext(saved)
                .verifyComplete();

        ArgumentCaptor<UserAuthority> captor = ArgumentCaptor.forClass(UserAuthority.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserCode()).isEqualTo(userCode);
        assertThat(captor.getValue().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void deleteShouldLocateByProvidedIdNotByCode() {
        UserAuthority entity = new UserAuthority();
        entity.setId(42);

        UserAuthorityReq request = new UserAuthorityReq();
        request.setId(42);

        when(repository.findById(42)).thenReturn(Mono.just(entity));
        when(repository.delete(any(UserAuthority.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(repository).findById(42);
        verify(repository, never()).findByCode(any());
        verify(repository).delete(entity);
    }

    @Test
    void deleteDoesNothingWhenIdNotFound() {
        UserAuthorityReq request = new UserAuthorityReq();
        request.setId(99);

        when(repository.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(repository, never()).delete(any(UserAuthority.class));
    }

    @Test
    void saveCreatesWhenNew() {
        UserAuthority entity = new UserAuthority();
        entity.setUserCode(UUID.randomUUID());
        entity.setAuthority("ROLE_USER");

        when(repository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(service.save(entity))
                .expectNext(entity)
                .verifyComplete();

        verify(repository).save(entity);
    }

    @Test
    void saveUpdatesExistingByMergingAuditFields() {
        UserAuthority entity = new UserAuthority();
        entity.setId(42);
        entity.setCode(UUID.randomUUID());
        entity.setUserCode(UUID.randomUUID());
        entity.setAuthority("ROLE_USER");

        UserAuthority old = new UserAuthority();
        UUID oldCode = UUID.randomUUID();
        LocalDateTime oldCreatedAt = LocalDateTime.now();
        UserAuditor oldCreatedBy = UserAuditor.withCode(UUID.randomUUID());
        old.setCode(oldCode);
        old.setCreatedAt(oldCreatedAt);
        old.setCreatedBy(oldCreatedBy);

        when(repository.findById(42)).thenReturn(Mono.just(old));
        when(repository.save(entity)).thenReturn(Mono.just(entity));

        StepVerifier.create(service.save(entity))
                .expectNext(entity)
                .verifyComplete();

        assertThat(entity.getCode()).isEqualTo(oldCode);
        assertThat(entity.getCreatedAt()).isEqualTo(oldCreatedAt);
        assertThat(entity.getCreatedBy()).isEqualTo(oldCreatedBy);
        verify(repository).save(entity);
    }

    @Test
    void saveThrowsWhenExistingAuthorityHasNullId() {
        UserAuthority authority = mock(UserAuthority.class);
        when(authority.isNew()).thenReturn(false);
        when(authority.getId()).thenReturn(null);

        StepVerifier.create(service.save(authority))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(RestServerException.class);
                    assertThat(((RestServerException) err).getReason()).isEqualTo("Id must not be null!");
                    assertThat(err.getCause()).isInstanceOf(IllegalArgumentException.class);
                })
                .verify();

        verify(repository, never()).save(any(UserAuthority.class));
    }

    @Test
    void onUserDeletedEventDeletesByUserCode() {
        User user = new User();
        UUID code = UUID.randomUUID();
        user.setCode(code);
        UserEvent event = UserEvent.delete(user);

        when(repository.deleteByUserCode(code)).thenReturn(Mono.just(3));

        service.onUserDeletedEvent(event);

        verify(repository).deleteByUserCode(code);
    }

    @Test
    void onUserDeletedEventHandlesError() {
        User user = new User();
        UUID code = UUID.randomUUID();
        user.setCode(code);
        UserEvent event = UserEvent.delete(user);

        when(repository.deleteByUserCode(code)).thenReturn(Mono.error(new RuntimeException("boom")));

        service.onUserDeletedEvent(event);

        verify(repository).deleteByUserCode(code);
    }

    @Test
    void onMenuDeletedEventDeletesByAuthority() {
        Menu menu = new Menu();
        menu.setCode(UUID.randomUUID());
        menu.setAuthority("ROLE_USERS");

        MenuEvent event = MenuEvent.delete(menu);

        when(repository.deleteByAuthorityIn(Set.of("ROLE_USERS"))).thenReturn(Mono.just(2));

        service.onMenuDeletedEvent(event);

        verify(repository).deleteByAuthorityIn(Set.of("ROLE_USERS"));
    }
}
