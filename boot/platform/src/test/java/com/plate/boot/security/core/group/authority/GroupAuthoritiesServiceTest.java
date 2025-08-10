package com.plate.boot.security.core.group.authority;

import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import reactor.test.StepVerifier;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupAuthoritiesServiceTest {

    private final UUID groupCode = UUID.randomUUID();
    @Autowired
    private GroupAuthoritiesService authoritiesService;
    @Autowired
    private GroupAuthoritiesRepository authoritiesRepository;

    @BeforeEach
    void setUp() {
        authoritiesRepository.deleteAll().block();
    }

    private GroupAuthority createGroupAuthority(String authority) {
        return new GroupAuthority(groupCode, authority);
    }

    @Test
    @Order(1)
    void testSave() {
        GroupAuthority authority = createGroupAuthority("TEST_SAVE");
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
        authoritiesService.save(createGroupAuthority("TEST_SEARCH_1")).block();
        authoritiesService.save(createGroupAuthority("TEST_SEARCH_2")).block();

        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(groupCode);

        StepVerifier.create(authoritiesService.search(req, PageRequest.of(0, 10)))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testBatch_AddNewAuthorities() {
        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(groupCode);
        req.setAuthorities(Set.of("AUTH_1", "AUTH_2"));

        StepVerifier.create(authoritiesService.batch(req))
                .expectNext(200)
                .verifyComplete();

        StepVerifier.create(authoritiesRepository.findByGroupCode(groupCode))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testBatch_AddAndKeepAuthorities() {
        authoritiesService.save(createGroupAuthority("AUTH_1")).block();

        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(groupCode);
        req.setAuthorities(Set.of("AUTH_1", "AUTH_2"));

        StepVerifier.create(authoritiesService.batch(req))
                .expectNext(200)
                .verifyComplete();

        StepVerifier.create(authoritiesRepository.findByGroupCode(groupCode))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @Order(5)
    void testBatch_RemoveAuthorities() {
        authoritiesService.save(createGroupAuthority("AUTH_1")).block();
        authoritiesService.save(createGroupAuthority("AUTH_2")).block();

        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(groupCode);
        req.setAuthorities(Set.of("AUTH_1"));

        StepVerifier.create(authoritiesService.batch(req))
                .expectNext(200)
                .verifyComplete();

        StepVerifier.create(authoritiesRepository.findByGroupCode(groupCode))
                .expectNextCount(1)
                .assertNext(authority -> Assertions.assertEquals("AUTH_1", authority.getAuthority()))
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testBatch_RemoveAllAuthorities() {
        authoritiesService.save(createGroupAuthority("AUTH_1")).block();

        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(groupCode);
        req.setAuthorities(Set.of());

        StepVerifier.create(authoritiesService.batch(req))
                .expectNext(200)
                .verifyComplete();

        StepVerifier.create(authoritiesRepository.findByGroupCode(groupCode))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(7)
    void testBatch_NullGroupCode() {
        GroupAuthorityReq req = new GroupAuthorityReq();
        StepVerifier.create(authoritiesService.batch(req))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @Order(8)
    void testOperate_Create() {
        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setGroupCode(groupCode);
        req.setAuthority("TEST_OPERATE");

        StepVerifier.create(authoritiesService.operate(req))
                .assertNext(saved -> Assertions.assertEquals("TEST_OPERATE", saved.getAuthority()))
                .verifyComplete();
    }

    @Test
    @Order(9)
    void testDelete() {
        GroupAuthority saved = authoritiesService.save(createGroupAuthority("TEST_DELETE")).block();
        Assertions.assertNotNull(saved);

        GroupAuthorityReq req = new GroupAuthorityReq();
        req.setId(saved.getId());

        StepVerifier.create(authoritiesService.delete(req))
                .verifyComplete();

        StepVerifier.create(authoritiesRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }
}