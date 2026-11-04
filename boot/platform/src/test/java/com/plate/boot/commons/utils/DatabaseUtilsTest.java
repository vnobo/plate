package com.plate.boot.commons.utils;

import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.exception.RestServerException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the pure parts of {@link DatabaseUtils} (no Spring / R2DBC connection required).
 * <p>
 * {@code getBeanSize} is exercised with the shared {@link ContextUtils#OBJECT_MAPPER}, and {@code batchEvent}
 * is a pure reactive operator that needs no database. The SQL-bound methods ({@code query}, {@code count},
 * {@code count(String, Map)}) rely on the static {@code DATABASE_CLIENT}/{@code ENTITY_TEMPLATE} which are only
 * populated by a Spring context, so they are covered by the integration tests instead of these unit tests.
 */
class DatabaseUtilsTest {

    private static JsonMapper savedMapper;

    private R2dbcEntityTemplate savedTemplate;
    private DatabaseClient savedClient;
    private R2dbcConverter savedConverter;
    private DataSize savedMaxSize;

    @BeforeAll
    static void setUp() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDown() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
    }

    @BeforeEach
    void captureDatabaseStatics() {
        savedTemplate = DatabaseUtils.ENTITY_TEMPLATE;
        savedClient = DatabaseUtils.DATABASE_CLIENT;
        savedConverter = DatabaseUtils.R2DBC_CONVERTER;
        savedMaxSize = DatabaseUtils.MAX_IN_MEMORY_SIZE;
    }

    @AfterEach
    void restoreDatabaseStatics() {
        DatabaseUtils.ENTITY_TEMPLATE = savedTemplate;
        DatabaseUtils.DATABASE_CLIENT = savedClient;
        DatabaseUtils.R2DBC_CONVERTER = savedConverter;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMaxSize;
    }

    @Test
    void getBeanSizeReturnsZeroForNull() {
        assertThat(DatabaseUtils.getBeanSize(null).toBytes()).isZero();
    }

    @Test
    void getBeanSizeReturnsPositiveForSerializableObject() {
        assertThat(DatabaseUtils.getBeanSize(Map.of("a", 1)).toBytes()).isPositive();
    }

    @Test
    void batchEventEmitsStartItemsAndEnd() {
        Flux<String> items = Flux.just("a", "b", "c");

        StepVerifier.create(DatabaseUtils.batchEvent(items, req -> Mono.just("ok")))
                .assertNext(e -> {
                    assertThat(e.getProcessed()).isZero();
                    assertThat(e.getMessage()).contains("Starting");
                })
                .assertNext(e -> {
                    assertThat(e.getProcessed()).isEqualTo(1);
                    assertThat(e.getIsOk()).isTrue();
                    assertThat(e.getRes()).isEqualTo("ok");
                })
                .assertNext(e -> {
                    assertThat(e.getProcessed()).isEqualTo(2);
                    assertThat(e.getRes()).isEqualTo("ok");
                })
                .assertNext(e -> {
                    assertThat(e.getProcessed()).isEqualTo(3);
                    assertThat(e.getRes()).isEqualTo("ok");
                })
                .assertNext(e -> {
                    assertThat(e.getProcessed()).isEqualTo(100);
                    assertThat(e.getMessage()).contains("completed");
                })
                .verifyComplete();
    }

    @Test
    void batchEventReportsErrorPerItem() {
        Flux<String> items = Flux.just("a");

        StepVerifier.create(DatabaseUtils.batchEvent(items,
                req -> Mono.error(new RuntimeException("boom", new IllegalStateException("root cause")))))
                .assertNext(e -> assertThat(e.getProcessed()).isZero())
                .assertNext(e -> {
                    assertThat(e.getProcessed()).isEqualTo(1);
                    assertThat(e.getIsOk()).isFalse();
                    assertThat(e.getError()).isInstanceOf(RestServerException.class);
                })
                .assertNext(e -> assertThat(e.getProcessed()).isEqualTo(100))
                .verifyComplete();
    }

    @Test
    void progressEventHelpersCompileAndChain() {
        ProgressEvent event = ProgressEvent.of(1L, "req")
                .withResult("done", "res");
        assertThat(event.getIsOk()).isTrue();
        assertThat(event.getRes()).isEqualTo("res");

        ProgressEvent failed = ProgressEvent.of(2L, "req").withError("bad", new RestServerException("x", new RuntimeException()));
        assertThat(failed.getIsOk()).isFalse();
        assertThat(failed.getError()).isNotNull();
    }

    @Test
    void getBeanSizeReturnsZeroWhenSerializationFails() {
        assertThat(DatabaseUtils.getBeanSize(new ThrowingBean()).toBytes()).isZero();
    }

    @Test
    void queryByQueryDelegatesToEntityTemplate() {
        R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
        DatabaseUtils.ENTITY_TEMPLATE = template;
        Item item = new Item("a");
        doReturn(Flux.just(item)).when(template).select(any(Query.class), any(Class.class));

        StepVerifier.create(DatabaseUtils.query(Query.empty(), Item.class))
                .expectNext(item)
                .verifyComplete();
    }

    @Test
    void countByQueryDelegatesToEntityTemplate() {
        R2dbcEntityTemplate template = mock(R2dbcEntityTemplate.class);
        DatabaseUtils.ENTITY_TEMPLATE = template;
        doReturn(Mono.just(5L)).when(template).count(any(Query.class), any(Class.class));

        StepVerifier.create(DatabaseUtils.count(Query.empty(), Item.class))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void queryBySqlBindsParamsAndMapsRows() {
        DatabaseClient client = mock(DatabaseClient.class);
        DatabaseUtils.DATABASE_CLIENT = client;
        DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class);
        RowsFetchSpec<Object> rows = mock(RowsFetchSpec.class);
        Item item = new Item("a");
        doReturn(spec).when(client).sql(any(Supplier.class));
        doReturn(spec).when(spec).bindValues(any(Map.class));
        doReturn(rows).when(spec).map(any(BiFunction.class));
        doReturn(Flux.just(item)).when(rows).all();

        StepVerifier.create(DatabaseUtils.query("select * from items", Map.of("k", "v"), Item.class))
                .expectNext(item)
                .verifyComplete();
    }

    @Test
    void countBySqlBindsParamsAndMapsLongValue() {
        DatabaseClient client = mock(DatabaseClient.class);
        DatabaseUtils.DATABASE_CLIENT = client;
        DatabaseClient.GenericExecuteSpec spec = mock(DatabaseClient.GenericExecuteSpec.class);
        RowsFetchSpec<Object> rows = mock(RowsFetchSpec.class);
        doReturn(spec).when(client).sql(any(Supplier.class));
        doReturn(spec).when(spec).bindValues(any(Map.class));
        doReturn(rows).when(spec).mapValue(Long.class);
        doReturn(Mono.just(42L)).when(rows).first();

        StepVerifier.create(DatabaseUtils.count("select count(*) from items", Map.of()))
                .expectNext(42L)
                .verifyComplete();
    }

    // ---- test fixtures -----------------------------------------------------

    static class Item {
        private final String name;

        Item(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class ThrowingBean {
        public String getBoom() {
            throw new IllegalStateException("boom");
        }
    }
}
