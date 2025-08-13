package com.plate.boot.security.core.group;

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
class GroupsServiceTest {

    @Autowired
    private GroupsService groupsService;

    @Autowired
    private GroupsRepository groupsRepository;

    private Group createGroup(String name) {
        Group group = new Group();
        group.setName(name);
        return group;
    }

    @BeforeEach
    void setUp() {
        groupsRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    void testSaveNewGroup() {
        Group group = createGroup("TestGroup");
        StepVerifier.create(groupsService.save(group))
                .assertNext(savedGroup -> {
                    Assertions.assertNotNull(savedGroup.getId());
                    Assertions.assertEquals("TestGroup", savedGroup.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void testSaveExistingGroup() {
        Group group = createGroup("TestGroup");
        Group saved = groupsService.save(group).block();
        Assertions.assertNotNull(saved);

        StepVerifier.create(groupsService.save(saved))
                .assertNext(updatedGroup -> Assertions.assertEquals(saved.getId(), updatedGroup.getId()))
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testAddGroup() {
        GroupReq req = new GroupReq();
        req.setName("NewGroup");
        StepVerifier.create(groupsService.operate(req))
                .assertNext(addedGroup -> {
                    Assertions.assertNotNull(addedGroup.getId());
                    Assertions.assertEquals("NewGroup", addedGroup.getName());
                })
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testAddGroup_AlreadyExists() {
        groupsService.save(createGroup("ExistingGroup")).block();
        GroupReq req = new GroupReq();
        req.setName("ExistingGroup");
        StepVerifier.create(groupsService.operate(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(5)
    void testModifyGroup() {
        Group saved = groupsService.save(createGroup("ModifyGroup")).block();
        Assertions.assertNotNull(saved);

        GroupReq req = new GroupReq();
        req.setCode(saved.getCode());
        req.setName("ModifyGroup");

        StepVerifier.create(groupsService.operate(req))
                .assertNext(modifiedGroup -> Assertions.assertEquals(saved.getId(), modifiedGroup.getId()))
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testModifyGroup_NotFound() {
        GroupReq req = new GroupReq();
        req.setCode(java.util.UUID.randomUUID());
        StepVerifier.create(groupsService.operate(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(7)
    void testDelete() {
        Group saved = groupsService.save(createGroup("DeleteGroup")).block();
        Assertions.assertNotNull(saved);

        GroupReq req = new GroupReq();
        req.setCode(saved.getCode());

        StepVerifier.create(groupsService.delete(req))
                .verifyComplete();

        Assertions.assertNotNull(saved.getId());
        StepVerifier.create(groupsRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(8)
    void testPage() {
        groupsService.save(createGroup("Group1")).block();
        groupsService.save(createGroup("Group2")).block();
        GroupReq req = new GroupReq();
        StepVerifier.create(groupsService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(9)
    void testPage_Empty() {
        GroupReq req = new GroupReq();
        StepVerifier.create(groupsService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(0, page.getTotalElements());
                    Assertions.assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }
}