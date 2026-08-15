package com.plate.boot.relational.logger;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggersService}.
 * <p>
 * The {@code search}/{@code page} methods execute raw SQL through
 * {@link DatabaseUtils#query(String, java.util.Map, Class)} and
 * {@link DatabaseUtils#count(String, java.util.Map)}, so those static helpers are mocked. The
 * cache is initialised per-test so that {@code cache.clear()} and cache population do not NPE.
 */
@ExtendWith(MockitoExtension.class)
class LoggersServiceTest {

    @Mock
    private LoggersRepository loggersRepository;

    @InjectMocks
    private LoggersService service;

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
        service.afterPropertiesSet();
    }

    @Test
    void searchReturnsLoggersFromDatabase() {
        LoggerRes res = new LoggerRes();
        res.setId(1L);
        LoggerReq request = new LoggerReq();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), any(), ArgumentMatchers.<Class<LoggerRes>>any()))
                    .thenReturn(Flux.just(res));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.search(request, pageable))
                    .expectNext(res)
                    .verifyComplete();
        }
    }

    @Test
    void pageCombinesSearchResultsAndCount() {
        LoggerRes first = new LoggerRes();
        first.setId(1L);
        LoggerRes second = new LoggerRes();
        second.setId(2L);
        LoggerReq request = new LoggerReq();
        Pageable pageable = PageRequest.of(0, 10);

        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), any(), ArgumentMatchers.<Class<LoggerRes>>any()))
                    .thenReturn(Flux.just(first, second));
            db.when(() -> DatabaseUtils.count(anyString(), any()))
                    .thenReturn(Mono.just(2L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(service.page(request, pageable))
                    .assertNext(page -> assertThat(page.getContent()).containsExactly(first, second))
                    .verifyComplete();
        }
    }

    @Test
    void operateInsertsNewLogger() {
        Logger saved = new Logger();
        saved.setId(1L);
        LoggerReq request = loggerRequest();

        when(loggersRepository.findByCode(any())).thenReturn(Mono.empty());
        when(loggersRepository.save(any(Logger.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.operate(request)).expectNext(saved).verifyComplete();

        verify(loggersRepository).save(any(Logger.class));
    }

    @Test
    void operateUpdatesExistingLogger() {
        UUID code = UUID.randomUUID();
        Logger existing = new Logger();
        existing.setId(1L);
        existing.setCode(code);

        Logger old = new Logger();
        old.setId(1L);
        old.setCode(code);
        old.setCreatedAt(LocalDateTime.now());

        LoggerReq request = loggerRequest();
        request.setId(1L);
        request.setCode(code);

        when(loggersRepository.findByCode(code)).thenReturn(Mono.just(existing));
        when(loggersRepository.findById(1L)).thenReturn(Mono.just(old));
        when(loggersRepository.save(any(Logger.class))).thenReturn(Mono.just(existing));

        StepVerifier.create(service.operate(request)).expectNext(existing).verifyComplete();

        verify(loggersRepository).findById(1L);
        verify(loggersRepository).save(any(Logger.class));
    }

    @Test
    void saveInsertsNewLogger() {
        Logger logger = new Logger();
        logger.setPrefix("P");
        Logger saved = new Logger();
        saved.setId(1L);

        when(loggersRepository.save(any(Logger.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.save(logger)).expectNext(saved).verifyComplete();

        verify(loggersRepository).save(logger);
    }

    @Test
    void saveUpdatesExistingLogger() {
        Logger logger = new Logger();
        logger.setId(1L);
        logger.setPrefix("P");

        Logger old = new Logger();
        old.setId(1L);
        old.setCreatedAt(LocalDateTime.now());

        when(loggersRepository.findById(1L)).thenReturn(Mono.just(old));
        when(loggersRepository.save(any(Logger.class))).thenReturn(Mono.just(logger));

        StepVerifier.create(service.save(logger)).expectNext(logger).verifyComplete();

        verify(loggersRepository).findById(1L);
        verify(loggersRepository).save(logger);
    }

    @Test
    void clearLoggersDeletesExpiredRecords() {
        when(loggersRepository.deleteByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(Mono.just(5L));

        service.clearLoggers();

        verify(loggersRepository).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }

    @Test
    void processLoggerEventSavesInsertEvent() {
        Logger saved = new Logger();
        saved.setId(1L);
        saved.setCode(UUID.randomUUID());
        saved.setPrefix("P");

        LoggerReq loggerReq = loggerRequest();
        LoggerEvent event = LoggerEvent.insert(loggerReq);

        when(loggersRepository.findByCode(any())).thenReturn(Mono.empty());
        when(loggersRepository.save(any(Logger.class))).thenReturn(Mono.just(saved));

        service.processLoggerEvent(event);

        verify(loggersRepository).save(any(Logger.class));
    }

    @Test
    void processLoggerEventIgnoresNonInsertKind() {
        LoggerReq loggerReq = loggerRequest();
        LoggerEvent event = LoggerEvent.update(loggerReq);

        service.processLoggerEvent(event);

        verifyNoInteractions(loggersRepository);
    }

    private static LoggerReq loggerRequest() {
        LoggerReq request = new LoggerReq();
        request.setOperator("admin");
        request.setPrefix("P");
        request.setUrl("/api");
        request.setStatus("200");
        return request;
    }
}
