package com.plate.boot.security.core.tenant;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TenantsServiceTest {

    @Autowired
    private TenantsService tenantsService;

    @Autowired
    private TenantsRepository tenantsRepository;

    private Tenant createTenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setDescription("Test Tenant Description");
        tenant.setCode(name.toLowerCase());
        tenant.setPcode("root");
        return tenant;
    }

    @BeforeEach
    void setUp() {
        tenantsRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    void testSaveNewTenant() {
        Tenant tenant = createTenant("TestTenant");
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
        Tenant saved = tenantsService.save(tenant).block();
        Assertions.assertNotNull(saved);

        saved.setDescription("Updated Description");
        StepVerifier.create(tenantsService.save(saved))
                .assertNext(updatedTenant -> {
                    Assertions.assertEquals("Updated Description", updatedTenant.getDescription());
                    Assertions.assertEquals(saved.getId(), updatedTenant.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testAddTenant() {
        TenantReq req = new TenantReq();
        req.setName("NewTenant");
        req.setDescription("New Tenant Description");
        req.setCode("newtenant");
        req.setPcode("root");
        StepVerifier.create(tenantsService.add(req))
                .assertNext(addedTenant -> {
                    Assertions.assertNotNull(addedTenant.getId());
                    Assertions.assertEquals("NewTenant", addedTenant.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testAddTenant_AlreadyExists() {
        tenantsService.save(createTenant("ExistingTenant")).block();
        TenantReq req = new TenantReq();
        req.setName("ExistingTenant");
        req.setCode("existingtenant");
        req.setPcode("root");
        StepVerifier.create(tenantsService.add(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(5)
    void testModifyTenant() {
        Tenant saved = tenantsService.save(createTenant("ModifyTenant")).block();
        Assertions.assertNotNull(saved);

        TenantReq req = new TenantReq();
        req.setCode(saved.getCode());
        req.setName("ModifyTenant");
        req.setDescription("Modified Description");

        StepVerifier.create(tenantsService.modify(req))
                .assertNext(modifiedTenant -> {
                    Assertions.assertEquals("Modified Description", modifiedTenant.getDescription());
                    Assertions.assertEquals(saved.getId(), modifiedTenant.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testModifyTenant_NotFound() {
        TenantReq req = new TenantReq();
        req.setCode("nonexistent");
        req.setName("NonExistentTenant");
        StepVerifier.create(tenantsService.modify(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(7)
    void testDelete() {
        Tenant saved = tenantsService.save(createTenant("DeleteTenant")).block();
        Assertions.assertNotNull(saved);

        TenantReq req = new TenantReq();
        req.setCode(saved.getCode());

        StepVerifier.create(tenantsService.delete(req))
                .verifyComplete();

        StepVerifier.create(tenantsRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(8)
    void testPage() {
        tenantsService.save(createTenant("Tenant1")).block();
        tenantsService.save(createTenant("Tenant2")).block();
        TenantReq req = new TenantReq();
        StepVerifier.create(tenantsService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(9)
    void testPage_Empty() {
        TenantReq req = new TenantReq();
        StepVerifier.create(tenantsService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(0, page.getTotalElements());
                    Assertions.assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }
}