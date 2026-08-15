package com.plate.boot.security.core.tenant.member;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.tenant.Tenant;
import com.plate.boot.security.core.tenant.TenantEvent;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserEvent;
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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveUpdateOperation;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TenantMembersService}.
 * <p>
 * Pure Mockito + StepVerifier tests; no Spring container or database is started. The
 * {@code operate} path relies on the static {@link DatabaseUtils#ENTITY_TEMPLATE}, which is replaced
 * with a mocked {@link R2dbcEntityTemplate} (including its fluent update API) for the duration of each
 * test. The {@code search}/{@code page} methods depend on the SQL-bound
 * {@link DatabaseUtils} helpers and are intentionally not covered here (see report).
 */
@ExtendWith(MockitoExtension.class)
class TenantMembersServiceTest {

    @Mock
    private TenantMembersRepository tenantMembersRepository;

    @InjectMocks
    private TenantMembersService service;

    private R2dbcEntityTemplate savedTemplate;

    private CacheManager savedCacheManager;

    @BeforeEach
    void setUp() {
        savedCacheManager = ContextUtils.CACHE_MANAGER;
        ContextUtils.CACHE_MANAGER = null;
        service.afterPropertiesSet();
        savedTemplate = DatabaseUtils.ENTITY_TEMPLATE;
    }

    @AfterEach
    void tearDown() {
        DatabaseUtils.ENTITY_TEMPLATE = savedTemplate;
        ContextUtils.CACHE_MANAGER = savedCacheManager;
    }

    @Test
    void operateUpdatesExistingMemberAndEnablesIt() {
        R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
        DatabaseUtils.ENTITY_TEMPLATE = template;

        ReactiveUpdateOperation.ReactiveUpdate reactiveUpdate = mock(ReactiveUpdateOperation.ReactiveUpdate.class);
        ReactiveUpdateOperation.TerminatingUpdate terminatingUpdate =
                mock(ReactiveUpdateOperation.TerminatingUpdate.class);
        when(template.update(TenantMember.class)).thenReturn(reactiveUpdate);
        when(reactiveUpdate.matching(any(Query.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.apply(any(Update.class))).thenReturn(Mono.just(1L));

        UUID userCode = UUID.randomUUID();
        TenantMemberReq request = new TenantMemberReq();
        request.setUserCode(userCode);
        request.setTenantCode(UUID.randomUUID());

        TenantMember existing = new TenantMember();
        existing.setUserCode(userCode);
        existing.setEnabled(false);
        when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.just(existing));

        TenantMember saved = new TenantMember();
        saved.setUserCode(userCode);
        saved.setEnabled(true);
        when(tenantMembersRepository.save(any(TenantMember.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request))
                .assertNext(member -> assertThat(member.getEnabled()).isTrue())
                .verifyComplete();

        verify(tenantMembersRepository).save(any(TenantMember.class));
    }

    @Test
    void operateCreatesNewMemberWhenAbsent() {
        R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
        DatabaseUtils.ENTITY_TEMPLATE = template;

        ReactiveUpdateOperation.ReactiveUpdate reactiveUpdate = mock(ReactiveUpdateOperation.ReactiveUpdate.class);
        ReactiveUpdateOperation.TerminatingUpdate terminatingUpdate =
                mock(ReactiveUpdateOperation.TerminatingUpdate.class);
        when(template.update(TenantMember.class)).thenReturn(reactiveUpdate);
        when(reactiveUpdate.matching(any(Query.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.apply(any(Update.class))).thenReturn(Mono.just(1L));

        UUID userCode = UUID.randomUUID();
        TenantMemberReq request = new TenantMemberReq();
        request.setUserCode(userCode);
        request.setTenantCode(UUID.randomUUID());

        when(template.selectOne(any(Query.class), eq(TenantMember.class))).thenReturn(Mono.empty());

        TenantMember saved = new TenantMember();
        saved.setUserCode(userCode);
        saved.setEnabled(true);
        when(tenantMembersRepository.save(any(TenantMember.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request))
                .assertNext(member -> assertThat(member.getEnabled()).isTrue())
                .verifyComplete();

        verify(tenantMembersRepository).save(any(TenantMember.class));
    }

    @Test
    void deleteDeletesMember() {
        TenantMemberReq request = new TenantMemberReq();
        request.setUserCode(UUID.randomUUID());
        request.setTenantCode(UUID.randomUUID());

        when(tenantMembersRepository.delete(any(TenantMember.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(tenantMembersRepository).delete(any(TenantMember.class));
    }

    @Test
    void onTenantDeletedEventDeletesMembersByTenantCode() {
        UUID code = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setCode(code);
        TenantEvent event = TenantEvent.delete(tenant);

        when(tenantMembersRepository.deleteByTenantCode(code)).thenReturn(Mono.just(3));

        service.onUserDeletedEvent(event);

        verify(tenantMembersRepository).deleteByTenantCode(code);
    }

    @Test
    void onTenantDeletedEventHandlesErrorGracefully() {
        UUID code = UUID.randomUUID();
        Tenant tenant = new Tenant();
        tenant.setCode(code);
        TenantEvent event = TenantEvent.delete(tenant);

        when(tenantMembersRepository.deleteByTenantCode(code))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        service.onUserDeletedEvent(event);

        verify(tenantMembersRepository).deleteByTenantCode(code);
    }

    @Test
    void onUserDeletedEventDeletesMembersByUserCode() {
        UUID code = UUID.randomUUID();
        User user = new User();
        user.setCode(code);
        UserEvent event = UserEvent.delete(user);

        when(tenantMembersRepository.deleteByUserCode(code)).thenReturn(Mono.just(2));

        service.onUserDeletedEvent(event);

        verify(tenantMembersRepository).deleteByUserCode(code);
    }

    @Test
    void searchReturnsMembers() {
        TenantMemberRes res = new TenantMemberRes();
        res.setName("tenant-a");

        TenantMemberReq request = new TenantMemberReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(TenantMemberRes.class)))
                    .thenReturn(Flux.just(res));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(res)
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void pageReturnsPagedMembers() {
        TenantMemberRes res = new TenantMemberRes();
        res.setName("tenant-a");

        TenantMemberReq request = new TenantMemberReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(TenantMemberRes.class)))
                    .thenReturn(Flux.just(res));
            db.when(() -> DatabaseUtils.count(anyString(), anyMap())).thenReturn(Mono.just(7L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> {
                        assertThat(page.getContent()).containsExactly(res);
                        assertThat(page.getSize()).isEqualTo(pageable.getPageSize());
                        assertThat(page.getNumber()).isEqualTo(pageable.getPageNumber());
                        assertThat(page.getTotalElements()).isEqualTo(1L);
                    })
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }
}
