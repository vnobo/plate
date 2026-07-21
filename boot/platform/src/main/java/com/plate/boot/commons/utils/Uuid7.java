package com.plate.boot.commons.utils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Local RFC 9562 UUIDv7 generator.
 *
 * <p>This class replaces the third-party {@code uuid-creator} library so the project no longer
 * depends on an external UUID implementation. A UUIDv7 is time-ordered: its 48 most significant
 * bits carry the Unix millisecond timestamp, which makes it a good fit for database primary keys
 * (sequential inserts, better index locality).
 *
 * <p>Generation is monotonic within the same millisecond via a 12-bit counter. If the counter
 * overflows inside a single millisecond, or the system clock moves backwards, the timestamp is
 * advanced by one millisecond to keep values strictly increasing and collision-free.
 */
public final class Uuid7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Object LOCK = new Object();

    /** 12-bit counter used for monotonic ordering inside the same millisecond. */
    private static long lastTimestampMillis = Long.MIN_VALUE;
    private static int counter = 0;

    private Uuid7() {
    }

    /**
     * Generates a new time-ordered UUIDv7.
     *
     * @return a newly created {@link UUID} instance, providing a unique and time-ordered identifier
     */
    public static UUID next() {
        long now = Instant.now().toEpochMilli();
        long timestamp;
        int randA;
        synchronized (LOCK) {
            if (now > lastTimestampMillis) {
                lastTimestampMillis = now;
                counter = RANDOM.nextInt(0x1000);
            } else {
                // Same millisecond (or clock moved backwards): stay monotonic.
                if (now < lastTimestampMillis) {
                    now = lastTimestampMillis;
                }
                counter = (counter + 1) & 0x0FFF;
                if (counter == 0) {
                    // Counter overflowed for this millisecond: bump the timestamp by 1ms.
                    lastTimestampMillis = lastTimestampMillis + 1;
                    now = lastTimestampMillis;
                    counter = RANDOM.nextInt(0x1000);
                }
            }
            timestamp = now;
            randA = counter;
        }

        // msb: [48-bit unix_ts_ms][4-bit version=0x7][12-bit rand_a]
        long msb = ((timestamp & 0xFFFFFFFFFFFFL) << 16) | (0x7L << 12) | (randA & 0x0FFFL);
        // lsb: [2-bit variant=0b10][62-bit rand_b]
        long lsb = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
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
