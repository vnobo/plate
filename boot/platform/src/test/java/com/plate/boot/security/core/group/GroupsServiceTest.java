package com.plate.boot.security.core.group;

import com.plate.boot.commons.base.AbstractEvent.Kind;
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
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link GroupsService}, exercising the create/update/delete/save/operate
 * flow and the group event publishing without a Spring container or database connection.
 */
@ExtendWith(MockitoExtension.class)
class GroupsServiceTest {

    @Mock
    private GroupsRepository groupsRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GroupsService service;

    private CacheManager savedCacheManager;

    @BeforeEach
    void setUp() {
        savedCacheManager = ContextUtils.CACHE_MANAGER;
        ContextUtils.CACHE_MANAGER = null;
        service.afterPropertiesSet();
        ContextUtils.APPLICATION_EVENT_PUBLISHER = eventPublisher;
    }

    @AfterEach
    void tearDown() {
        ContextUtils.APPLICATION_EVENT_PUBLISHER = null;
        ContextUtils.CACHE_MANAGER = savedCacheManager;
    }

    @Test
    void operateCreatesGroupWhenCodeNotFound() {
        UUID code = UUID.randomUUID();
        GroupReq request = new GroupReq();
        request.setCode(code);
        request.setName("Developers");

        when(groupsRepository.findByCode(code)).thenReturn(Mono.empty());
        when(groupsRepository.save(any(Group.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.operate(request))
                .assertNext(group -> assertThat(group.getName()).isEqualTo("Developers"))
                .verifyComplete();

        ArgumentCaptor<Group> savedCaptor = ArgumentCaptor.forClass(Group.class);
        verify(groupsRepository).save(savedCaptor.capture());
        assertThat(savedCaptor.getValue().getName()).isEqualTo("Developers");
        assertThat(savedCaptor.getValue().getCode()).isEqualTo(code);

        ArgumentCaptor<GroupEvent> eventCaptor = ArgumentCaptor.forClass(GroupEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getKind()).isEqualTo(Kind.INSERT);
        assertThat(eventCaptor.getValue().getEntity()).isSameAs(savedCaptor.getValue());
    }

    @Test
    void operateUpdatesGroupWhenCodeFound() {
        UUID code = UUID.randomUUID();
        Group existing = new Group();
        existing.setId(1);
        existing.setCode(code);
        existing.setName("Old");

        Group old = new Group();
        old.setId(1);
        old.setCode(code);
        old.setName("Old");
        old.setCreatedAt(LocalDateTime.now());

        GroupReq request = new GroupReq();
        request.setCode(code);
        request.setName("New Name");

        when(groupsRepository.findByCode(code)).thenReturn(Mono.just(existing));
        when(groupsRepository.findById(1)).thenReturn(Mono.just(old));
        when(groupsRepository.save(any(Group.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.operate(request))
                .assertNext(group -> assertThat(group.getName()).isEqualTo("New Name"))
                .verifyComplete();

        verify(groupsRepository).findById(1);
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getCreatedAt()).isEqualTo(old.getCreatedAt());
        assertThat(existing.getCode()).isEqualTo(code);

        ArgumentCaptor<GroupEvent> eventCaptor = ArgumentCaptor.forClass(GroupEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getKind()).isEqualTo(Kind.UPDATE);
        assertThat(eventCaptor.getValue().getEntity()).isSameAs(existing);
    }

    @Test
    void saveNewGroupInsertsAndPublishesInsertEvent() {
        Group group = new Group();
        group.setName("Fresh");

        when(groupsRepository.save(any(Group.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.save(group))
                .assertNext(saved -> assertThat(saved).isSameAs(group))
                .verifyComplete();

        verify(groupsRepository).save(group);

        ArgumentCaptor<GroupEvent> eventCaptor = ArgumentCaptor.forClass(GroupEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getKind()).isEqualTo(Kind.INSERT);
        assertThat(eventCaptor.getValue().getEntity()).isSameAs(group);
    }

    @Test
    void saveExistingGroupUpdatesAndPublishesUpdateEvent() {
        Group group = new Group();
        group.setId(7);
        group.setName("Changed");

        Group old = new Group();
        old.setId(7);
        old.setCode(UUID.randomUUID());
        old.setCreatedAt(LocalDateTime.now());

        when(groupsRepository.findById(7)).thenReturn(Mono.just(old));
        when(groupsRepository.save(any(Group.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(service.save(group))
                .assertNext(saved -> assertThat(saved).isSameAs(group))
                .verifyComplete();

        verify(groupsRepository).findById(7);
        assertThat(group.getCreatedAt()).isEqualTo(old.getCreatedAt());
        assertThat(group.getCode()).isEqualTo(old.getCode());

        ArgumentCaptor<GroupEvent> eventCaptor = ArgumentCaptor.forClass(GroupEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getKind()).isEqualTo(Kind.UPDATE);
        assertThat(eventCaptor.getValue().getEntity()).isSameAs(group);
    }

    @Test
    void saveExistingGroupCompletesEmptyWhenOldRecordMissing() {
        Group group = new Group();
        group.setId(99);

        when(groupsRepository.findById(99)).thenReturn(Mono.empty());

        StepVerifier.create(service.save(group)).verifyComplete();

        verify(groupsRepository).findById(99);
        verify(groupsRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deletePublishesDeleteEventAndDeletes() {
        UUID code = UUID.randomUUID();
        Group group = new Group();
        group.setId(5);
        group.setCode(code);

        GroupReq request = new GroupReq();
        request.setCode(code);

        when(groupsRepository.findByCode(code)).thenReturn(Mono.just(group));
        when(groupsRepository.delete(group)).thenReturn(Mono.empty());

        StepVerifier.withVirtualTime(() -> service.delete(request))
                .thenAwait(Duration.ofSeconds(2))
                .verifyComplete();

        verify(groupsRepository).delete(group);

        ArgumentCaptor<GroupEvent> eventCaptor = ArgumentCaptor.forClass(GroupEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getKind()).isEqualTo(Kind.DELETE);
        assertThat(eventCaptor.getValue().getEntity()).isSameAs(group);
    }

    @Test
    void deleteCompletesEmptyWhenGroupNotFound() {
        UUID code = UUID.randomUUID();
        GroupReq request = new GroupReq();
        request.setCode(code);

        when(groupsRepository.findByCode(code)).thenReturn(Mono.empty());

        StepVerifier.withVirtualTime(() -> service.delete(request))
                .thenAwait(Duration.ofSeconds(2))
                .verifyComplete();

        verify(groupsRepository, never()).delete(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void searchReturnsQueriedGroups() {
        Group group = new Group();
        group.setId(1);
        group.setCode(UUID.randomUUID());

        GroupReq request = new GroupReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(Group.class)))
                    .thenReturn(Flux.just(group));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(group)
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void pageReturnsPagedGroups() {
        Group group = new Group();
        group.setId(1);
        group.setCode(UUID.randomUUID());

        GroupReq request = new GroupReq();
        Pageable pageable = PageRequest.of(0, 10);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(Group.class)))
                    .thenReturn(Flux.just(group));
            db.when(() -> DatabaseUtils.count(anyString(), anyMap())).thenReturn(Mono.just(3L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> {
                        assertThat(page.getContent()).containsExactly(group);
                        assertThat(page.getSize()).isEqualTo(pageable.getPageSize());
                        assertThat(page.getNumber()).isEqualTo(pageable.getPageNumber());
                        // countWithCache returns 0 on cache-miss, and PageImpl re-derives the
                        // total as offset + content.size() for this partial last page.
                        assertThat(page.getTotalElements()).isEqualTo(1L);
                    })
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }
}
