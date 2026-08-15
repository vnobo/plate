package com.plate.boot.security.core.group.authority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupAuthoritiesController}.
 */
@ExtendWith(MockitoExtension.class)
class GroupAuthoritiesControllerTest {

    @Mock
    private GroupAuthoritiesService authoritiesService;

    @InjectMocks
    private GroupAuthoritiesController controller;

    private static GroupAuthority authority() {
        GroupAuthority authority = new GroupAuthority();
        authority.setAuthority("ROLE_USER");
        return authority;
    }

    @Test
    void searchDelegatesToService() {
        GroupAuthorityReq request = new GroupAuthorityReq();
        Pageable pageable = PageRequest.of(0, 20);
        GroupAuthority authority = authority();
        when(authoritiesService.search(request, pageable)).thenReturn(Flux.just(authority));

        StepVerifier.create(controller.search(request, pageable))
                .expectNext(authority)
                .verifyComplete();

        verify(authoritiesService).search(request, pageable);
    }

    @Test
    void saveDelegatesToService() {
        GroupAuthorityReq request = new GroupAuthorityReq();
        GroupAuthority authority = authority();
        when(authoritiesService.operate(request)).thenReturn(Mono.just(authority));

        StepVerifier.create(controller.save(request))
                .expectNext(authority)
                .verifyComplete();

        verify(authoritiesService).operate(request);
    }

    @Test
    void saveBatchEmitsProgressEvents() {
        GroupAuthorityReq first = new GroupAuthorityReq();
        GroupAuthorityReq second = new GroupAuthorityReq();
        when(authoritiesService.operate(any(GroupAuthorityReq.class))).thenReturn(Mono.just(authority()));

        StepVerifier.create(controller.saveBatch(Flux.just(first, second)))
                .assertNext(event -> assertThat(event.getProcessed()).isZero())
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(1L))
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(2L))
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(100L))
                .verifyComplete();

        verify(authoritiesService, times(2)).operate(any(GroupAuthorityReq.class));
    }

    @Test
    void deleteRejectsNullId() {
        GroupAuthorityReq request = new GroupAuthorityReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be null");

        verify(authoritiesService, never()).delete(any(GroupAuthorityReq.class));
    }

    @Test
    void deleteDelegatesWhenIdPresent() {
        GroupAuthorityReq request = new GroupAuthorityReq();
        request.setId(1);

        when(authoritiesService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(authoritiesService).delete(request);
    }
}
