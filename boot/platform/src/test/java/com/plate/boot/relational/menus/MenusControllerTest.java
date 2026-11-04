package com.plate.boot.relational.menus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static com.plate.boot.commons.utils.ContextUtils.RULE_ADMINISTRATORS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MenusController}.
 */
@ExtendWith(MockitoExtension.class)
class MenusControllerTest {

    @Mock
    private MenusService menusService;

    @InjectMocks
    private MenusController controller;

    private static Menu menu(String authority) {
        Menu menu = new Menu();
        menu.setAuthority(authority);
        return menu;
    }

    @Test
    void searchDistinctsMenusByAuthority() {
        MenuReq request = new MenuReq();
        Pageable pageable = PageRequest.of(0, 10);

        Menu first = menu("ROLE_DASHBOARD");
        Menu second = menu("ROLE_DASHBOARD");
        when(menusService.search(request, pageable)).thenReturn(Flux.just(first, second));

        StepVerifier.create(controller.search(request, pageable))
                .expectNext(first)
                .verifyComplete();
    }

    @Test
    void pageWrapsServicePageInPagedModel() {
        MenuReq request = new MenuReq();
        Pageable pageable = PageRequest.of(0, 10);

        Menu menu = menu("ROLE_DASHBOARD");
        Page<Menu> page = new PageImpl<>(List.of(menu), pageable, 1);
        when(menusService.page(request, pageable)).thenReturn(Mono.just(page));

        StepVerifier.create(controller.page(request, pageable))
                .assertNext(paged -> {
                    assertThat(paged).isInstanceOf(PagedModel.class);
                    assertThat(paged.getContent()).containsExactly(menu);
                    assertThat(paged.getMetadata().totalElements()).isEqualTo(1L);
                })
                .verifyComplete();
    }

    @Test
    void loadLeavesRulesUntouchedForAdministrator() {
        MenuReq request = new MenuReq();
        Menu menu = menu("ROLE_DASHBOARD");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(RULE_ADMINISTRATORS));
        SecurityContextImpl securityContext = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("admin", null, authorities));

        when(menusService.search(any(MenuReq.class), any(Pageable.class))).thenReturn(Flux.just(menu));

        StepVerifier.create(controller.load(request)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNext(menu)
                .verifyComplete();

        ArgumentCaptor<MenuReq> captor = ArgumentCaptor.forClass(MenuReq.class);
        verify(menusService).search(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue().getRules()).isNull();
    }

    @Test
    void loadAppliesNoneAuthorityWhenRulesEmpty() {
        MenuReq request = new MenuReq();
        Menu menu = menu("ROLE_NONE");

        SecurityContextImpl securityContext = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("admin", null, List.of()));

        when(menusService.search(any(MenuReq.class), any(Pageable.class))).thenReturn(Flux.just(menu));

        StepVerifier.create(controller.load(request)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNext(menu)
                .verifyComplete();

        ArgumentCaptor<MenuReq> captor = ArgumentCaptor.forClass(MenuReq.class);
        verify(menusService).search(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue().getRules()).containsExactly("NONE_AUTHORITY");
    }

    @Test
    void loadAppliesUserRulesForNonAdministrator() {
        MenuReq request = new MenuReq();
        Menu menu = menu("ROLE_USER");

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        SecurityContextImpl securityContext = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken("admin", null, authorities));

        when(menusService.search(any(MenuReq.class), any(Pageable.class))).thenReturn(Flux.just(menu));

        StepVerifier.create(controller.load(request)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNext(menu)
                .verifyComplete();

        ArgumentCaptor<MenuReq> captor = ArgumentCaptor.forClass(MenuReq.class);
        verify(menusService).search(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue().getRules()).containsExactly("ROLE_USER");
    }

    @Test
    void saveAddsNewMenuWhenRequestIsNew() {
        MenuReq request = new MenuReq();
        Menu saved = menu("ROLE_DASHBOARD");
        saved.setId(1);

        when(menusService.add(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.save(request))
                .expectNext(saved)
                .verifyComplete();

        verify(menusService).add(request);
        verify(menusService, never()).modify(any(MenuReq.class));
    }

    @Test
    void saveModifiesExistingMenuWhenRequestHasId() {
        MenuReq request = new MenuReq();
        request.setId(2);
        Menu saved = menu("ROLE_DASHBOARD");

        when(menusService.modify(request)).thenReturn(Mono.just(saved));

        StepVerifier.create(controller.save(request))
                .expectNext(saved)
                .verifyComplete();

        verify(menusService).modify(request);
        verify(menusService, never()).add(any(MenuReq.class));
    }

    @Test
    void deleteDelegatesWhenRequestCarriesIdAndCode() {
        MenuReq request = new MenuReq();
        request.setId(1);
        request.setCode(UUID.randomUUID());

        when(menusService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(menusService).delete(request);
    }

    @Test
    void deleteRejectsNewRequest() {
        MenuReq request = new MenuReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Delete [ID] cannot be empty!");

        verify(menusService, never()).delete(any(MenuReq.class));
    }
}
