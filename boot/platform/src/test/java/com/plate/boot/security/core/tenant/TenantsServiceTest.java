package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.tenant.member.TenantMembersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ExtendWith(MockitoExtension.class)
class TenantsServiceTest {

    @Mock
    private TenantsRepository tenantsRepository;

    @Mock
    private TenantMembersRepository membersRepository;

    private TenantsService tenantsService;

    private Tenant testTenant;

    private TenantReq testTenantReq;

    @BeforeEach
    void setUp() {
        tenantsService = new TenantsService(tenantsRepository, membersRepository);
        testTenant = new Tenant();
        testTenant.setCode(UUID.randomUUID());
        testTenant.setName("test_tenant");

        testTenantReq = new TenantReq();
        testTenantReq.setCode(testTenant.getCode());
        testTenantReq.setName("test_tenant_req");
    }

    @Nested
    @DisplayName("Tenant search and page tests")
    class TenantSearchTests {

        @Test
        @DisplayName("Should search tenants successfully")
        void shouldSearchTenantsSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(Tenant.class)))
                        .thenReturn(Flux.just(testTenant));

                StepVerifier.create(tenantsService.search(testTenantReq, PageRequest.of(0, 10)))
                        .expectNext(testTenant)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should get page of tenants successfully")
        void shouldGetPageOfTenantsSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(Tenant.class)))
                        .thenReturn(Flux.just(testTenant));
                mockedDatabaseUtils.when(() -> DatabaseUtils.count(any(String.class), any(Map.class)))
                        .thenReturn(Mono.just(1L));

                StepVerifier.create(tenantsService.page(testTenantReq, PageRequest.of(0, 10)))
                        .expectNextMatches(page -> {
                            assertThat(page.getTotalElements()).isEqualTo(1);
                            assertThat(page.getContent()).contains(testTenant);
                            return true;
                        })
                        .verifyComplete();
            }
        }
    }

    @Nested
    @DisplayName("Tenant operation tests")
    class TenantOperationTests {

        @Test
        @DisplayName("Should operate on a tenant successfully")
        void shouldOperateOnTenantSuccessfully() {
            when(tenantsRepository.findByCode(testTenantReq.getCode())).thenReturn(Mono.just(testTenant));
            when(tenantsRepository.save(any(Tenant.class))).thenReturn(Mono.just(testTenant));

            StepVerifier.create(tenantsService.operate(testTenantReq))
                    .expectNextMatches(tenant -> {
                        assertThat(tenant.getName()).isEqualTo(testTenantReq.getName());
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should delete a tenant successfully")
        void shouldDeleteTenantSuccessfully() {
            when(tenantsRepository.delete(any(Tenant.class))).thenReturn(Mono.empty());
            when(membersRepository.deleteByTenantCode(testTenantReq.getCode())).thenReturn(Mono.empty());

            StepVerifier.create(tenantsService.delete(testTenantReq))
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should save a new tenant successfully")
        void shouldSaveNewTenantSuccessfully() {
            Tenant newTenant = new Tenant(); // isNew() will be true
            newTenant.setName("new_tenant");
            when(tenantsRepository.save(newTenant)).thenReturn(Mono.just(newTenant));

            StepVerifier.create(tenantsService.save(newTenant))
                    .expectNext(newTenant)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should update an existing tenant successfully")
        void shouldUpdateExistingTenantSuccessfully() {
            testTenant.setId(1); // isNew() will be false
            when(tenantsRepository.findById(1)).thenReturn(Mono.just(new Tenant()));
            when(tenantsRepository.save(testTenant)).thenReturn(Mono.just(testTenant));

            StepVerifier.create(tenantsService.save(testTenant))
                    .expectNext(testTenant)
                    .verifyComplete();
        }
    }
}