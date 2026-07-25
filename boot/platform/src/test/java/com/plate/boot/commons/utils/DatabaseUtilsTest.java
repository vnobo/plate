package com.plate.boot.commons.utils;

import com.plate.boot.commons.ProgressEvent;
import com.plate.boot.commons.exception.RestServerException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeAll
    static void setUp() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
    }

    @AfterAll
    static void tearDown() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
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
}
