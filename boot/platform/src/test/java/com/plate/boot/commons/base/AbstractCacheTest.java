package com.plate.boot.commons.base;

import com.plate.boot.commons.utils.ContextUtils;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.unit.DataSize;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for the pure cache-initialisation logic in {@link AbstractCache}
 * (no Spring / R2DBC connection required).
 * <p>
 * When {@link ContextUtils#CACHE_MANAGER} is {@code null}, {@code initializingCache} falls back to a
 * local {@link ConcurrentMapCache}. The SQL-bound {@code queryWithCache}/{@code countWithCache}
 * helpers depend on the static {@code DatabaseUtils} template/client, which are only populated by
 * Spring, so they are intentionally NOT tested here (see report).
 */
class AbstractCacheTest {

    private static org.springframework.cache.CacheManager savedManager;

    @BeforeAll
    static void setUp() {
        savedManager = ContextUtils.CACHE_MANAGER;
        ContextUtils.CACHE_MANAGER = null;
    }

    @AfterAll
    static void tearDown() {
        ContextUtils.CACHE_MANAGER = savedManager;
    }

    static class TestCache extends AbstractCache {
    }

    @Test
    void afterPropertiesSetInitialisesLocalCache() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        assertThat(cache.cache).isNotNull();
        assertThat(cache.cache).isInstanceOf(ConcurrentMapCache.class);
        assertThat(cache.cache.getName()).endsWith(".cache");
    }

    @Test
    void initializingCacheFallsBackToConcurrentMapCache() {
        Cache cache = new TestCache().initializingCache("custom.cache");

        assertThat(cache).isInstanceOf(ConcurrentMapCache.class);
        assertThat(cache.getName()).isEqualTo("custom.cache");
    }

    @Test
    void queryWithCacheFluxExecutesSourceOnMissAndCaches() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(cache.queryWithCache("k", Flux.just("a", "b")))
                    .expectNext("a", "b")
                    .verifyComplete();

            assertThat(cache.cache.get("k:data", List.class)).containsExactly("a", "b");
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void queryWithCacheFluxReturnsCachedDataOnHit() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();
        cache.cache.put("k:data", List.of("x", "y"));

        StepVerifier.create(cache.queryWithCache("k", Flux.just("ignored")))
                .expectNext("x", "y")
                .verifyComplete();
    }

    @Test
    void queryWithCacheQueryDelegatesToDatabaseUtils() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(any(Query.class), eq(String.class)))
                    .thenReturn(Flux.just("a"));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(cache.queryWithCache("q", Query.empty(), String.class))
                    .expectNext("a")
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void queryWithCacheSqlDelegatesToDatabaseUtils() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.query(anyString(), anyMap(), eq(String.class)))
                    .thenReturn(Flux.just("a"));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            StepVerifier.create(cache.queryWithCache("q", "select 1", Map.of(), String.class))
                    .expectNext("a")
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void countWithCacheQueryDelegatesToDatabaseUtils() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.count(any(Query.class), eq(String.class)))
                    .thenReturn(Mono.just(9L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(8));

            StepVerifier.create(cache.countWithCache("q", Query.empty(), String.class))
                    .expectNext(9L)
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void countWithCacheSqlDelegatesToDatabaseUtils() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.count(anyString(), anyMap())).thenReturn(Mono.just(9L));
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(8));

            StepVerifier.create(cache.countWithCache("q", "select count(*)", Map.of()))
                    .expectNext(9L)
                    .verifyComplete();
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void countWithCacheMonoReturnsCachedValueOnHit() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();
        cache.cache.put("k:count", 5L);

        StepVerifier.create(cache.countWithCache("k", Mono.just(99L)))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void countWithCacheMonoFallsBackToSourceOnMissAndCachesResult() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();
        AtomicBoolean subscribed = new AtomicBoolean(false);

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(8));

            StepVerifier.create(cache.countWithCache("k",
                            Mono.fromSupplier(() -> {
                                subscribed.set(true);
                                return 42L;
                            })))
                    .expectNext(42L)
                    .verifyComplete();

            assertThat(subscribed).isTrue();
            assertThat(cache.cache.get("k:count", Long.class)).isEqualTo(42L);
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }
    }

    @Test
    void cachePutSkipsLargeObjects() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofBytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(100));

            cache.cachePut("k", "big");
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }

        assertThat(cache.cache.get("k")).isNull();
    }

    @Test
    void cachePutStoresSmallObjects() {
        TestCache cache = new TestCache();
        cache.afterPropertiesSet();

        DataSize savedMax = DatabaseUtils.MAX_IN_MEMORY_SIZE;
        DatabaseUtils.MAX_IN_MEMORY_SIZE = DataSize.ofMegabytes(1);
        try (MockedStatic<DatabaseUtils> db = mockStatic(DatabaseUtils.class)) {
            db.when(() -> DatabaseUtils.getBeanSize(any())).thenReturn(DataSize.ofBytes(16));

            cache.cachePut("k", "small");
        } finally {
            DatabaseUtils.MAX_IN_MEMORY_SIZE = savedMax;
        }

        assertThat(cache.cache.get("k", String.class)).isEqualTo("small");
    }
}
