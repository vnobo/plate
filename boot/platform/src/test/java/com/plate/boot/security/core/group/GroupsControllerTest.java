package com.plate.boot.security.core.group;

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

import static com.plate.boot.commons.utils.ContextUtils.DEFAULT_UUID_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupsController}.
 */
@ExtendWith(MockitoExtension.class)
class GroupsControllerTest {

    @Mock
    private GroupsService groupsService;

    @InjectMocks
    private GroupsController controller;

    private static SecurityDetails securityDetails() {
        return new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
    }

    private static Group group(String name) {
        Group group = new Group();
        group.setName(name);
        return group;
    }

    @Test
    void searchStampsSecurityCodeAndReturnsFlux() {
        GroupReq request = new GroupReq();
        Pageable pageable = PageRequest.of(0, 20);
        Group group = group("ops");

        when(groupsService.search(any(GroupReq.class), any(Pageable.class))).thenReturn(Flux.just(group));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityDetails());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(controller.search(request, pageable)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNext(group)
                .verifyComplete();

        verify(groupsService).search(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void pageStampsSecurityCodeAndWrapsInPagedModel() {
        GroupReq request = new GroupReq();
        Pageable pageable = PageRequest.of(0, 20);
        Group group = group("ops");
        Page<Group> page = new PageImpl<>(List.of(group), pageable, 1);

        when(groupsService.page(any(GroupReq.class), any(Pageable.class))).thenReturn(Mono.just(page));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityDetails());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(controller.page(request, pageable)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .assertNext(paged -> {
                    assertThat(paged).isInstanceOf(PagedModel.class);
                    assertThat(paged.getContent()).containsExactly(group);
                })
                .verifyComplete();

        verify(groupsService).page(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void addDelegatesToService() {
        GroupReq request = new GroupReq();
        request.setName("ops");

        Group saved = group("ops");
        when(groupsService.operate(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.add(request))
                .expectNext(saved)
                .verifyComplete();

        verify(groupsService).operate(request);
    }

    @Test
    void deleteRejectsNullId() {
        GroupReq request = new GroupReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be null");

        verify(groupsService, never()).delete(any(GroupReq.class));
    }

    @Test
    void deleteDelegatesWhenIdPresent() {
        GroupReq request = new GroupReq();
        request.setId(1);

        when(groupsService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(groupsService).delete(request);
    }
}
