package com.plate.boot.commons.utils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local RFC 9562 UUIDv7 generator.
 *
 * <p>This class replaces the third-party {@code uuid-creator} library so the project no longer
 * depends on an external UUID implementation. A UUIDv7 is time-ordered: its 48 most significant
 * bits carry the Unix millisecond timestamp, which makes it a good fit for database primary keys
 * (sequential inserts, better index locality).
 *
 * <p>Generation is lock-free: the 48-bit timestamp and the 12-bit monotonic counter share a single
 * {@link AtomicLong} word (counter in bits 0-11, timestamp in bits 12-59). A compare-and-swap keeps
 * values strictly increasing across threads and clock rollbacks — when the counter overflows it
 * carries into the timestamp bits, so monotonicity holds without any branching. This removes the
 * global lock contention a {@code synchronized} generator would impose under high concurrency
 * (including virtual threads).
 *
 * <p>Random bits come from {@link ThreadLocalRandom}, which is per-thread and uncontended. These
 * UUIDs serve as primary keys (collision resistance), not security tokens, so cryptographic
 * randomness from {@link java.security.SecureRandom} is not required by RFC 9562.
 */
public final class Uuid7 {

    /**
     * Monotonic generation state: bits 0-11 hold the 12-bit counter, bits 12-59 hold the 48-bit
     * Unix millisecond timestamp. Initialized to {@code 0}, so the first call — with a real,
     * positive epoch timestamp — always takes the "new millisecond" branch and seeds a fresh counter.
     */
    private static final AtomicLong STATE = new AtomicLong();

    private Uuid7() {
    }

    /**
     * Generates a new time-ordered UUIDv7.
     *
     * @return a newly created {@link UUID} instance, providing a unique and time-ordered identifier
     */
    public static UUID next() {
        long now = System.currentTimeMillis();
        long installed;
        while (true) {
            long state = STATE.get();
            long timestamp = state >>> 12;
            long next;
            if (now > timestamp) {
                // New millisecond: seed the 12-bit counter with fresh random data (RFC 9562 method 2).
                next = (now << 12) | ThreadLocalRandom.current().nextInt(0x1000);
            } else {
                // Same millisecond or clock moved backwards: strictly increase. Overflow of the
                // 12-bit counter carries into the timestamp bits, preserving monotonicity.
                next = state + 1;
            }
            if (STATE.compareAndSet(state, next)) {
                installed = next;
                break;
            }
        }
        long timestamp = installed >>> 12;
        int randA = (int) (installed & 0x0FFF);
        // msb: [48-bit unix_ts_ms][4-bit version=0x7][12-bit rand_a]
        long msb = (timestamp << 16) | 0x7000L | randA;
        // lsb: [2-bit variant=0b10][62-bit rand_b]
        long lsb = ThreadLocalRandom.current().nextLong() & 0x3FFFFFFFFFFFFFFFL | 0x8000000000000000L;
        return new UUID(msb, lsb);
    }

    /**
     * Extracts the embedded Unix millisecond timestamp from a UUIDv7.
     *
     * @param uuid a UUIDv7 instance
     * @return the Unix timestamp in milliseconds carried by the UUID
     */
    public static long extractTimestampMillis(UUID uuid) {
        return (uuid.getMostSignificantBits() >> 16) & 0xFFFFFFFFFFFFL;
    }
}
