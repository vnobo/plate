package com.plate.boot.security.core.tenant.member;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TenantMembersController}.
 */
@ExtendWith(MockitoExtension.class)
class TenantMembersControllerTest {

    @Mock
    private TenantMembersService tenantMembersService;

    @InjectMocks
    private TenantMembersController controller;

    private static SecurityDetails securityDetails() {
        return new SecurityDetails(List.of(), Map.of("username", "admin"), "username");
    }

    private static TenantMemberRes memberRes() {
        TenantMemberRes res = new TenantMemberRes();
        res.setName("acme");
        return res;
    }

    private static TenantMember member() {
        TenantMember member = new TenantMember();
        member.setUserCode(java.util.UUID.randomUUID());
        member.setEnabled(true);
        return member;
    }

    @Test
    void searchStampsSecurityCodeAndReturnsFlux() {
        TenantMemberReq request = new TenantMemberReq();
        Pageable pageable = PageRequest.of(0, 20);
        TenantMemberRes res = memberRes();

        when(tenantMembersService.search(any(TenantMemberReq.class), any(Pageable.class))).thenReturn(Flux.just(res));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(securityDetails());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(controller.search(request, pageable)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext))))
                .expectNext(res)
                .verifyComplete();

        verify(tenantMembersService).search(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void pageStampsSecurityCodeAndWrapsInPagedModel() {
        TenantMemberReq request = new TenantMemberReq();
        Pageable pageable = PageRequest.of(0, 20);
        TenantMemberRes res = memberRes();
        Page<TenantMemberRes> page = new PageImpl<>(List.of(res), pageable, 1);

        when(tenantMembersService.page(any(TenantMemberReq.class), any(Pageable.class))).thenReturn(Mono.just(page));

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

        verify(tenantMembersService).page(request, pageable);
        assertThat(request.getSecurityCode()).isEqualTo(DEFAULT_UUID_CODE);
    }

    @Test
    void saveDelegatesToService() {
        TenantMemberReq request = new TenantMemberReq();
        TenantMember member = member();
        when(tenantMembersService.operate(request)).thenReturn(Mono.just(member));

        StepVerifier.create(controller.save(request))
                .expectNext(member)
                .verifyComplete();

        verify(tenantMembersService).operate(request);
    }

    @Test
    void saveBatchEmitsProgressEvents() {
        TenantMemberReq first = new TenantMemberReq();
        TenantMemberReq second = new TenantMemberReq();
        when(tenantMembersService.operate(any(TenantMemberReq.class))).thenReturn(Mono.just(member()));

        StepVerifier.create(controller.saveBatch(Flux.just(first, second)))
                .assertNext(event -> assertThat(event.getProcessed()).isZero())
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(1L))
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(2L))
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(100L))
                .verifyComplete();

        verify(tenantMembersService, times(2)).operate(any(TenantMemberReq.class));
    }

    @Test
    void deleteRejectsNullId() {
        TenantMemberReq request = new TenantMemberReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be null");

        verify(tenantMembersService, never()).delete(any(TenantMemberReq.class));
    }

    @Test
    void deleteDelegatesWhenIdPresent() {
        TenantMemberReq request = new TenantMemberReq();
        request.setId(1L);

        when(tenantMembersService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(tenantMembersService).delete(request);
    }
}
