package com.plate.boot.security.oauth2;

import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Oauth2UserService Unit Tests
 *
 * <p>This test class provides unit tests for the Oauth2UserService class, covering:</p>
 * <ul>
 *   <li>Random password generation</li>
 *   <li>Local user loading and registration</li>
 *   <li>User modification with OAuth2 data</li>
 *   <li>Conversion between OAuth2User and User entities</li>
 * </ul>
 *
 * @author Qwen Code
 */
class Oauth2UserServiceTest {

    private Oauth2UserService oauth2UserService;
    private SecurityManager securityManager;

    @BeforeEach
    void setUp() {
        securityManager = mock(SecurityManager.class);
        oauth2UserService = new Oauth2UserService(securityManager);
    }

    @Nested
    @DisplayName("Password Generation Tests")
    class PasswordGenerationTests {

        @Test
        @DisplayName("Should generate random password")
        void shouldGenerateRandomPassword() {
            // When
            String password = Oauth2UserService.generateRandoPassword();

            // Then
            assertThat(password).isNotNull();
            assertThat(password).isNotBlank();
            // Base64 encoded 16 bytes should be 24 characters long
            assertThat(password).hasSize(24);
        }

        @Test
        @DisplayName("Should generate different passwords each time")
        void shouldGenerateDifferentPasswordsEachTime() {
            // When
            String password1 = Oauth2UserService.generateRandoPassword();
            String password2 = Oauth2UserService.generateRandoPassword();

            // Then
            assertThat(password1).isNotEqualTo(password2);
        }
    }

    @Nested
    @DisplayName("User Conversion Tests")
    class UserConversionTests {

        @Test
        @DisplayName("Should convert OAuth2User to UserReq for GitHub")
        void shouldConvertOAuth2UserToUserReqForGitHub() {
            // Given
            String registrationId = "GITHUB";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("login", "testuser");
            attributes.put("id", 12345);
            attributes.put("name", "Test User");
            attributes.put("email", "test@example.com");
            attributes.put("avatar_url", "http://example.com/avatar.jpg");
            attributes.put("bio", "Test bio");

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "login");

            // When
            UserReq userReq = oauth2UserService.convertToUserRequest(registrationId, oAuth2User);

            // Then
            assertThat(userReq).isNotNull();
            assertThat(userReq.getUsername()).isEqualTo("github#testuser-12345");
            assertThat(userReq.getName()).isEqualTo("Test User");
            assertThat(userReq.getEmail()).isEqualTo("test@example.com");
            assertThat(userReq.getAvatar()).isEqualTo("http://example.com/avatar.jpg");
            assertThat(userReq.getBio()).isEqualTo("Test bio");
            assertThat(userReq.getPassword()).isNotBlank();
            assertThat(userReq.getExtend()).isNotNull();
        }

        @Test
        @DisplayName("Should convert OAuth2User to UserReq for Gitee")
        void shouldConvertOAuth2UserToUserReqForGitee() {
            // Given
            String registrationId = "gitee";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("login", "testuser");
            attributes.put("id", 12345);
            attributes.put("name", "Test User");
            attributes.put("email", "test@example.com");
            attributes.put("avatar_url", "http://example.com/avatar.jpg");
            attributes.put("bio", "Test bio");

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "login");

            // When
            UserReq userReq = oauth2UserService.convertToUserRequest(registrationId, oAuth2User);

