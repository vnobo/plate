package com.plate.boot.security.oauth2;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.security.SecurityDetails;
import com.plate.boot.security.SecurityManager;
import com.plate.boot.security.core.user.User;
import com.plate.boot.security.core.user.UserReq;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Oauth2UserService} (no Spring / OAuth2 provider required).
 * <p>
 * The {@code SecurityManager} collaborator is mocked. JSON handling relies on the shared
 * {@link ContextUtils#OBJECT_MAPPER}, which is seeded with a standalone {@link JsonMapper}
 * for the duration of the test class. The {@link Oauth2UserService#loadUser} override is a
 * thin delegation to {@code super.loadUser(...)} (a live WebClient call) and is covered via
 * {@link Oauth2UserService#loadLocalUser} instead; see the class report.
 */
@ExtendWith(MockitoExtension.class)
class Oauth2UserServiceTest {

    @Mock
    private SecurityManager securityManager;

    @InjectMocks
    private Oauth2UserService service;

    private static JsonMapper savedMapper;

    @BeforeAll
    static void setUpMapper() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDownMapper() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @Test
    void generateRandoPasswordProducesBase64Of16Bytes() {
        String password = Oauth2UserService.generateRandoPassword();

        assertThat(password).isNotBlank();
        assertThat(Base64.getDecoder().decode(password)).hasSize(16);
        assertThat(password).isNotEqualTo(Oauth2UserService.generateRandoPassword());
    }

    @Test
    void convertToUserRequestPopulatesGithubFields() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("alice");
        when(oAuth2User.getAttribute("id")).thenReturn(12345);
        when(oAuth2User.getAttribute("name")).thenReturn("Alice");
        when(oAuth2User.getAttribute("email")).thenReturn("alice@example.com");
        when(oAuth2User.getAttribute("avatar_url")).thenReturn("http://avatar");
        when(oAuth2User.getAttribute("bio")).thenReturn("bio");
        when(oAuth2User.getName()).thenReturn("openid-123");
        when(oAuth2User.getAttributes()).thenReturn(Map.of("login", "alice", "id", 12345));

        UserReq request = service.convertToUserRequest("github", oAuth2User);

        assertThat(request.getUsername()).isEqualTo("github#alice-12345");
        assertThat(request.getName()).isEqualTo("Alice");
        assertThat(request.getEmail()).isEqualTo("alice@example.com");
        assertThat(request.getAvatar()).isEqualTo("http://avatar");
        assertThat(request.getBio()).isEqualTo("bio");
        assertThat(request.getPassword()).isNotBlank();
        assertThat(request.getExtend()).isNotNull();
        assertThat(request.getExtend().get("oauth2").get("github")).isNotNull();
    }

    @Test
    void convertToUserRequestTreatsGiteeLikeGithub() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("bob");
        when(oAuth2User.getAttribute("id")).thenReturn(99);
        when(oAuth2User.getName()).thenReturn("openid-99");
        when(oAuth2User.getAttributes()).thenReturn(Map.of());

        UserReq request = service.convertToUserRequest("gitee", oAuth2User);

        assertThat(request.getUsername()).isEqualTo("gitee#bob-99");
        assertThat(request.getExtend()).isNotNull();
        assertThat(request.getExtend().get("oauth2").get("gitee")).isNotNull();
    }

    @Test
    void convertToUserRequestFallsBackToNameForOtherProvider() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getName()).thenReturn("carol");

        UserReq request = service.convertToUserRequest("google", oAuth2User);

        assertThat(request.getUsername()).isEqualTo("carol");
        assertThat(request.getExtend()).isNull();
    }

    @Test
    void convertToOauth2UserEnrichesAttributes() {
        User user = new User();
        user.setUsername("local-user");
        user.setCode(UUID.randomUUID());
        user.setName("Local User");

        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("sub", "x"));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(oAuth2User).getAuthorities();

        OAuth2User result = service.convertToOauth2User(user, oAuth2User);

        assertThat(result).isInstanceOf(SecurityDetails.class);
        assertThat(result.getAttributes()).containsEntry("username", "local-user");
        assertThat(result.getAuthorities()).isNotEmpty();
        assertThat(((SecurityDetails) result).getUsername()).isEqualTo("local-user");
    }

    @Test
    void registerUserConvertsAndRegisters() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("dave");
        when(oAuth2User.getAttribute("id")).thenReturn(1);
        when(oAuth2User.getName()).thenReturn("openid-1");
        when(oAuth2User.getAttributes()).thenReturn(Map.of());

        User registered = new User();
        registered.setUsername("github#dave-1");
        when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(registered));

        StepVerifier.create(service.registerUser("github", oAuth2User))
                .expectNext(registered)
                .verifyComplete();

        ArgumentCaptor<UserReq> captor = ArgumentCaptor.forClass(UserReq.class);
        verify(securityManager).registerOrModifyUser(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("github#dave-1");
    }

    @Test
    void modifyUserMergesOauth2IntoNullExtend() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("eve");
        when(oAuth2User.getAttribute("id")).thenReturn(2);
        when(oAuth2User.getName()).thenReturn("openid-2");
        when(oAuth2User.getAttributes()).thenReturn(Map.of());

        User user = new User();
        user.setId(10L);
        user.setUsername("local-eve");
        user.setCode(UUID.randomUUID());

        when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(user));

        StepVerifier.create(service.modifyUser(user, "github", oAuth2User)).verifyComplete();

        ArgumentCaptor<UserReq> captor = ArgumentCaptor.forClass(UserReq.class);
        verify(securityManager).registerOrModifyUser(captor.capture());
        UserReq request = captor.getValue();
        assertThat(request.getId()).isEqualTo(10L);
        assertThat(request.getUsername()).isEqualTo("local-eve");
        assertThat(request.getExtend().get("oauth2").get("registrationId")).isNotNull();
    }

    @Test
    void modifyUserPreservesExistingExtendAndOauth2() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("frank");
        when(oAuth2User.getAttribute("id")).thenReturn(3);
        when(oAuth2User.getName()).thenReturn("openid-3");
        when(oAuth2User.getAttributes()).thenReturn(Map.of());

        User user = new User();
        user.setId(11L);
        user.setUsername("local-frank");
        user.setCode(UUID.randomUUID());

        ObjectNode existingExtend = ContextUtils.OBJECT_MAPPER.createObjectNode();
        ObjectNode existingOauth2 = ContextUtils.OBJECT_MAPPER.createObjectNode();
        existingOauth2.put("legacy", "value");
        existingExtend.set("oauth2", existingOauth2);
        existingExtend.put("tenant", "x");
        user.setExtend(existingExtend);

        when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(user));

        StepVerifier.create(service.modifyUser(user, "github", oAuth2User)).verifyComplete();

        ArgumentCaptor<UserReq> captor = ArgumentCaptor.forClass(UserReq.class);
        verify(securityManager).registerOrModifyUser(captor.capture());
        UserReq request = captor.getValue();
        assertThat(request.getExtend().get("oauth2").get("registrationId")).isNotNull();
        assertThat(request.getExtend().get("oauth2").get("legacy").asText()).isEqualTo("value");
        assertThat(request.getExtend().get("tenant").asText()).isEqualTo("x");
    }

    @Test
    void loadLocalUserModifiesExistingUser() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("grace");
        when(oAuth2User.getAttribute("id")).thenReturn(4);
        when(oAuth2User.getName()).thenReturn("openid-4");
        when(oAuth2User.getAttributes()).thenReturn(Map.of());
        when(oAuth2User.getAuthorities()).thenReturn(List.of());

        User user = new User();
        user.setId(12L);
        user.setUsername("github#grace-4");
        user.setCode(UUID.randomUUID());

        when(securityManager.loadByOauth2("github", "openid-4")).thenReturn(Mono.just(user));
        when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(user));

        StepVerifier.create(service.loadLocalUser("github", oAuth2User))
                .assertNext(result -> {
                    assertThat(result).isInstanceOf(SecurityDetails.class);
                    assertThat(result.getAttributes()).containsEntry("username", "github#grace-4");
                })
                .verifyComplete();

        verify(securityManager).loadByOauth2("github", "openid-4");
        verify(securityManager).registerOrModifyUser(any(UserReq.class));
    }

    @Test
    void loadLocalUserRegistersWhenNotFound() {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("login")).thenReturn("heidi");
        when(oAuth2User.getAttribute("id")).thenReturn(5);
        when(oAuth2User.getName()).thenReturn("openid-5");
        when(oAuth2User.getAttributes()).thenReturn(Map.of());
        when(oAuth2User.getAuthorities()).thenReturn(List.of());

        User registered = new User();
        registered.setUsername("github#heidi-5");
        registered.setCode(UUID.randomUUID());

        when(securityManager.loadByOauth2("github", "openid-5")).thenReturn(Mono.empty());
        when(securityManager.registerOrModifyUser(any(UserReq.class))).thenReturn(Mono.just(registered));

        StepVerifier.create(service.loadLocalUser("github", oAuth2User))
                .assertNext(result -> {
                    assertThat(result).isInstanceOf(SecurityDetails.class);
                    assertThat(result.getAttributes()).containsEntry("username", "github#heidi-5");
                })
                .verifyComplete();

        verify(securityManager).loadByOauth2("github", "openid-5");
        verify(securityManager).registerOrModifyUser(any(UserReq.class));
    }
}
