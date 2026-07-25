package com.plate.boot.commons.base;

import com.plate.boot.commons.utils.ContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import static org.assertj.core.api.Assertions.assertThat;

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
}
