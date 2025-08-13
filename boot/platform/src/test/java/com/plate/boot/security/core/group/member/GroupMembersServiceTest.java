package com.plate.boot.security.core.group.member;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.core.group.Group;
import com.plate.boot.security.core.group.GroupsRepository;
import com.plate.boot.security.core.user.UserEvent;
import com.plate.boot.security.core.user.UsersRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupMembersServiceTest {

    @Autowired
    private GroupMembersService membersService;

    @Autowired
    private GroupMembersRepository membersRepository;

    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private com.plate.boot.security.core.user.User testUser;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        membersRepository.deleteAll().block();
        usersRepository.deleteAll().block();
        groupsRepository.deleteAll().block();

        com.plate.boot.security.core.user.User user = new com.plate.boot.security.core.user.User();
        user.setUsername("testuser");
        user.setPassword("Password123");
        user.setCode(UUID.randomUUID());
        testUser = usersRepository.save(user).block();

        Group group = new Group();
        group.setName("TestGroup");
        group.setCode(UUID.randomUUID());
        testGroup = groupsRepository.save(group).block();
    }

    private GroupMember createGroupMember() {
        GroupMember member = new GroupMember();
        member.setUserCode(testUser.getCode());
        member.setGroupCode(testGroup.getCode());
        return member;
    }

    @Test
    @Order(1)
    void testSave() {
        GroupMember member = createGroupMember();
        StepVerifier.create(membersService.save(member))
                .assertNext(saved -> {
                    Assertions.assertNotNull(saved.getId());
                    Assertions.assertEquals(testUser.getCode(), saved.getUserCode());
                    Assertions.assertEquals(testGroup.getCode(), saved.getGroupCode());
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void testSearch() {
        membersService.save(createGroupMember()).block();
        GroupMemberReq req = new GroupMemberReq();
        req.setUsername("testuser");

        StepVerifier.create(membersService.search(req, PageRequest.of(0, 10)))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testPage() {
        membersService.save(createGroupMember()).block();
        GroupMemberReq req = new GroupMemberReq();
        req.setUsername("testuser");

        StepVerifier.create(membersService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(1, page.getTotalElements());
                    Assertions.assertEquals(1, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testOperate_Create() {
        GroupMemberReq req = new GroupMemberReq();
        req.setUserCode(testUser.getCode());
        req.setGroupCode(testGroup.getCode());

        StepVerifier.create(membersService.operate(req))
                .assertNext(saved -> Assertions.assertNotNull(saved.getId()))
                .verifyComplete();
    }

    @Test
    @Order(5)
    void testDelete() {
        GroupMember saved = membersService.save(createGroupMember()).block();
        Assertions.assertNotNull(saved);

        GroupMemberReq req = new GroupMemberReq();
        req.setId(saved.getId());

        StepVerifier.create(membersService.delete(req))
                .verifyComplete();

        StepVerifier.create(membersRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testOnUserDeletedEvent() {
        membersService.save(createGroupMember()).block();
        UserEvent event = UserEvent.delete(testUser);

        eventPublisher.publishEvent(event);

        StepVerifier.create(membersRepository.findByUserCode(testUser.getCode()).collectList()
                        .delayElement(java.time.Duration.ofMillis(500)))
                .assertNext(list -> Assertions.assertTrue(list.isEmpty()))
                .verifyComplete();
    }
}