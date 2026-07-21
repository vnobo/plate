package com.plate.boot.commons.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
}
