package com.plate.boot.security.core.tenant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TenantsController}.
 */
@ExtendWith(MockitoExtension.class)
class TenantsControllerTest {

    @Mock
    private TenantsService tenantsService;

    @InjectMocks
    private TenantsController controller;

    private static Tenant tenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        return tenant;
    }

    @Test
    void searchDelegatesToService() {
        TenantReq request = new TenantReq();
        Pageable pageable = PageRequest.of(0, 20);
        Tenant tenant = tenant("acme");
        when(tenantsService.search(request, pageable)).thenReturn(Flux.just(tenant));

        StepVerifier.create(controller.search(request, pageable))
                .expectNext(tenant)
                .verifyComplete();

        verify(tenantsService).search(request, pageable);
    }

    @Test
    void pageWrapsServicePageInPagedModel() {
        TenantReq request = new TenantReq();
        Pageable pageable = PageRequest.of(0, 20);
        Tenant tenant = tenant("acme");
        Page<Tenant> page = new PageImpl<>(List.of(tenant), pageable, 1);
        when(tenantsService.page(request, pageable)).thenReturn(Mono.just(page));

        StepVerifier.create(controller.page(request, pageable))
                .assertNext(paged -> {
                    assertThat(paged).isInstanceOf(PagedModel.class);
                    assertThat(paged.getContent()).containsExactly(tenant);
                })
                .verifyComplete();

        verify(tenantsService).page(request, pageable);
    }

    @Test
    void operateDelegatesToService() {
        TenantReq request = new TenantReq();
        request.setName("acme");

        Tenant saved = tenant("acme");
        when(tenantsService.operate(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.operate(request))
                .expectNext(saved)
                .verifyComplete();

        verify(tenantsService).operate(request);
    }

    @Test
    void deleteRejectsNullId() {
        TenantReq request = new TenantReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be null");

        verify(tenantsService, never()).delete(any(TenantReq.class));
    }

    @Test
    void deleteDelegatesWhenIdPresent() {
        TenantReq request = new TenantReq();
        request.setId(1);

        when(tenantsService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(tenantsService).delete(request);
    }
}
