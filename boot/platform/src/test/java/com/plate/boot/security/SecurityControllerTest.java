package com.plate.boot.security;

import com.plate.boot.security.core.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

/**
 * Comprehensive test suite for {@link SecurityController}.
 * Tests password change functionality and security endpoint behavior.
 *
 * @author <a href="https://github.com/vnobo">Alex bob</a>
 */
@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock
    private SecurityManager securityManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecurityController securityController;

    private WebTestClient webTestClient;
    private User testUser;
    private SecurityDetails testUserDetails;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToController(securityController).build();
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setCode(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setPassword("encodedOldPassword");
        testUser.setEmail("test@example.com");
        
        // Setup test user details
        testUserDetails = SecurityDetails.of(testUser, 
            List.of(new SimpleGrantedAuthority("ROLE_USER")), 
            Map.of("username", "testuser"));
    }

    @Nested
    @DisplayName("Password Change Tests")
    class PasswordChangeTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            request.setPassword("oldPassword");
            request.setNewPassword("newPassword");

            SecurityDetails updatedUserDetails = SecurityDetails.of(testUser, 
                List.of(new SimpleGrantedAuthority("ROLE_USER")), 
                Map.of("username", "testuser"));
            updatedUserDetails.setPassword("encodedNewPassword");

            when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
            when(securityManager.updatePassword(any(SecurityDetails.class), anyString()))
                    .thenReturn(Mono.just(updatedUserDetails));

            // Act & Assert
            webTestClient.mutateWith(csrf())
                    .mutateWith(mockUser(testUserDetails))
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Should return error when new password is same as old")
        void shouldReturnErrorWhenNewPasswordIsSame() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            request.setPassword("samePassword");
            request.setNewPassword("samePassword");

            // Act & Assert
            webTestClient.mutateWith(csrf())
                    .mutateWith(mockUser(testUserDetails))
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Should return error when current password verification fails")
        void shouldReturnErrorWhenPasswordVerificationFails() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            request.setPassword("wrongPassword");
            request.setNewPassword("newPassword");

            when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);
            // Use lenient() to avoid unnecessary stubbing exception
            lenient().when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
            lenient().when(securityManager.updatePassword(any(), anyString())).thenReturn(Mono.just(testUserDetails));

            // Act & Assert
            webTestClient.mutateWith(csrf())
                    .mutateWith(mockUser(testUserDetails))
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").exists()
                    .jsonPath("$.message").exists();
        }

        @Test
        @DisplayName("Should handle missing password fields")
        void shouldHandleMissingPasswordFields() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            // Leave password fields null

            // Act & Assert
            webTestClient.mutateWith(csrf())
                    .mutateWith(mockUser(testUserDetails))
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("Should handle empty password fields")
        void shouldHandleEmptyPasswordFields() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            request.setPassword("");
            request.setNewPassword("");

            // Act & Assert
            webTestClient.mutateWith(csrf())
                    .mutateWith(mockUser(testUserDetails))
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("Security Endpoint Tests")
    class SecurityEndpointTests {

        @Test
        @DisplayName("Should require authentication for password change")
        void shouldRequireAuthenticationForPasswordChange() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            request.setPassword("oldPassword");
            request.setNewPassword("newPassword");

            // Act & Assert - without authentication
            webTestClient.mutateWith(csrf())
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("Should require CSRF token for password change")
        void shouldRequireCsrfTokenForPasswordChange() {
            // Arrange
            SecurityController.ChangePasswordRequest request = new SecurityController.ChangePasswordRequest();
            request.setPassword("oldPassword");
            request.setNewPassword("newPassword");

            // Act & Assert - without CSRF token
            webTestClient.mutateWith(mockUser(testUserDetails))
                    .post().uri("/oauth2/change/password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }
}
