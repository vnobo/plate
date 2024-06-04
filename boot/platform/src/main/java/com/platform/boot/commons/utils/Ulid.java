package com.platform.boot.commons.utils;

import com.platform.boot.commons.exception.RestServerException;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Random;

/**
 * ULID string generator and parser class, using Crockford Base32 encoding. Only upper case letters
 * are used for generation. Parsing allows upper and lower case letters, and i and l will be treated
 * as 1 and o will be treated as 0. <br>
 * <br>
 * ULID generation examples:<br>
 *
 * <pre>
 * String ulid1 = ULID.random();
 * String ulid2 = ULID.random(ThreadLocalRandom.current());
 * String ulid3 = ULID.random(SecureRandom.newInstance("SHA1PRNG"));
 * byte[] entropy = new byte[] {0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9};
 * String ulid4 = ULID.generate(System.currentTimeMillis(), entropy);
 * </pre>
 * <p>
 * ULID parsing examples:<br>
 *
 * <pre>
 * String ulid = "003JZ9J6G80123456789abcdef";
 * assert ULID.isValid(ulid);
 * long ts = ULID.getTimestamp(ulid);
 * assert ts == 123456789000L;
 * byte[] entropy = ULID.getEntropy(ulid);
 * </pre>
 *
 * @author azam
 * @see <a href="http://www.crockford.com/wrmg/base32.html">Base32 Encoding</a>
 * @see <a href="https://github.com/ulid/spec">ULID</a>
 */
public class Ulid implements Serializable {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int ENTROPY_LENGTH = 10;

    private static final long MIN_TIME = 0x0L;

    private static final long MAX_TIME = 0x0000ffffffffffffL;

    private static final char[] C = new char[]{
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
            0x38, 0x39, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
            0x47, 0x48, 0x4a, 0x4b, 0x4d, 0x4e, 0x50, 0x51,
            0x52, 0x53, 0x54, 0x56, 0x57, 0x58, 0x59, 0x5a
    };

    /**
     * Generate random UID string using {@link Random} instance.
     *
     * @return UID string
     */
    public static String random() {
        byte[] entropy = new byte[10];
        SECURE_RANDOM.nextBytes(entropy);
        return generate(System.currentTimeMillis(), entropy);
    }

    /**
     * Generate UID string from Unix epoch timestamp in millisecond and entropy bytes. Throws
     * {@link IllegalArgumentException} if timestamp is less than {@value #MIN_TIME}, is
     * more than {@value #MAX_TIME}, or entropy bytes is null or less than 10 bytes.
     *
     * @param time    Unix epoch timestamp in millisecond
     * @param entropy Entropy bytes
     * @return UID string
     */
    public static String generate(long time, byte[] entropy) {
        if (time < MIN_TIME || time > MAX_TIME || entropy == null || entropy.length < ENTROPY_LENGTH) {
            throw RestServerException.withMsg(
                    "Time is too long, or entropy is less than 10 bytes or null!",
                    "Time is too long, or entropy is less than 10 bytes or null!");
        }

        char[] chars = new char[26];

        // time
        chars[0] = C[((byte) (time >>> 45)) & 0x1f];
        chars[1] = C[((byte) (time >>> 40)) & 0x1f];
        chars[2] = C[((byte) (time >>> 35)) & 0x1f];
        chars[3] = C[((byte) (time >>> 30)) & 0x1f];
        chars[4] = C[((byte) (time >>> 25)) & 0x1f];
        chars[5] = C[((byte) (time >>> 20)) & 0x1f];
        chars[6] = C[((byte) (time >>> 15)) & 0x1f];
        chars[7] = C[((byte) (time >>> 10)) & 0x1f];
        chars[8] = C[((byte) (time >>> 5)) & 0x1f];
        chars[9] = C[((byte) (time)) & 0x1f];

        // entropy
        chars[10] = C[(byte) ((entropy[0] & 0xff) >>> 3)];
        chars[11] = C[(byte) (((entropy[0] << 2) | ((entropy[1] & 0xff) >>> 6)) & 0x1f)];
        chars[12] = C[(byte) (((entropy[1] & 0xff) >>> 1) & 0x1f)];
        chars[13] = C[(byte) (((entropy[1] << 4) | ((entropy[2] & 0xff) >>> 4)) & 0x1f)];
        chars[14] = C[(byte) (((entropy[2] << 1) | ((entropy[3] & 0xff) >>> 7)) & 0x1f)];
        chars[15] = C[(byte) (((entropy[3] & 0xff) >>> 2) & 0x1f)];
        chars[16] = C[(byte) (((entropy[3] << 3) | ((entropy[4] & 0xff) >>> 5)) & 0x1f)];
        chars[17] = C[(byte) (entropy[4] & 0x1f)];
        chars[18] = C[(byte) ((entropy[5] & 0xff) >>> 3)];
        chars[19] = C[(byte) (((entropy[5] << 2) | ((entropy[6] & 0xff) >>> 6)) & 0x1f)];
        chars[20] = C[(byte) (((entropy[6] & 0xff) >>> 1) & 0x1f)];
        chars[21] = C[(byte) (((entropy[6] << 4) | ((entropy[7] & 0xff) >>> 4)) & 0x1f)];
        chars[22] = C[(byte) (((entropy[7] << 1) | ((entropy[8] & 0xff) >>> 7)) & 0x1f)];
        chars[23] = C[(byte) (((entropy[8] & 0xff) >>> 2) & 0x1f)];
        chars[24] = C[(byte) (((entropy[8] << 3) | ((entropy[9] & 0xff) >>> 5)) & 0x1f)];
        chars[25] = C[(byte) (entropy[9] & 0x1f)];

        return new String(chars);
    }

}