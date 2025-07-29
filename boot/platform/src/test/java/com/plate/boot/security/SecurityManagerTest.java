package com.plate.boot.security;

import com.plate.boot.commons.utils.DatabaseUtils;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import com.plate.boot.security.core.user.UsersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link SecurityManager} focusing on password update functionality.
 */
@ExtendWith(MockitoExtension.class)
class SecurityManagerTest {

    @Mock
    private UsersService usersService;

    @Mock
    private Cache cache;

    @InjectMocks
    private SecurityManager securityManager;

    private User testUser;
    private UUID userCode;
    private String testUsername;
    private String testPassword;

    @BeforeEach
    void setUp() {
        userCode = UUID.randomUUID();
        testUsername = "testuser";
        testPassword = "password123";
        
        testUser = createTestUser(userCode, testUsername, testPassword, "Test User", false);
        
        // Set the cache field using reflection
        ReflectionTestUtils.setField(securityManager, "cache", cache);
    }

    /**
     * Helper method to create test users
     */
    private User createTestUser(UUID code, String username, String password, String name, Boolean disabled) {
        User user = new User();
        user.setCode(code);
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setTenantCode("default");
        user.setLoginTime(LocalDateTime.now());
        user.setDisabled(disabled);
        user.setAccountExpired(false);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
        return user;
    }

    @Test
    @DisplayName("Should update password successfully")
    void testUpdatePassword_Success() {
        // Arrange
        String newPassword = "newPassword123";
        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        Map<String, Object> attributes = Map.of("username", testUsername, "userCode", userCode);
        SecurityDetails securityDetails = SecurityDetails.of(testUser, authorities, attributes);
        
        // Mock the static ENTITY_TEMPLATE field
        R2dbcEntityTemplate entityTemplate = mock(R2dbcEntityTemplate.class);
        R2dbcEntityTemplate.ReactiveUpdate reactiveUpdate = mock(R2dbcEntityTemplate.ReactiveUpdate.class);
        R2dbcEntityTemplate.TerminatingUpdate terminatingUpdate = mock(R2dbcEntityTemplate.TerminatingUpdate.class);
        
        // Set up the mock chain
        when(entityTemplate.update(User.class)).thenReturn(reactiveUpdate);
        when(reactiveUpdate.matching(any(Query.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.apply(any(Update.class))).thenReturn(Mono.just(1L));
        
        // Use reflection to set the static field
        try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
            DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
            
            // Act & Assert
            StepVerifier.create(securityManager.updatePassword(securityDetails, newPassword))
                    .expectNextMatches(userDetails -> {
                        assertThat(userDetails.getUsername()).isEqualTo(testUsername);
                        assertThat(userDetails.getPassword()).isEqualTo(newPassword);
                        return true;
                    })
                    .verifyComplete();
            
            // Verify that the database update was called
            verify(terminatingUpdate).apply(any(Update.class));
        } finally {
            // Clean up the static field
            DatabaseUtils.ENTITY_TEMPLATE = null;
        }
    }

    @Test
    @DisplayName("Should handle database error during password update")
    void testUpdatePassword_DatabaseError() {
        // Arrange
        String newPassword = "newPassword123";
        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        Map<String, Object> attributes = Map.of("username", testUsername, "userCode", userCode);
        SecurityDetails securityDetails = SecurityDetails.of(testUser, authorities, attributes);
        
        // Mock database update failure
        R2dbcEntityTemplate entityTemplate = mock(R2dbcEntityTemplate.class);
        R2dbcEntityTemplate.ReactiveUpdate reactiveUpdate = mock(R2dbcEntityTemplate.ReactiveUpdate.class);
        R2dbcEntityTemplate.TerminatingUpdate terminatingUpdate = mock(R2dbcEntityTemplate.TerminatingUpdate.class);
        
        // Set up the mock chain
        when(entityTemplate.update(User.class)).thenReturn(reactiveUpdate);
        when(reactiveUpdate.matching(any(Query.class))).thenReturn(terminatingUpdate);
        when(terminatingUpdate.apply(any(Update.class)))
                .thenReturn(Mono.error(new RuntimeException("Database update failed")));
        
        // Use reflection to set the static field
        try (MockedStatic<DatabaseUtils> mockedDatabaseUtils = mockStatic(DatabaseUtils.class)) {
            DatabaseUtils.ENTITY_TEMPLATE = entityTemplate;
            
            // Act & Assert
            StepVerifier.create(securityManager.updatePassword(securityDetails, newPassword))
                    .expectErrorMatches(throwable -> 
                        throwable instanceof RuntimeException && 
                        throwable.getMessage().equals("Database update failed"))
                    .verify();
        } finally {
            // Clean up the static field
            DatabaseUtils.ENTITY_TEMPLATE = null;
        }
    }

    @Test
    @DisplayName("Should register new user when no code is provided")
    void testRegisterOrModifyUser_NewUser() {
        // Arrange
        UserReq request = new UserReq();
        request.setUsername("newuser");
        request.setPassword("newpass");
        request.setName("New User");
        
        User expectedUser = createTestUser(UUID.randomUUID(), "newuser", "newpass", "New User", false);
        
        when(usersService.add(request)).thenReturn(Mono.just(expectedUser));

        // Act & Assert
        StepVerifier.create(securityManager.registerOrModifyUser(request))
                .expectNext(expectedUser)
                .verifyComplete();
        
        verify(usersService).add(request);
        verify(usersService, never()).operate(any());
    }

    @Test
    @DisplayName("Should modify existing user when code is provided")
    void testRegisterOrModifyUser_ExistingUser() {
        // Arrange
        UserReq request = new UserReq();
        request.setCode(userCode);
        request.setUsername(testUsername);
        request.setName("Updated User");
        
        User updatedUser = createTestUser(userCode, testUsername, testPassword, "Updated User", false);
        
        when(usersService.operate(request)).thenReturn(Mono.just(updatedUser));

        // Act & Assert
        StepVerifier.create(securityManager.registerOrModifyUser(request))
                .expectNext(updatedUser)
                .verifyComplete();
        
        verify(usersService).operate(request);
        verify(usersService, never()).add(any());
    }
}