package com.plate.boot.security.core.user;

import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class UsersServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsersService usersService;

    private User testUser;
    private UserReq testUserReq;
    private UserRes testUserRes;

    @BeforeEach
    void setUp() {
        usersService = new UsersService(passwordEncoder, usersRepository);
        testUser = new User();
        testUser.setCode(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");

        testUserReq = new UserReq();
        testUserReq.setUsername("testuser");
        testUserReq.setPassword("password");
        testUserReq.setCode(testUser.getCode());

        testUserRes = new UserRes();
        testUserRes.setUsername("testuser");
    }

    @Nested
    @DisplayName("User Creation and Modification Tests")
    class UserCreationAndModificationTests {

        @Test
        @DisplayName("Should add a new user successfully")
        void shouldAddNewUserSuccessfully() {
            when(usersRepository.existsByUsernameIgnoreCase("testuser")).thenReturn(Mono.just(false));
            when(passwordEncoder.upgradeEncoding("password")).thenReturn(true);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(usersRepository.findByCode(any())).thenReturn(Mono.empty());
            when(usersRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

            StepVerifier.create(usersService.add(testUserReq))
                    .expectNextMatches(user -> {
                        assertThat(user.getUsername()).isEqualTo("testuser");
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should modify an existing user successfully")
        void shouldModifyUserSuccessfully() {
            when(usersRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));
            when(passwordEncoder.upgradeEncoding("password")).thenReturn(true);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(usersRepository.findByCode(testUser.getCode())).thenReturn(Mono.just(testUser));
            when(usersRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

            StepVerifier.create(usersService.modify(testUserReq))
                    .expectNext(testUser)
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should operate on a user successfully")
        void shouldOperateOnUserSuccessfully() {
            when(passwordEncoder.upgradeEncoding("password")).thenReturn(true);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(usersRepository.findByCode(testUser.getCode())).thenReturn(Mono.just(testUser));
            when(usersRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

            StepVerifier.create(usersService.operate(testUserReq))
                    .expectNext(testUser)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("User Deletion Tests")
    class UserDeletionTests {
        @Test
        @DisplayName("Should delete a user successfully")
        void shouldDeleteUserSuccessfully() {
            when(usersRepository.findByCode(testUser.getCode())).thenReturn(Mono.just(testUser));
            when(usersRepository.delete(testUser)).thenReturn(Mono.empty());

            StepVerifier.create(usersService.delete(testUserReq))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("User Search Tests")
    class UserSearchTests {
        @Test
        @DisplayName("Should search for users successfully")
        void shouldSearchForUsersSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(UserRes.class)))
                        .thenReturn(Flux.just(testUserRes));

                StepVerifier.create(usersService.search(testUserReq, PageRequest.of(0, 10)))
                        .expectNext(testUserRes)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should get a page of users successfully")
        void shouldGetPageOfUsersSuccessfully() {
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.query(any(String.class), any(Map.class), eq(UserRes.class)))
                        .thenReturn(Flux.just(testUserRes));
                mockedDatabaseUtils.when(() -> DatabaseUtils.count(any(String.class), any(Map.class)))
                        .thenReturn(Mono.just(1L));

                StepVerifier.create(usersService.page(testUserReq, PageRequest.of(0, 10)))
                        .expectNextMatches(page -> {
                            assertThat(page.getTotalElements()).isEqualTo(1);
                            assertThat(page.getContent()).contains(testUserRes);
                            return true;
                        })
                        .verifyComplete();
            }
        }
    }
}