package com.plate.boot.security.core.user.authority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserAuthoritiesService#delete(UserAuthorityReq)}.
 * <p>
 * Verifies the delete path locates the row by the {@code id} supplied by the controller
 * (previously it mistakenly used {@code code}, producing a silent no-op), and that the
 * repository delete is invoked with the resolved entity.
 */
@ExtendWith(MockitoExtension.class)
class UserAuthoritiesServiceTest {

    @Mock
    private UserAuthoritiesRepository repository;

    @InjectMocks
    private UserAuthoritiesService service;

    @Test
    void deleteShouldLocateByProvidedIdNotByCode() {
        UserAuthority entity = new UserAuthority();
        entity.setId(42);

        UserAuthorityReq request = new UserAuthorityReq();
        request.setId(42);
        // code intentionally left null, mirroring a normal delete request shape

        when(repository.findById(42)).thenReturn(Mono.just(entity));
        when(repository.delete(any(UserAuthority.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(repository).findById(42);
        verify(repository, never()).findByCode(any());
        verify(repository).delete(entity);
    }
}
