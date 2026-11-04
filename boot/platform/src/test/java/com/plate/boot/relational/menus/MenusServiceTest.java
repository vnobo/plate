package com.plate.boot.relational.menus;

import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MenusService}.
 * <p>
 * The {@code cache} field inherited from {@link com.plate.boot.commons.base.AbstractCache} is
 * initialised via {@link MenusService#afterPropertiesSet()} in {@link #setUp()}. The static
 * {@link ContextUtils#APPLICATION_EVENT_PUBLISHER} is replaced with a fresh mock per test so that
 * event publishing can be verified without a Spring container.
 */
@ExtendWith(MockitoExtension.class)
class MenusServiceTest {

    @Mock
    private MenusRepository menusRepository;

    @InjectMocks
    private MenusService service;

    private ApplicationEventPublisher eventPublisher;

    private static JsonMapper savedMapper;
    private static DataSize savedMaxInMemory;

    @BeforeAll
    static void setUpStatics() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
        savedMaxInMemory = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(10);
    }

    @AfterAll
    static void tearDownStatics() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMaxInMemory;
    }

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        ContextUtils.APPLICATION_EVENT_PUBLISHER = eventPublisher;
        service.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        ContextUtils.APPLICATION_EVENT_PUBLISHER = null;
    }

    @Test
    void searchReturnsMenusFromDatabase() {
        Menu menu = new Menu();
        menu.setId(1);
        MenuReq request = new MenuReq();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(any(Query.class), ArgumentMatchers.<Class<Menu>>any()))
                    .thenReturn(Flux.just(menu));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(menu)
                    .verifyComplete();
        }
    }

    @Test
    void pageCombinesSearchResultsAndCount() {
        Menu first = new Menu();
        first.setId(1);
        Menu second = new Menu();
        second.setId(2);
        MenuReq request = new MenuReq();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(any(Query.class), ArgumentMatchers.<Class<Menu>>any()))
                    .thenReturn(Flux.just(first, second));
            db.when(() -> DatabaseUtils.count(any(Query.class), any()))
                    .thenReturn(Mono.just(2L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> assertThat(page.getContent()).containsExactly(first, second))
                    .verifyComplete();
        }
    }

    @Test
    void addRejectsMenuWhenAuthorityAlreadyExists() {
        Menu existing = new Menu();
        existing.setId(1);
        MenuReq request = MenuReq.of(UUID.randomUUID(), "dashboard");

        when(menusRepository.findByTenantCodeAndAuthority(any(), any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.add(request))
                .expectError(RestServerException.class)
                .verify();

        verify(menusRepository, never()).save(any(Menu.class));
    }

    @Test
    void addInsertsNewMenuWhenAuthorityNotExists() {
        Menu saved = new Menu();
        saved.setId(1);
        MenuReq request = MenuReq.of(UUID.randomUUID(), "dashboard");

        when(menusRepository.findByTenantCodeAndAuthority(any(), any())).thenReturn(Mono.empty());
        when(menusRepository.findByCode(any())).thenReturn(Mono.empty());
        when(menusRepository.save(any(Menu.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.add(request)).expectNext(saved).verifyComplete();

        verify(menusRepository).save(any(Menu.class));
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void modifyRejectsMenuWhenCodeNotFound() {
        MenuReq request = MenuReq.of(UUID.randomUUID(), "dashboard");
        request.setCode(UUID.randomUUID());

        when(menusRepository.findByCode(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.modify(request))
                .expectError(RestServerException.class)
                .verify();

        verify(menusRepository, never()).save(any(Menu.class));
    }

    @Test
    void modifyUpdatesExistingMenu() {
        UUID code = UUID.randomUUID();
        Menu existing = new Menu();
        existing.setId(1);
        existing.setCode(code);
        existing.setAuthority("ROLE_DASHBOARD");

        Menu old = new Menu();
        old.setId(1);
        old.setCode(code);
        old.setCreatedAt(LocalDateTime.now());

        MenuReq request = MenuReq.of(UUID.randomUUID(), "dashboard");
        request.setCode(code);

        when(menusRepository.findByCode(code)).thenReturn(Mono.just(existing));
        when(menusRepository.findById(1)).thenReturn(Mono.just(old));
        when(menusRepository.save(any(Menu.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(service.modify(request)).expectNext(existing).verifyComplete();

        verify(menusRepository).save(any(Menu.class));
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void operateUpdatesMenuFoundByTenantAndAuthority() {
        Menu existing = new Menu();
        existing.setId(2);
        existing.setCode(UUID.randomUUID());
        existing.setAuthority("ROLE_REPORTS");

        Menu old = new Menu();
        old.setId(2);
        old.setCode(existing.getCode());
        old.setCreatedAt(LocalDateTime.now());

        MenuReq request = MenuReq.of(UUID.randomUUID(), "reports");

        when(menusRepository.findByCode(any())).thenReturn(Mono.empty());
        when(menusRepository.findByTenantCodeAndAuthority(any(), any())).thenReturn(Mono.just(existing));
        when(menusRepository.findById(2)).thenReturn(Mono.just(old));
        when(menusRepository.save(any(Menu.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(service.operate(request)).expectNext(existing).verifyComplete();

        verify(menusRepository).save(any(Menu.class));
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void saveInsertsNewMenuAndPublishesInsertEvent() {
        Menu menu = new Menu();
        menu.setName("dashboard");
        Menu saved = new Menu();
        saved.setId(5);

        when(menusRepository.save(any(Menu.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.save(menu)).expectNext(saved).verifyComplete();

        verify(menusRepository).save(menu);
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void saveUpdatesExistingMenuAndPublishesUpdateEvent() {
        Menu menu = new Menu();
        menu.setId(1);
        menu.setName("dashboard");

        Menu old = new Menu();
        old.setId(1);
        old.setCode(UUID.randomUUID());
        old.setCreatedAt(LocalDateTime.now());

        when(menusRepository.findById(1)).thenReturn(Mono.just(old));
        when(menusRepository.save(any(Menu.class))).thenReturn(Mono.just(menu));

        StepVerifier.create(service.save(menu)).expectNext(menu).verifyComplete();

        verify(menusRepository).findById(1);
        verify(menusRepository).save(menu);
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void deleteRemovesMenuAndPublishesDeleteEvent() {
        Menu menu = new Menu();
        menu.setId(1);
        menu.setCode(UUID.randomUUID());
        MenuReq request = new MenuReq();
        request.setCode(menu.getCode());

        when(menusRepository.findByCode(menu.getCode())).thenReturn(Mono.just(menu));
        when(menusRepository.delete(any(Menu.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(menusRepository).delete(menu);
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void deleteCompletesWhenMenuMissing() {
        MenuReq request = new MenuReq();
        request.setCode(UUID.randomUUID());

        when(menusRepository.findByCode(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(menusRepository, never()).delete(any(Menu.class));
        verify(eventPublisher, never()).publishEvent(any(ApplicationEvent.class));
    }
}
