package com.plate.boot.security.core.group.authority;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.relational.menus.Menu;
import com.plate.boot.relational.menus.MenuEvent;
import com.plate.boot.security.core.group.Group;
import com.plate.boot.security.core.group.GroupEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupAuthoritiesService}, covering operate/save/delete plus the two
 * {@code @EventListener} handlers, without starting a Spring container or connecting to a database.
 */
@ExtendWith(MockitoExtension.class)
class GroupAuthoritiesServiceTest {

    @Mock
    private GroupAuthoritiesRepository authoritiesRepository;

    @InjectMocks
    private GroupAuthoritiesService service;

    private CacheManager savedCacheManager;

    @BeforeEach
    void setUp() {
        savedCacheManager = ContextUtils.CACHE_MANAGER;
        ContextUtils.CACHE_MANAGER = null;
        service.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        ContextUtils.APPLICATION_EVENT_PUBLISHER = null;
        ContextUtils.CACHE_MANAGER = savedCacheManager;
    }

    @Test
    void operateReturnsExistingAuthorityWhenFound() {
        UUID groupCode = UUID.randomUUID();
        GroupAuthorityReq request = new GroupAuthorityReq();
        request.setGroupCode(groupCode);
        request.setAuthority("ROLE_ADMIN");

        GroupAuthority existing = new GroupAuthority();
        existing.setId(1);
        existing.setGroupCode(groupCode);
        existing.setAuthority("ROLE_ADMIN");

        when(authoritiesRepository.findByGroupCodeAndAuthority(groupCode, "ROLE_ADMIN"))
                .thenReturn(Mono.just(existing));

        StepVerifier.create(service.operate(request))
                .assertNext(authority -> assertThat(authority).isSameAs(existing))
                .verifyComplete();

        verify(authoritiesRepository).findByGroupCodeAndAuthority(groupCode, "ROLE_ADMIN");
        verify(authoritiesRepository, never()).save(any());
    }

    @Test
    void operateSavesNewAuthorityWhenNotFound() {
        UUID groupCode = UUID.randomUUID();
        GroupAuthorityReq request = new GroupAuthorityReq();
        request.setGroupCode(groupCode);
        request.setAuthority("ROLE_USER");

        when(authoritiesRepository.findByGroupCodeAndAuthority(groupCode, "ROLE_USER"))
                .thenReturn(Mono.empty());
        when(authoritiesRepository.save(any(GroupAuthority.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.operate(request))
                .assertNext(authority -> {
                    assertThat(authority.getGroupCode()).isEqualTo(groupCode);
                    assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
                })
                .verifyComplete();

        verify(authoritiesRepository).save(any(GroupAuthority.class));
    }

    @Test
    void saveNewAuthorityInserts() {
        GroupAuthority authority = new GroupAuthority();
        authority.setGroupCode(UUID.randomUUID());
        authority.setAuthority("ROLE_USER");

        when(authoritiesRepository.save(any(GroupAuthority.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.save(authority))
                .assertNext(saved -> assertThat(saved).isSameAs(authority))
                .verifyComplete();

        verify(authoritiesRepository).save(authority);
    }

    @Test
    void saveExistingAuthorityUpdates() {
        GroupAuthority authority = new GroupAuthority();
        authority.setId(2);
        authority.setGroupCode(UUID.randomUUID());
        authority.setAuthority("ROLE_USER");

        when(authoritiesRepository.findById(2)).thenReturn(Mono.just(authority));
        when(authoritiesRepository.save(any(GroupAuthority.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.save(authority))
                .assertNext(saved -> assertThat(saved).isSameAs(authority))
                .verifyComplete();

        verify(authoritiesRepository).findById(2);
        verify(authoritiesRepository).save(authority);
    }

    @Test
    void saveExistingAuthorityCompletesEmptyWhenMissing() {
        GroupAuthority authority = new GroupAuthority();
        authority.setId(99);

        when(authoritiesRepository.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(service.save(authority)).verifyComplete();

        verify(authoritiesRepository).findById(99);
        verify(authoritiesRepository, never()).save(any());
    }

    @Test
    void deleteDeletesAuthority() {
        GroupAuthorityReq request = new GroupAuthorityReq();
        request.setId(1);
        request.setGroupCode(UUID.randomUUID());
        request.setAuthority("ROLE_USER");

        when(authoritiesRepository.delete(any(GroupAuthority.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(authoritiesRepository).delete(any(GroupAuthority.class));
    }

    @Test
    void onGroupDeletedEventRemovesByGroupCode() {
        UUID groupCode = UUID.randomUUID();
        Group group = new Group();
        group.setCode(groupCode);

        when(authoritiesRepository.deleteByGroupCode(groupCode)).thenReturn(Mono.just(4));

        service.onUserDeletedEvent(GroupEvent.delete(group));

        verify(authoritiesRepository).deleteByGroupCode(groupCode);
    }

    @Test
    void onMenuDeletedEventRemovesByAuthority() {
        UUID code = UUID.randomUUID();
        Menu menu = new Menu();
        menu.setCode(code);
        menu.setAuthority("ROLE_ADMIN");

        when(authoritiesRepository.deleteByAuthorityIn(any())).thenReturn(Mono.just(1));

        service.onMenuDeletedEvent(MenuEvent.delete(menu));

        verify(authoritiesRepository).deleteByAuthorityIn(Set.of("ROLE_ADMIN"));
    }

    @Test
    void searchReturnsAuthorities() {
        GroupAuthority authority = new GroupAuthority();
        authority.setId(1);
        authority.setGroupCode(UUID.randomUUID());
        authority.setAuthority("ROLE_USER");

        GroupAuthorityReq request = new GroupAuthorityReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(any(Query.class), eq(GroupAuthority.class)))
                    .thenReturn(Flux.just(authority));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(authority)
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }
}
