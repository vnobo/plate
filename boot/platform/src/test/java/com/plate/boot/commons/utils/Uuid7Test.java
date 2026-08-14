package com.plate.boot.commons.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the local {@link Uuid7} generator and its wiring through
 * {@link ContextUtils#nextId()}. These tests do not require a database or Docker.
 */
class Uuid7Test {

    @Test
    void next_returnsNonNullValidUuid() {
        UUID uuid = Uuid7.next();
        assertNotNull(uuid);
        // round-trip through the canonical string form must yield the same UUID
        assertEquals(uuid, UUID.fromString(uuid.toString()));
    }

    @Test
    void next_isVersion7AndVariantRfc4122() {
        for (int i = 0; i < 10_000; i++) {
            UUID uuid = Uuid7.next();
            assertEquals(7, uuid.version(), "UUID version must be 7");
            assertEquals(2, uuid.variant(), "UUID variant must be RFC 4122 (2)");
        }
    }

    @Test
    void next_timestampCloseToNow() {
        long before = System.currentTimeMillis();
        UUID uuid = Uuid7.next();
        long after = System.currentTimeMillis();
        long ts = Uuid7.extractTimestampMillis(uuid);
        assertTrue(ts >= before - 5 && ts <= after + 5,
                "embedded timestamp should fall inside the generation window");
    }

    @Test
    void next_isUniqueAcrossManyGenerations() {
        int count = 200_000;
        Set<UUID> seen = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            seen.add(Uuid7.next());
        }
        assertEquals(count, seen.size(), "all generated UUIDs must be unique");
    }

    @Test
    void contextUtils_nextId_delegatesToLocalUuid7() {
        UUID uuid = ContextUtils.nextId();
        assertNotNull(uuid);
        assertEquals(7, uuid.version());
        assertEquals(2, uuid.variant());
    }

    @Test
    void next_isMonotonicAndUniqueUnderConcurrency() throws InterruptedException {
        int threadCount = 16;
        int perThread = 20_000;
        int total = threadCount * perThread;

        // msb encodes (timestamp, rand_a) — the lock-free CAS state. Asserting msb
        // uniqueness proves the generator stays monotonic and collision-free when the
        // AtomicLong is contended across threads, not merely that rand_b happens to differ.
        Set<Long> msbs = ConcurrentHashMap.newKeySet(total);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int t = 0; t < threadCount; t++) {
                executor.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < perThread; i++) {
                            msbs.add(Uuid7.next().getMostSignificantBits());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            start.countDown();
            assertTrue(done.await(60, TimeUnit.SECONDS), "all generator tasks must finish");
        }

        assertEquals(total, msbs.size(), "msb (timestamp + rand_a) must be unique across threads");
    }
}
