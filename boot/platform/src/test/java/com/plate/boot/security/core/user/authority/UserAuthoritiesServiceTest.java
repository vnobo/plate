package com.plate.boot.security.core.user.authority;

import com.plate.boot.config.InfrastructureConfiguration;
import com.plate.boot.security.core.user.UserEvent;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserAuthoritiesServiceTest {

    private final UUID userCode = UUID.randomUUID();
    @Autowired
    private UserAuthoritiesService authoritiesService;
    @Autowired
    private UserAuthoritiesRepository authoritiesRepository;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        authoritiesRepository.deleteAll().block();
    }

    private UserAuthority createUserAuthority(String authority) {
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUserCode(userCode);
        userAuthority.setAuthority(authority);
        return userAuthority;
    }

    @Test
    @Order(1)
    void testSave() {
        UserAuthority authority = createUserAuthority("TEST_SAVE");
        StepVerifier.create(authoritiesService.save(authority))
                .assertNext(saved -> {
                    Assertions.assertNotNull(saved.getId());
                    Assertions.assertEquals("TEST_SAVE", saved.getAuthority());
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void testSearch() {
        authoritiesService.save(createUserAuthority("TEST_SEARCH_1")).block();
        authoritiesService.save(createUserAuthority("TEST_SEARCH_2")).block();

        UserAuthorityReq req = new UserAuthorityReq();
        req.setUserCode(userCode);

        StepVerifier.create(authoritiesService.search(req))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testOperate_Create() {
        UserAuthorityReq req = new UserAuthorityReq();
        req.setUserCode(userCode);
        req.setAuthority("TEST_OPERATE");

        StepVerifier.create(authoritiesService.operate(req))
                .assertNext(saved -> Assertions.assertEquals("TEST_OPERATE", saved.getAuthority()))
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testOperate_Update() {
        UserAuthority saved = authoritiesService.save(createUserAuthority("TEST_OPERATE_UPDATE")).block();
        Assertions.assertNotNull(saved);

        UserAuthorityReq req = new UserAuthorityReq();
        req.setUserCode(userCode);
        req.setAuthority("TEST_OPERATE_UPDATE");

        StepVerifier.create(authoritiesService.operate(req))
                .assertNext(updated -> {
                    Assertions.assertEquals(saved.getId(), updated.getId());
                    Assertions.assertEquals("TEST_OPERATE_UPDATE", updated.getAuthority());
                })
                .verifyComplete();
    }

    @Test
    @Order(5)
    void testDelete() {
        UserAuthority saved = authoritiesService.save(createUserAuthority("TEST_DELETE")).block();
        Assertions.assertNotNull(saved);

        UserAuthorityReq req = new UserAuthorityReq();
        req.setId(saved.getId());

        StepVerifier.create(authoritiesService.delete(req))
                .verifyComplete();

        StepVerifier.create(authoritiesRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testOnUserDeletedEvent() {
        authoritiesService.save(createUserAuthority("TEST_EVENT_DELETE")).block();
        User user = new User();
        user.setCode(userCode);
        UserEvent event = UserEvent.delete(user);

        eventPublisher.publishEvent(event);

        // Allow some time for the event to be processed
        StepVerifier.create(authoritiesRepository.findByUserCode(userCode).collectList().delayElement(java.time.Duration.ofMillis(500)))
                .assertNext(list -> Assertions.assertTrue(list.isEmpty()))
                .verifyComplete();
    }
}