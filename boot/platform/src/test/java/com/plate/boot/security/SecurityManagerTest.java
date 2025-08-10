package com.plate.boot.security;

import com.plate.boot.security.core.user.UserReq;
import com.plate.boot.security.core.user.UsersService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * SecurityManager Unit Tests
 * 
 * <p>This test class provides unit tests for the SecurityManager class, covering:</p>
 * <ul>
 *   <li>User registration and modification</li>
 * </ul>
 *
 * @author Qwen Code
 */
class SecurityManagerTest {

    @Nested
    @DisplayName("User Registration and Modification Tests")
    class UserRegistrationModificationTests {

        @Test
        @DisplayName("Should register new user when code is empty")
        void shouldRegisterNewUserWhenCodeIsEmpty() {
            // Given
            UserReq request = new UserReq();
            request.setCode(null); // Empty code indicates new user
            User newUser = new User();
            newUser.setCode(UUID.randomUUID());
            newUser.setUsername("newuser");
            
            UsersService usersService = mock(UsersService.class);
            when(usersService.add(request)).thenReturn(Mono.just(newUser));

            // When
            SecurityManager securityManager = new SecurityManager(usersService);
            Mono<User> result = securityManager.registerOrModifyUser(request);

            // Then
            StepVerifier.create(result)
                .expectNextMatches(user -> user.getUsername().equals("newuser"))
                .verifyComplete();
                
            verify(usersService).add(request);
        }

        @Test
        @DisplayName("Should modify existing user when code is provided")
        void shouldModifyExistingUserWhenCodeIsProvided() {
            // Given
            UserReq request = new UserReq();
            UUID userCode = UUID.randomUUID();
            request.setCode(userCode); // Non-empty code indicates modification
            User modifiedUser = new User();
            modifiedUser.setCode(userCode);
            modifiedUser.setUsername("modifieduser");
            
            UsersService usersService = mock(UsersService.class);
            when(usersService.operate(request)).thenReturn(Mono.just(modifiedUser));

            // When
            SecurityManager securityManager = new SecurityManager(usersService);
            Mono<User> result = securityManager.registerOrModifyUser(request);

            // Then
            StepVerifier.create(result)
                .expectNextMatches(user -> user.getUsername().equals("modifieduser"))
                .verifyComplete();
                
            verify(usersService).operate(request);
        }
    }
}