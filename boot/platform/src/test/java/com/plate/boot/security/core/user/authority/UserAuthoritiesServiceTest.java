package com.plate.boot.security.core.user.authority;

import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for {@link UserAuthoritiesService}.
 * Tests user authority management functionality including CRUD operations and search capabilities.
 *
 * @author CodeBuddy
 */
@ExtendWith(MockitoExtension.class)
class UserAuthoritiesServiceTest {

    @Mock
    private UserAuthoritiesRepository userAuthoritiesRepository;

    private UserAuthoritiesService userAuthoritiesService;
    private UserAuthority testUserAuthority;
    private UserAuthorityReq testUserAuthorityReq;

    @BeforeEach
    void setUp() {
        userAuthoritiesService = new UserAuthoritiesService(userAuthoritiesRepository);
        
        // Setup test user authority
        testUserAuthority = new UserAuthority();
        testUserAuthority.setId(1);
        testUserAuthority.setCode(UUID.randomUUID());
        testUserAuthority.setUserCode(UUID.randomUUID());
        testUserAuthority.setAuthority("ROLE_USER");
        testUserAuthority.setTenantCode("tenant1");
        
        // Setup test user authority request
        testUserAuthorityReq = new UserAuthorityReq();
        testUserAuthorityReq.setCode(testUserAuthority.getCode());
        testUserAuthorityReq.setUserCode(testUserAuthority.getUserCode());
        testUserAuthorityReq.setAuthority("ROLE_USER");
        testUserAuthorityReq.setTenantCode("tenant1");
    }

    @Nested
    @DisplayName("User Authority Operation Tests")
    class UserAuthorityOperationTests {

        @Test
        @DisplayName("Should operate on user authority successfully")
        void shouldOperateOnUserAuthoritySuccessfully() {
            // Arrange
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.ENTITY_TEMPLATE.selectOne(any(), eq(UserAuthority.class)))
                        .thenReturn(Mono.just(testUserAuthority));

                // Act & Assert
                StepVerifier.create(userAuthoritiesService.operate(testUserAuthorityReq))
                        .expectNext(testUserAuthority)
                        .verifyComplete();
            }
        }

        @Test
        @DisplayName("Should create new user authority when not found")
        void shouldCreateNewUserAuthorityWhenNotFound() {
            // Arrange
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.ENTITY_TEMPLATE.selectOne(any(), eq(UserAuthority.class)))
                        .thenReturn(Mono.empty());
                
                when(userAuthoritiesRepository.save(any(UserAuthority.class))).thenReturn(Mono.just(testUserAuthority));

                // Act & Assert
                StepVerifier.create(userAuthoritiesService.operate(testUserAuthorityReq))
                        .expectNext(testUserAuthority)
                        .verifyComplete();

                verify(userAuthoritiesRepository).save(any(UserAuthority.class));
            }
        }
    }

    @Nested
    @DisplayName("User Authority Search Tests")
    class UserAuthoritySearchTests {

        @Test
        @DisplayName("Should search user authorities successfully")
        void shouldSearchUserAuthoritiesSuccessfully() {
            // Arrange - Mock the cache query method
            try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
                mockedDatabaseUtils.when(() -> DatabaseUtils.ENTITY_TEMPLATE.select(any(), eq(UserAuthority.class)))
                        .thenReturn(Flux.just(testUserAuthority));

                // Act & Assert
                StepVerifier.create(userAuthoritiesService.search(testUserAuthorityReq))
                        .expectNext(testUserAuthority)
                        .verifyComplete();
            }
        }
    }

    @Nested
    @DisplayName("User Authority Deletion Tests")
    class UserAuthorityDeletionTests {

        @Test
        @DisplayName("Should delete user authority successfully")
        void shouldDeleteUserAuthoritySuccessfully() {
            // Arrange
            when(userAuthoritiesRepository.delete(any(UserAuthority.class))).thenReturn(Mono.empty());

            // Act & Assert
            StepVerifier.create(userAuthoritiesService.delete(testUserAuthorityReq))
                    .verifyComplete();

            verify(userAuthoritiesRepository).delete(any(UserAuthority.class));
        }
    }
}