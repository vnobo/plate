package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.query.QueryHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TenantsServiceTest {

    private TenantsService tenantsService;
    private TenantsRepository tenantsRepository;
    private PasswordEncoder passwordEncoder;
    private QueryHelper queryHelper;

    @BeforeEach
    void setUp() {
        tenantsRepository = Mockito.mock(TenantsRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        queryHelper = Mockito.mock(QueryHelper.class);

        // Use reflection to set the queryHelper field since there's no setter method
        try {
            var field = TenantsService.class.getDeclaredField("queryHelper");
            field.setAccessible(true);
            tenantsService = new TenantsService(tenantsRepository, null, passwordEncoder);
            field.set(tenantsService, queryHelper);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set queryHelper field via reflection", e);
        }
        
        tenantsRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    void testSaveNewTenant() {
        Tenant tenant = createTenant("TestTenant");
        Mockito.when(tenantsRepository.save(tenant)).thenReturn(Mono.just(tenant));
        StepVerifier.create(tenantsService.save(tenant))
                .assertNext(savedTenant -> {
                    Assertions.assertNotNull(savedTenant.getId());
                    Assertions.assertEquals("TestTenant", savedTenant.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void testSaveExistingTenant() {
        Tenant tenant = createTenant("TestTenant");
        tenant.setId("1");
        Mockito.when(tenantsRepository.findById("1")).thenReturn(Mono.just(tenant));
        Mockito.when(tenantsRepository.save(tenant)).thenReturn(Mono.just(tenant));
        StepVerifier.create(tenantsService.save(tenant))
                .assertNext(updatedTenant -> {
                    Assertions.assertEquals(tenant.getId(), updatedTenant.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testAddTenant() {
        TenantReq req = new TenantReq();
        req.setCode("new-tenant");
        req.setPcode("0");
        req.setName("New Tenant");
        Mockito.when(tenantsRepository.findByCode("new-tenant")).thenReturn(Mono.empty());
        Mockito.when(tenantsRepository.save(Mockito.any(Tenant.class))).thenReturn(Mono.just(req.toTenant()));
        StepVerifier.create(tenantsService.operate(req))
                .assertNext(addedTenant -> {
                    Assertions.assertNotNull(addedTenant.getId());
                    Assertions.assertEquals("New Tenant", addedTenant.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testAddTenant_AlreadyExists() {
        Tenant existing = createTenant("ExistingTenant");
        existing.setId("1");
        Mockito.when(tenantsRepository.findByCode("existingtenant")).thenReturn(Mono.just(existing));
        Mockito.when(tenantsRepository.save(existing)).thenReturn(Mono.just(existing));
        TenantReq req = new TenantReq();
        req.setName("ExistingTenant");
        req.setCode("existingtenant");
        req.setPcode("root");
        StepVerifier.create(tenantsService.operate(req))
                .assertNext(updatedTenant -> {
                    Assertions.assertEquals(existing.getId(), updatedTenant.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(5)
    void testModifyTenant() {
        Tenant saved = createTenant("ModifyTenant");
        saved.setId("1");
        Mockito.when(tenantsRepository.findById("1")).thenReturn(Mono.just(saved));
        Mockito.when(tenantsRepository.save(saved)).thenReturn(Mono.just(saved));
        TenantReq req = new TenantReq();
        req.setId("1");
        req.setCode("modify-tenant");
        req.setName("Modified Tenant");
        StepVerifier.create(tenantsService.operate(req))
                .assertNext(updatedTenant -> {
                    Assertions.assertEquals("Modified Tenant", updatedTenant.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testModifyTenant_NotFound() {
        TenantReq req = new TenantReq();
        req.setId("999");
        Mockito.when(tenantsRepository.findById("999")).thenReturn(Mono.empty());
        Mockito.when(tenantsRepository.save(Mockito.any(Tenant.class))).thenReturn(Mono.just(req.toTenant()));
        StepVerifier.create(tenantsService.operate(req))
                .assertNext(newTenant -> {
                    Assertions.assertEquals("New Tenant", newTenant.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(7)
    void testFindById() {
        Tenant tenant = createTenant("FindByIdTenant");
        tenant.setId("1");
        Mockito.when(tenantsRepository.findById("1")).thenReturn(Mono.just(tenant));
        StepVerifier.create(tenantsService.findById(tenant.getId()))
                .assertNext(found -> {
                    Assertions.assertEquals("FindByIdTenant", found.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(8)
    void testFindById_NotFound() {
        Mockito.when(tenantsRepository.findById("999")).thenReturn(Mono.empty());
        StepVerifier.create(tenantsService.findById("999"))
                .verifyComplete();
    }

    @Test
    @Order(9)
    void testSearch() {
        Tenant tenant1 = createTenant("SearchTenant1");
        tenant1.setId("1");
        Tenant tenant2 = createTenant("SearchTenant2");
        tenant2.setId("2");
        Mockito.when(tenantsRepository.findAll()).thenReturn(Flux.just(tenant1, tenant2));
        StepVerifier.create(tenantsService.search(new TenantReq(), PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(10)
    void testDelete() {
        TenantReq req = new TenantReq();
        req.setCode("delete-tenant");
        req.setId("1");
        Tenant tenant = createTenant("DeleteTenant");
        tenant.setId("1");

        Mockito.when(tenantsRepository.findById("1")).thenReturn(Mono.just(tenant));
        Mockito.when(tenantsRepository.delete(tenant)).thenReturn(Mono.empty());

        StepVerifier.create(tenantsService.delete(req)).verifyComplete();
    }

    @Test
    @Order(11)
    void testSaveWithNullFields() {
        Tenant tenant = new Tenant();
        tenant.setCode("null-fields");
        Mockito.when(tenantsRepository.save(tenant)).thenReturn(Mono.just(tenant));
        StepVerifier.create(tenantsService.save(tenant))
                .assertNext(saved -> {
                    Assertions.assertNotNull(saved.getCode());
                })
                .verifyComplete();
    }

    @Test
    @Order(12)
    void testSearchWithFilters() {
        TenantReq req = new TenantReq();
        req.setName("filter");
        Mockito.when(tenantsRepository.findAll()).thenReturn(Flux.empty());
        StepVerifier.create(tenantsService.search(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    @Order(13)
    void testPage() {
        TenantReq req = new TenantReq();
        req.setName("PageTenant");

        // Mock QueryFragment for search
        QueryFragment searchFragment = QueryFragment.withNew()
                .columns("*")
                .from("tenants")
                .where("name LIKE :name")
                .orderBy("id ASC")
                .limit(10, 0);

        // Mock QueryFragment for count
        QueryFragment countFragment = QueryFragment.withNew()
                .columns("*")
                .from("tenants")
                .where("name LIKE :name");

        // Mock the queryHelper behavior
        Mockito.when(QueryHelper.query(req, PageRequest.of(0, 10))).thenReturn(searchFragment);
        Mockito.when(queryHelper.query(req)).thenReturn(countFragment);

        // Mock the repository behavior
        Mockito.when(tenantsRepository.findAll()).thenReturn(Flux.just(createTenant("PageTenant1"), createTenant("PageTenant2")));
        
        StepVerifier.create(tenantsService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }


    private Tenant createTenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setCode("test-tenant-" + name.toLowerCase().replace(" ", "-"));
        tenant.setPcode("0");
        tenant.setName(name);
        return tenant;
    }
}