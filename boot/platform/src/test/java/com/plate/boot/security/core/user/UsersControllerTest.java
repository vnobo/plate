package com.plate.boot.security.core.user;

import com.plate.boot.security.SecurityDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UsersController}.
 */
@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private UsersController controller;

    private static SecurityDetails securityDetails() {
        return new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
    }

    private static UserRes userRes(String username) {
        UserRes res = new UserRes();
        res.setUsername(username);
        return res;
    }

    @Test
    void searchStampsSecurityCodeAndReturnsFlux() {
        UserReq request = new UserReq();
        Pageable pageable = PageRequest.of(0, 20);
        UserRes res = userRes("admin");

        when(usersService.search(any(UserReq.class), any(Pageable.class))).thenReturn(Flux.just(res));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityDetails());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(controller.search(request, pageable)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNext(res)
                .verifyComplete();

        verify(usersService).search(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void searchCompletesEmptyWithoutSecurityContext() {
        UserReq request = new UserReq();
        Pageable pageable = PageRequest.of(0, 20);

        StepVerifier.create(controller.search(request, pageable)).verifyComplete();

        verify(usersService, never()).search(any(UserReq.class), any(Pageable.class));
    }

    @Test
    void pageStampsSecurityCodeAndWrapsInPagedModel() {
        UserReq request = new UserReq();
        Pageable pageable = PageRequest.of(0, 20);
        UserRes res = userRes("admin");
        Page<UserRes> page = new PageImpl<>(List.of(res), pageable, 1);

        when(usersService.page(any(UserReq.class), any(Pageable.class))).thenReturn(Mono.just(page));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityDetails());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(controller.page(request, pageable)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .assertNext(paged -> {
                    assertThat(paged).isInstanceOf(PagedModel.class);
                    assertThat(paged.getContent()).containsExactly(res);
                })
                .verifyComplete();

        verify(usersService).page(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void addRejectsNonNullCode() {
        UserReq request = new UserReq();
        request.setCode(UUID.randomUUID());

        assertThatThrownBy(() -> controller.add(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code must be null");

        verify(usersService, never()).add(any(UserReq.class));
    }

    @Test
    void addMapsUserToUserRes() {
        UserReq request = new UserReq();
        request.setUsername("admin");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        when(usersService.add(request)).thenReturn(Mono.just(user));

        StepVerifier.create(controller.add(request))
                .assertNext(res -> {
                    assertThat(res).isInstanceOf(UserRes.class);
                    assertThat(res.getUsername()).isEqualTo("admin");
                })
                .verifyComplete();

        verify(usersService).add(request);
    }

    @Test
    void modifyRejectsNullCode() {
        UserReq request = new UserReq();

        assertThatThrownBy(() -> controller.modify(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code must not be null");

        verify(usersService, never()).modify(any(UserReq.class));
    }

    @Test
    void modifyMapsUserToUserRes() {
        UserReq request = new UserReq();
        request.setCode(UUID.randomUUID());
        request.setUsername("admin");

        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        when(usersService.modify(request)).thenReturn(Mono.just(user));

        StepVerifier.create(controller.modify(request))
                .assertNext(res -> {
                    assertThat(res).isInstanceOf(UserRes.class);
                    assertThat(res.getUsername()).isEqualTo("admin");
                })
                .verifyComplete();

        verify(usersService).modify(request);
    }

    @Test
    void deleteRejectsNullCode() {
        UserReq request = new UserReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("code must not be null");

        verify(usersService, never()).delete(any(UserReq.class));
    }

    @Test
    void deleteDelegatesWhenCodePresent() {
        UserReq request = new UserReq();
        request.setCode(UUID.randomUUID());

        when(usersService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(usersService).delete(request);
    }
}
