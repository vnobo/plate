package com.plate.boot.security.core.user;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UsersService}.
 * <p>
 * Pure Mockito + StepVerifier tests; no Spring container or database is started. The
 * {@code search}/{@code page} methods delegate to the static {@link DatabaseUtils} helpers, which are
 * mocked with {@link MockedStatic} so the query-building path can be exercised without a live R2DBC
 * client.
 */
@ExtendWith(MockitoExtension.class)
class UsersServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UsersService service;

    private ApplicationEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = mock(ApplicationEventPublisher.class);
        ContextUtils.APPLICATION_EVENT_PUBLISHER = publisher;
    }

    @AfterEach
    void tearDown() {
        ContextUtils.APPLICATION_EVENT_PUBLISHER = null;
    }

    @Test
    void addThrowsWhenUsernameAlreadyExists() {
        UserReq request = new UserReq();
        request.setUsername("admin");

        when(usersRepository.existsByUsernameIgnoreCase("admin")).thenReturn(Mono.just(true));

        StepVerifier.create(service.add(request))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(RestServerException.class);
                    assertThat(((RestServerException) err).getReason()).isEqualTo("User already exists");
                    assertThat(err.getCause()).isInstanceOf(UsernameNotFoundException.class);
                })
                .verify();

        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void addCreatesNewUserWhenUsernameAvailable() {
        UserReq request = new UserReq();
        request.setUsername("newuser");
        request.setPassword("Passw0rd");

        when(usersRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(Mono.just(false));
        when(passwordEncoder.upgradeEncoding("Passw0rd")).thenReturn(false);
        when(usersRepository.findByCode(any())).thenReturn(Mono.empty());
        when(usersRepository.findByUsername("newuser")).thenReturn(Mono.empty());

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("newuser");
        when(usersRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.add(request))
                .assertNext(user -> assertThat(user.getUsername()).isEqualTo("newuser"))
                .verifyComplete();

        verify(passwordEncoder).upgradeEncoding("Passw0rd");
        verify(passwordEncoder, never()).encode(anyString());
        verify(usersRepository).save(any(User.class));
        verify(publisher).publishEvent(any(UserEvent.class));
    }

    @Test
    void addUpgradesPasswordWhenEncodingNeedsUpgrade() {
        UserReq request = new UserReq();
        request.setUsername("newuser");
        request.setPassword("Passw0rd");

        when(usersRepository.existsByUsernameIgnoreCase("newuser")).thenReturn(Mono.just(false));
        when(passwordEncoder.upgradeEncoding("Passw0rd")).thenReturn(true);
        when(passwordEncoder.encode("Passw0rd")).thenReturn("{bcrypt}encoded");
        when(usersRepository.findByCode(any())).thenReturn(Mono.empty());
        when(usersRepository.findByUsername("newuser")).thenReturn(Mono.empty());

        User saved = new User();
        saved.setUsername("newuser");
        when(usersRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        StepVerifier.create(service.add(request)).expectNextCount(1).verifyComplete();

        verify(passwordEncoder).encode("Passw0rd");
        verify(usersRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("{bcrypt}encoded");
    }

    @Test
    void modifyThrowsWhenUserNotFound() {
        UserReq request = new UserReq();
        request.setUsername("ghost");

        when(usersRepository.findByUsername("ghost")).thenReturn(Mono.empty());

        StepVerifier.create(service.modify(request))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(RestServerException.class);
                    assertThat(((RestServerException) err).getReason()).isEqualTo("User not found");
                })
                .verify();

        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void modifyUpdatesExistingUser() {
        UUID code = UUID.randomUUID();
        User existing = new User();
        existing.setId(1L);
        existing.setCode(code);
        existing.setUsername("admin");

        UserReq request = new UserReq();
        request.setUsername("admin");

        when(usersRepository.findByUsername("admin")).thenReturn(Mono.just(existing));
        when(usersRepository.findByCode(code)).thenReturn(Mono.just(existing));

        User old = new User();
        old.setId(1L);
        old.setCode(code);
        old.setPassword("oldpwd");
        old.setAccountExpired(true);
        old.setAccountLocked(true);
        old.setCredentialsExpired(true);
        when(usersRepository.findById(1L)).thenReturn(Mono.just(old));

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("admin");
        when(usersRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.modify(request))
                .assertNext(user -> assertThat(user.getUsername()).isEqualTo("admin"))
                .verifyComplete();

        verify(usersRepository).findByUsername("admin");
        verify(usersRepository).findByCode(code);
        verify(usersRepository).findById(1L);
        verify(usersRepository).save(any(User.class));
        verify(publisher).publishEvent(any(UserEvent.class));
    }

    @Test
    void operateFindsByUsernameWhenCodeMissing() {
        UUID code = UUID.randomUUID();
        UserReq request = new UserReq();
        request.setCode(code);
        request.setUsername("admin");

        when(usersRepository.findByCode(code)).thenReturn(Mono.empty());

        User existing = new User();
        existing.setId(2L);
        existing.setCode(code);
        existing.setUsername("admin");
        when(usersRepository.findByUsername("admin")).thenReturn(Mono.just(existing));

        User old = new User();
        old.setId(2L);
        old.setPassword("pwd");
        when(usersRepository.findById(2L)).thenReturn(Mono.just(old));

        User saved = new User();
        saved.setId(2L);
        saved.setUsername("admin");
        when(usersRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request))
                .assertNext(user -> assertThat(user.getId()).isEqualTo(2L))
                .verifyComplete();

        verify(usersRepository).findByUsername("admin");
        verify(usersRepository).save(any(User.class));
    }

    @Test
    void saveInsertsNewUser() {
        User user = new User();
        user.setUsername("fresh");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("fresh");
        when(usersRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.save(user))
                .assertNext(res -> assertThat(res.getId()).isEqualTo(1L))
                .verifyComplete();

        verify(usersRepository).save(any(User.class));
        verify(publisher).publishEvent(any(UserEvent.class));
    }

    @Test
    void saveThrowsWhenExistingUserNotFound() {
        User user = new User();
        user.setId(9L);

        when(usersRepository.findById(9L)).thenReturn(Mono.empty());

        StepVerifier.create(service.save(user))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(RestServerException.class);
                    assertThat(((RestServerException) err).getReason()).isEqualTo("Id must not be null!");
                    assertThat(err.getCause()).isInstanceOf(IllegalArgumentException.class);
                })
                .verify();

        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void saveThrowsWhenExistingUserHasNullId() {
        User user = mock(User.class);
        when(user.isNew()).thenReturn(false);
        when(user.getId()).thenReturn(null);

        StepVerifier.create(service.save(user))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(RestServerException.class);
                    assertThat(((RestServerException) err).getReason()).isEqualTo("Id must not be null!");
                    assertThat(err.getCause()).isInstanceOf(IllegalArgumentException.class);
                })
                .verify();

        verify(usersRepository, never()).save(any(User.class));
    }

    @Test
    void saveMergesPasswordAndAccountStateWhenUpdating() {
        User user = new User();
        user.setId(5L);
        user.setUsername("admin");
        user.setPassword("newpwd");

        User old = new User();
        old.setId(5L);
        old.setPassword("oldpwd");
        old.setAccountExpired(true);
        old.setAccountLocked(true);
        old.setCredentialsExpired(true);
        when(usersRepository.findById(5L)).thenReturn(Mono.just(old));

        User saved = new User();
        saved.setId(5L);
        when(usersRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        StepVerifier.create(service.save(user)).expectNextCount(1).verifyComplete();

        verify(usersRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("oldpwd");
        assertThat(captor.getValue().getAccountExpired()).isTrue();
        assertThat(captor.getValue().getAccountLocked()).isTrue();
        assertThat(captor.getValue().getCredentialsExpired()).isTrue();
        verify(publisher).publishEvent(any(UserEvent.class));
    }

    @Test
    void deleteDeletesExistingUserAndPublishesEvent() {
        UUID code = UUID.randomUUID();
        UserReq request = new UserReq();
        request.setCode(code);

        User user = new User();
        user.setCode(code);
        when(usersRepository.findByCode(code)).thenReturn(Mono.just(user));
        when(usersRepository.delete(user)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(usersRepository).delete(user);
        verify(publisher).publishEvent(any(UserEvent.class));
    }

    @Test
    void deleteCompletesEmptyWhenUserNotFound() {
        UUID code = UUID.randomUUID();
        UserReq request = new UserReq();
        request.setCode(code);

        when(usersRepository.findByCode(code)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(usersRepository, never()).delete(any(User.class));
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void searchReturnsMappedResults() {
        UserReq request = new UserReq();
        Pageable pageable = PageRequest.of(0, 20);
        UserRes res = new UserRes();
        res.setUsername("admin");

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), any(), eq(UserRes.class)))
                    .thenReturn(Flux.just(res));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(res)
                    .verifyComplete();
        }
    }

    @Test
    void pageZipsSearchAndCount() {
        UserReq request = new UserReq();
        Pageable pageable = PageRequest.of(0, 20);
        UserRes res = new UserRes();
        res.setUsername("admin");

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), any(), eq(UserRes.class)))
                    .thenReturn(Flux.just(res));
            db.when(() -> DatabaseUtils.count(anyString(), any()))
                    .thenReturn(Mono.just(100L));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> {
                        assertThat(page.getTotalElements()).isEqualTo(100L);
                        assertThat(page.getContent()).containsExactly(res);
                    })
                    .verifyComplete();
        }
    }
}
