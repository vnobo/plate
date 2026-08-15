package com.plate.boot.relational.dictionaries;

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
 * Unit tests for {@link DictionariesService}.
 * <p>
 * Mirrors the test strategy used for {@code MenusServiceTest}: the inherited cache is
 * initialised per-test, the static event publisher is replaced with a mock, and the
 * {@link DatabaseUtils} query/count helpers are mocked for the search/page paths.
 */
@ExtendWith(MockitoExtension.class)
class DictionariesServiceTest {

    @Mock
    private DictionariesRepository dictionariesRepository;

    @InjectMocks
    private DictionariesService service;

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
    void searchReturnsDictionariesFromDatabase() {
        Dictionary dictionary = new Dictionary();
        dictionary.setId(1L);
        DictionaryReq request = new DictionaryReq();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(any(Query.class), ArgumentMatchers.<Class<Dictionary>>any()))
                    .thenReturn(Flux.just(dictionary));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(dictionary)
                    .verifyComplete();
        }
    }

    @Test
    void pageCombinesSearchResultsAndCount() {
        Dictionary first = new Dictionary();
        first.setId(1L);
        Dictionary second = new Dictionary();
        second.setId(2L);
        DictionaryReq request = new DictionaryReq();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(any(Query.class), ArgumentMatchers.<Class<Dictionary>>any()))
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
    void findByTypeReturnsDictionariesFromRepository() {
        Dictionary first = new Dictionary();
        first.setId(1L);
        Dictionary second = new Dictionary();
        second.setId(2L);
        UUID tenantCode = UUID.randomUUID();

        when(dictionariesRepository.findByTenantCodeAndDictTypeOrderBySortNoAsc(tenantCode, "STATUS"))
                .thenReturn(Flux.just(first, second));

        StepVerifier.create(service.findByType(tenantCode, "STATUS"))
                .expectNext(first, second)
                .verifyComplete();
    }

    @Test
    void findEnabledByTypeReturnsEnabledDictionaries() {
        Dictionary dictionary = new Dictionary();
        dictionary.setId(1L);
        UUID tenantCode = UUID.randomUUID();

        when(dictionariesRepository.findByTenantCodeAndDictTypeAndEnabledOrderBySortNoAsc(tenantCode, "STATUS", true))
                .thenReturn(Flux.just(dictionary));

        StepVerifier.create(service.findEnabledByType(tenantCode, "STATUS"))
                .expectNext(dictionary)
                .verifyComplete();
    }

    @Test
    void findChildrenReturnsChildDictionaries() {
        Dictionary child = new Dictionary();
        child.setId(1L);
        UUID pcode = UUID.randomUUID();

        when(dictionariesRepository.findByPcodeOrderBySortNoAsc(pcode)).thenReturn(Flux.just(child));

        StepVerifier.create(service.findChildren(pcode))
                .expectNext(child)
                .verifyComplete();
    }

    @Test
    void addRejectsDictionaryWhenDuplicateExists() {
        Dictionary existing = new Dictionary();
        existing.setId(1L);
        DictionaryReq request = dictionaryRequest();

        when(dictionariesRepository.findByTenantCodeAndDictTypeAndDictKey(any(), any(), any()))
                .thenReturn(Mono.just(existing));

        StepVerifier.create(service.add(request))
                .expectError(RestServerException.class)
                .verify();

        verify(dictionariesRepository, never()).save(any(Dictionary.class));
    }

    @Test
    void addInsertsNewDictionaryWhenNoDuplicate() {
        Dictionary saved = new Dictionary();
        saved.setId(1L);
        DictionaryReq request = dictionaryRequest();

        when(dictionariesRepository.findByTenantCodeAndDictTypeAndDictKey(any(), any(), any()))
                .thenReturn(Mono.empty());
        when(dictionariesRepository.findByCode(any())).thenReturn(Mono.empty());
        when(dictionariesRepository.save(any(Dictionary.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.add(request)).expectNext(saved).verifyComplete();

        verify(dictionariesRepository).save(any(Dictionary.class));
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void modifyRejectsDictionaryWhenCodeNotFound() {
        DictionaryReq request = dictionaryRequest();
        request.setCode(UUID.randomUUID());

        when(dictionariesRepository.findByCode(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.modify(request))
                .expectError(RestServerException.class)
                .verify();

        verify(dictionariesRepository, never()).save(any(Dictionary.class));
    }

    @Test
    void modifyUpdatesExistingDictionary() {
        UUID code = UUID.randomUUID();
        Dictionary existing = new Dictionary();
        existing.setId(1L);
        existing.setCode(code);

        Dictionary old = new Dictionary();
        old.setId(1L);
        old.setCode(code);
        old.setCreatedAt(LocalDateTime.now());

        DictionaryReq request = dictionaryRequest();
        request.setCode(code);

        when(dictionariesRepository.findByCode(code)).thenReturn(Mono.just(existing));
        when(dictionariesRepository.findById(1L)).thenReturn(Mono.just(old));
        when(dictionariesRepository.save(any(Dictionary.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(service.modify(request)).expectNext(existing).verifyComplete();

        verify(dictionariesRepository).save(any(Dictionary.class));
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void operateUpdatesDictionaryFoundByTenantTypeAndKey() {
        Dictionary existing = new Dictionary();
        existing.setId(2L);
        existing.setCode(UUID.randomUUID());

        Dictionary old = new Dictionary();
        old.setId(2L);
        old.setCode(existing.getCode());
        old.setCreatedAt(LocalDateTime.now());

        DictionaryReq request = dictionaryRequest();

        when(dictionariesRepository.findByCode(any())).thenReturn(Mono.empty());
        when(dictionariesRepository.findByTenantCodeAndDictTypeAndDictKey(any(), any(), any()))
                .thenReturn(Mono.just(existing));
        when(dictionariesRepository.findById(2L)).thenReturn(Mono.just(old));
        when(dictionariesRepository.save(any(Dictionary.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(service.operate(request)).expectNext(existing).verifyComplete();

        verify(dictionariesRepository).save(any(Dictionary.class));
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void saveInsertsNewDictionaryAndPublishesInsertEvent() {
        Dictionary dictionary = new Dictionary();
        dictionary.setDictType("STATUS");
        Dictionary saved = new Dictionary();
        saved.setId(5L);

        when(dictionariesRepository.save(any(Dictionary.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.save(dictionary)).expectNext(saved).verifyComplete();

        verify(dictionariesRepository).save(dictionary);
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void saveUpdatesExistingDictionaryAndPublishesUpdateEvent() {
        Dictionary dictionary = new Dictionary();
        dictionary.setId(1L);
        dictionary.setDictType("STATUS");

        Dictionary old = new Dictionary();
        old.setId(1L);
        old.setCode(UUID.randomUUID());
        old.setCreatedAt(LocalDateTime.now());

        when(dictionariesRepository.findById(1L)).thenReturn(Mono.just(old));
        when(dictionariesRepository.save(any(Dictionary.class))).thenReturn(Mono.just(dictionary));

        StepVerifier.create(service.save(dictionary)).expectNext(dictionary).verifyComplete();

        verify(dictionariesRepository).findById(1L);
        verify(dictionariesRepository).save(dictionary);
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void deleteRemovesDictionaryAndPublishesDeleteEvent() {
        Dictionary dictionary = new Dictionary();
        dictionary.setId(1L);
        dictionary.setCode(UUID.randomUUID());
        DictionaryReq request = new DictionaryReq();
        request.setCode(dictionary.getCode());

        when(dictionariesRepository.findByCode(dictionary.getCode())).thenReturn(Mono.just(dictionary));
        when(dictionariesRepository.delete(any(Dictionary.class))).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(dictionariesRepository).delete(dictionary);
        verify(eventPublisher).publishEvent(any(ApplicationEvent.class));
    }

    @Test
    void deleteCompletesWhenDictionaryMissing() {
        DictionaryReq request = new DictionaryReq();
        request.setCode(UUID.randomUUID());

        when(dictionariesRepository.findByCode(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(request)).verifyComplete();

        verify(dictionariesRepository, never()).delete(any(Dictionary.class));
        verify(eventPublisher, never()).publishEvent(any(ApplicationEvent.class));
    }

    private static DictionaryReq dictionaryRequest() {
        DictionaryReq request = new DictionaryReq();
        request.setTenantCode(UUID.randomUUID());
        request.setDictType("STATUS");
        request.setDictKey("ACTIVE");
        request.setDictValue("1");
        request.setDictLabel("Active");
        return request;
    }
}
