package com.plate.boot.security.core.user;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

@SpringBootTest
@Import(InfrastructureConfiguration.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsersServiceTest {
    @Autowired
    private UsersService usersService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setDisabled(false);
        return user;
    }

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    void testSaveNewUser() {
        User user = createUser("testuser", "Password123");
        StepVerifier.create(usersService.save(user))
                .assertNext(savedUser -> {
                    Assertions.assertNotNull(savedUser.getId());
                    Assertions.assertEquals("testuser", savedUser.getUsername());
                })
                .verifyComplete();
    }

    @Test
    @Order(2)
    void testSaveExistingUser() {
        User user = createUser("testuser", "Password123");
        User saved = usersService.save(user).block();
        Assertions.assertNotNull(saved);

        saved.setName("Updated Name");
        StepVerifier.create(usersService.save(saved))
                .assertNext(updatedUser -> {
                    Assertions.assertEquals("Updated Name", updatedUser.getName());
                    Assertions.assertEquals(saved.getId(), updatedUser.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(3)
    void testFindByUsername() {
        User user = createUser("testuser", "Password123");
        usersService.save(user).block();

        StepVerifier.create(usersService.findByUsername("testuser"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @Order(4)
    void testFindByUsername_NotFound() {
        StepVerifier.create(usersService.findByUsername("nonexistent"))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(5)
    void testDelete() {
        User user = createUser("testuser", "Password123");
        User saved = usersService.save(user).block();
        Assertions.assertNotNull(saved);

        StepVerifier.create(usersService.delete(saved.getId()))
                .verifyComplete();

        StepVerifier.create(usersRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @Order(6)
    void testDelete_NotFound() {
        StepVerifier.create(usersService.delete(999L))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(7)
    void testQuery() {
        usersService.save(createUser("user1", "Password123")).block();
        usersService.save(createUser("user2", "Password123")).block();

        StepVerifier.create(usersService.query(PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(8)
    void testQuery_Empty() {
        StepVerifier.create(usersService.query(PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(0, page.getTotalElements());
                    Assertions.assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    @Order(9)
    void testAddUser() {
        UserReq req = new UserReq();
        req.setUsername("newuser");
        req.setPassword("Password123");
        StepVerifier.create(usersService.add(req))
                .assertNext(addedUser -> {
                    Assertions.assertNotNull(addedUser.getId());
                    Assertions.assertEquals("newuser", addedUser.getUsername());
                })
                .verifyComplete();
    }

    @Test
    @Order(10)
    void testAddUser_AlreadyExists() {
        usersService.save(createUser("existinguser", "Password123")).block();
        UserReq req = new UserReq();
        req.setUsername("existinguser");
        req.setPassword("Password123");
        StepVerifier.create(usersService.add(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(11)
    void testModifyUser() {
        User saved = usersService.save(createUser("modifyuser", "Password123")).block();
        Assertions.assertNotNull(saved);

        UserReq req = new UserReq();
        req.setUsername("modifyuser");
        req.setName("Modified Name");

        StepVerifier.create(usersService.modify(req))
                .assertNext(modifiedUser -> {
                    Assertions.assertEquals("Modified Name", modifiedUser.getName());
                    Assertions.assertEquals(saved.getId(), modifiedUser.getId());
                })
                .verifyComplete();
    }

    @Test
    @Order(12)
    void testModifyUser_NotFound() {
        UserReq req = new UserReq();
        req.setUsername("nonexistent");
        StepVerifier.create(usersService.modify(req))
                .expectError(RestServerException.class)
                .verify();
    }

    @Test
    @Order(13)
    void testPage() {
        usersService.save(createUser("user1", "Password123")).block();
        usersService.save(createUser("user2", "Password123")).block();
        UserReq req = new UserReq();
        StepVerifier.create(usersService.page(req, PageRequest.of(0, 10)))
                .assertNext(page -> {
                    Assertions.assertEquals(2, page.getTotalElements());
                    Assertions.assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    @Order(14)
    void testDeleteUserReq() {
        User saved = usersService.save(createUser("deleteuser", "Password123")).block();
        Assertions.assertNotNull(saved);

        UserReq req = new UserReq();
        req.setCode(saved.getCode());

        StepVerifier.create(usersService.delete(req))
                .verifyComplete();

        StepVerifier.create(usersRepository.findById(saved.getId()))
                .expectNextCount(0)
                .verifyComplete();
    }
}