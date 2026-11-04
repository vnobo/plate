package com.plate.boot.security.core.user.authority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserAuthoritiesController}.
 */
@ExtendWith(MockitoExtension.class)
class UserAuthoritiesControllerTest {

    @Mock
    private UserAuthoritiesService authoritiesService;

    @InjectMocks
    private UserAuthoritiesController controller;

    private static UserAuthority authority() {
        UserAuthority authority = new UserAuthority();
        authority.setAuthority("ROLE_USER");
        return authority;
    }

    @Test
    void searchDelegatesToService() {
        UserAuthorityReq request = new UserAuthorityReq();
        UserAuthority authority = authority();
        when(authoritiesService.search(request)).thenReturn(Flux.just(authority));

        StepVerifier.create(controller.search(request))
                .expectNext(authority)
                .verifyComplete();

        verify(authoritiesService).search(request);
    }

    @Test
    void saveDelegatesToService() {
        UserAuthorityReq request = new UserAuthorityReq();
        UserAuthority authority = authority();
        when(authoritiesService.operate(request)).thenReturn(Mono.just(authority));

        StepVerifier.create(controller.save(request))
                .expectNext(authority)
                .verifyComplete();

        verify(authoritiesService).operate(request);
    }

    @Test
    void saveBatchEmitsProgressEvents() {
        UserAuthorityReq first = new UserAuthorityReq();
        UserAuthorityReq second = new UserAuthorityReq();
        when(authoritiesService.operate(any(UserAuthorityReq.class))).thenReturn(Mono.just(authority()));

        StepVerifier.create(controller.saveBatch(Flux.just(first, second)))
                .assertNext(event -> assertThat(event.getProcessed()).isZero())
                .assertNext(event -> {
                    assertThat(event.getProcessed()).isEqualTo(1L);
                    assertThat(event.getIsOk()).isTrue();
                })
                .assertNext(event -> {
                    assertThat(event.getProcessed()).isEqualTo(2L);
                    assertThat(event.getIsOk()).isTrue();
                })
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(100L))
                .verifyComplete();

        verify(authoritiesService, org.mockito.Mockito.times(2)).operate(any(UserAuthorityReq.class));
    }

    @Test
    void saveBatchReportsFailedItem() {
        UserAuthorityReq request = new UserAuthorityReq();
        when(authoritiesService.operate(any(UserAuthorityReq.class)))
                .thenReturn(Mono.error(new RuntimeException("boom")));

        StepVerifier.create(controller.saveBatch(Flux.just(request)))
                .assertNext(event -> assertThat(event.getProcessed()).isZero())
                .assertNext(event -> {
                    assertThat(event.getProcessed()).isEqualTo(1L);
                    assertThat(event.getIsOk()).isFalse();
                })
                .assertNext(event -> assertThat(event.getProcessed()).isEqualTo(100L))
                .verifyComplete();
    }

    @Test
    void deleteRejectsNullId() {
        UserAuthorityReq request = new UserAuthorityReq();

        assertThatThrownBy(() -> controller.delete(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID must not be null");

        verify(authoritiesService, never()).delete(any(UserAuthorityReq.class));
    }

    @Test
    void deleteDelegatesWhenIdPresent() {
        UserAuthorityReq request = new UserAuthorityReq();
        request.setId(1);

        when(authoritiesService.delete(request)).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(request)).verifyComplete();

        verify(authoritiesService).delete(request);
    }
}