            // Then
            assertThat(userReq).isNotNull();
            assertThat(userReq.getUsername()).isEqualTo("gitee#testuser-12345");
            assertThat(userReq.getName()).isEqualTo("Test User");
            assertThat(userReq.getEmail()).isEqualTo("test@example.com");
            assertThat(userReq.getAvatar()).isEqualTo("http://example.com/avatar.jpg");
            assertThat(userReq.getBio()).isEqualTo("Test bio");
            assertThat(userReq.getPassword()).isNotBlank();
            assertThat(userReq.getExtend()).isNotNull();
        }

        @Test
        @DisplayName("Should convert OAuth2User to UserReq for other providers")
        void shouldConvertOAuth2UserToUserReqForOtherProviders() {
            // Given
            String registrationId = "google";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("sub", "123456789");
            attributes.put("name", "Test User");
            attributes.put("email", "test@example.com");

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "sub");

            // When
            UserReq userReq = oauth2UserService.convertToUserRequest(registrationId, oAuth2User);

            // Then
            assertThat(userReq).isNotNull();
            assertThat(userReq.getUsername()).isEqualTo("123456789");
            assertThat(userReq.getPassword()).isNotBlank();
        }

        @Test
        @DisplayName("Should convert User and OAuth2User to SecurityDetails")
        void shouldConvertUserAndOAuth2UserToSecurityDetails() {
            // Given
            User user = new User();
            user.setCode(UUID.randomUUID());
            user.setUsername("testuser");
            user.setPassword("encodedPassword");

            Map<String, Object> oauthAttributes = new HashMap<>();
            oauthAttributes.put("sub", "123456789");
            oauthAttributes.put("name", "Test User");

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, oauthAttributes, "sub");

            // When
            OAuth2User result = oauth2UserService.convertToOauth2User(user, oAuth2User);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isInstanceOf(SecurityDetails.class);
            SecurityDetails securityDetails = (SecurityDetails) result;
            assertThat(securityDetails.getCode()).isEqualTo(user.getCode());
            assertThat(securityDetails.getUsername()).isEqualTo(user.getUsername());
            assertThat(securityDetails.getPassword()).isEqualTo(user.getPassword());
            assertThat(securityDetails.getAttributes()).containsEntry("username", "testuser");
            assertThat(securityDetails.getAttributes()).containsEntry("sub", "123456789");
        }
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user")
        void shouldRegisterNewUser() {
            // Given
            String registrationId = "github";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("login", "testuser");
            attributes.put("id", 12345);

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "login");

            UserReq expectedRequest = new UserReq();
            expectedRequest.setUsername("github#testuser-12345");
            expectedRequest.setPassword(anyString());

            User registeredUser = new User();
            registeredUser.setCode(UUID.randomUUID());
            registeredUser.setUsername("github#testuser-12345");

            when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(registeredUser));

            // When
            Mono<User> result = oauth2UserService.registerUser(registrationId, oAuth2User);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(user -> {
                        assertThat(user).isNotNull();
                        assertThat(user.getCode()).isEqualTo(registeredUser.getCode());
                        return true;
                    })
                    .verifyComplete();

            // Verify security manager method was called
            verify(securityManager).registerOrModifyUser(any(UserReq.class));
        }
    }

    @Nested
    @DisplayName("User Modification Tests")
    class UserModificationTests {

        @Test
        @DisplayName("Should modify existing user")
        void shouldModifyExistingUser() {
            // Given
            String registrationId = "github";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("login", "testuser");
            attributes.put("id", 12345);

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "login");

            User user = new User();
            user.setId(1L);
            user.setCode(UUID.randomUUID());
            user.setUsername("github#testuser-12345");

            when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(user));

            // When
            Mono<Void> result = oauth2UserService.modifyUser(user, registrationId, oAuth2User);

            // Then
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify security manager method was called
            verify(securityManager).registerOrModifyUser(any(UserReq.class));
        }
    }

    @Nested
    @DisplayName("Local User Loading Tests")
    class LocalUserLoadingTests {

        @Test
        @DisplayName("Should load existing local user")
        void shouldLoadExistingLocalUser() {
            // Given
            String registrationId = "github";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("login", "testuser");
            attributes.put("id", 12345);

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "login");

            User existingUser = new User();
            existingUser.setCode(UUID.randomUUID());
            existingUser.setUsername("github#testuser-12345");

            when(securityManager.loadByOauth2(registrationId, oAuth2User.getName())).thenReturn(Mono.just(existingUser));

            Map<String, Object> resultAttributes = new HashMap<>();
            resultAttributes.put("sub", "123456789");
            resultAttributes.put("username", "github#testuser-12345");

            Collection<GrantedAuthority> resultAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            SecurityDetails expectedResult = new SecurityDetails(resultAuthorities, resultAttributes, "username");
            expectedResult.setCode(existingUser.getCode());

            when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(existingUser));

            // When
            Mono<OAuth2User> result = oauth2UserService.loadLocalUser(registrationId, oAuth2User);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(oauth2User -> {
                        assertThat(oauth2User).isNotNull();
                        assertThat(oauth2User).isInstanceOf(SecurityDetails.class);
                        return true;
                    })
                    .verifyComplete();

            // Verify security manager methods were called
            verify(securityManager).loadByOauth2(registrationId, oAuth2User.getName());
        }

        @Test
        @DisplayName("Should register new user when local user not found")
        void shouldRegisterNewUserWhenLocalUserNotFound() {
            // Given
            String registrationId = "github";
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("login", "testuser");
            attributes.put("id", 12345);

            Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            OAuth2User oAuth2User = new DefaultOAuth2User(authorities, attributes, "login");

            when(securityManager.loadByOauth2(registrationId, oAuth2User.getName())).thenReturn(Mono.empty());

            User newUser = new User();
            newUser.setCode(UUID.randomUUID());
            newUser.setUsername("github#testuser-12345");

            when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(newUser));

            // When
            Mono<OAuth2User> result = oauth2UserService.loadLocalUser(registrationId, oAuth2User);

            // Then
            StepVerifier.create(result)
                    .expectNextMatches(oauth2User -> {
                        assertThat(oauth2User).isNotNull();
                        return true;
                    })
                    .verifyComplete();

            // Verify security manager methods were called
            verify(securityManager).loadByOauth2(registrationId, oAuth2User.getName());
            verify(securityManager).registerOrModifyUser(any(UserReq.class));
        }
    }
}