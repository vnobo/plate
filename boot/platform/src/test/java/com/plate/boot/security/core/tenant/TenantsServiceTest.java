package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TenantsService}.
 * <p>
 * Pure Mockito + StepVerifier tests; no Spring container or database is started. The
 * {@code search}/{@code page} methods delegate to the static {@link DatabaseUtils} helpers, which are
 * mocked with {@link MockedStatic} so the query-building path can be exercised without a live R2DBC
 * client.
 */
@ExtendWith(MockitoExtension.class)
class TenantsServiceTest {

    @Mock
    private TenantsRepository tenantsRepository;

    @InjectMocks
    private TenantsService service;

    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
    }

    @AfterEach
    void tearDown() {
        ContextUtils.APPLICATION_EVENT_PUBLISHER = null;
    }

    @Test
    void operateCreatesNewTenant() {
        UUID code = UUID.randomUUID();
        TenantReq request = new TenantReq();
        request.setCode(code);
        request.setName("Tenant A");
        request.setPcode(UUID.randomUUID());

        when(tenantsRepository.findByCode(code)).thenReturn(Mono.empty());

        Tenant saved = new Tenant();
        saved.setId(1);
        saved.setName("Tenant A");
        when(tenantsRepository.save(any(Tenant.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request))
                .assertNext(tenant -> assertThat(tenant.getName()).isEqualTo("Tenant A"))
                .verifyComplete();

        verify(tenantsRepository).save(any(Tenant.class));
    }

    @Test
    void operateUpdatesExistingTenant() {
        UUID code = UUID.randomUUID();
        TenantReq request = new TenantReq();
        request.setId(7);
        request.setCode(code);
        request.setName("Updated");

        Tenant existing = new Tenant();
        existing.setId(7);
        existing.setCode(code);
        existing.setName("Old");
        when(tenantsRepository.findByCode(code)).thenReturn(Mono.just(existing));

        Tenant old = new Tenant();
        old.setId(7);
        old.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));
        when(tenantsRepository.findById(7)).thenReturn(Mono.just(old));

        Tenant saved = new Tenant();
        saved.setId(7);
        saved.setName("Updated");
        when(tenantsRepository.save(any(Tenant.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request))
                .assertNext(tenant -> assertThat(tenant.getName()).isEqualTo("Updated"))
                .verifyComplete();

        verify(tenantsRepository).findById(7);
        verify(tenantsRepository).save(any(Tenant.class));
    }

    @Test
    void saveInsertsNewTenant() {
        Tenant tenant = new Tenant();
        tenant.setName("New");

        Tenant saved = new Tenant();
        saved.setId(1);
        saved.setName("New");
        when(tenantsRepository.save(any(Tenant.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.save(tenant))
                .assertNext(t -> assertThat(t.getId()).isEqualTo(1))
                .verifyComplete();

        verify(tenantsRepository).save(any(Tenant.class));
    }

    @Test
    void saveUpdatesExistingTenantPreservingCreatedAt() {
        Tenant tenant = new Tenant();
        tenant.setId(3);
        tenant.setName("Updated");

        Tenant old = new Tenant();
        old.setId(3);
        old.setCreatedAt(LocalDateTime.of(2021, 5, 5, 10, 0));
        when(tenantsRepository.findById(3)).thenReturn(Mono.just(old));

        Tenant saved = new Tenant();
        saved.setId(3);
        when(tenantsRepository.save(any(Tenant.class))).thenReturn(Mono.just(saved));

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);

        StepVerifier.create(service.save(tenant)).expectNextCount(1).verifyComplete();

        verify(tenantsRepository).findById(3);
        verify(tenantsRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(old.getCreatedAt());
    }

    @Test
    void deleteDeletesTenantAndPublishesEvent() {
        UUID code = UUID.randomUUID();
        TenantReq request = new TenantReq();
        request.setCode(code);

        Tenant tenant = new Tenant();
        tenant.setCode(code);
        when(tenantsRepository.findByCode(code)).thenReturn(Mono.just(tenant));
        when(tenantsRepository.delete(tenant)).thenReturn(Mono.empty());

        StepVerifier.withVirtualTime(() -> service.delete(request))
                .thenAwait(Duration.ofSeconds(2))
                .verifyComplete();

        verify(tenantsRepository).delete(tenant);
        verify(publisher).publishEvent(any(TenantEvent.class));
    }

    @Test
    void deleteCompletesEmptyWhenTenantNotFound() {
        UUID code = UUID.randomUUID();
        TenantReq request = new TenantReq();
        request.setCode(code);

        when(tenantsRepository.findByCode(code)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(tenantsRepository, never()).delete(any(Tenant.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void searchReturnsMappedTenants() {
        TenantReq request = new TenantReq();
        Pageable pageable = PageRequest.of(0, 20);
        Tenant tenant = new Tenant();
        tenant.setName("T1");

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), any(), eq(Tenant.class)))
                    .thenReturn(Flux.just(tenant));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(tenant)
                    .verifyComplete();
        }
    }

    @Test
    void pageZipsSearchAndCount() {
        TenantReq request = new TenantReq();
        Pageable pageable = PageRequest.of(0, 20);
        Tenant tenant = new Tenant();
        tenant.setName("T1");

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), any(), eq(Tenant.class)))
                    .thenReturn(Flux.just(tenant));
            db.when(() -> DatabaseUtils.count(anyString(), any()))
                    .thenReturn(Mono.just(100L));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> {
                        assertThat(page.getTotalElements()).isEqualTo(100L);
                        assertThat(page.getContent()).containsExactly(tenant);
                    })
                    .verifyComplete();
        }
    }
}
