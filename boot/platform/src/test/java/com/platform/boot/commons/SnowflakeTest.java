package com.platform.boot.commons;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SnowflakeTest {
    private final Snowflake snowflake = new Snowflake(1, 1);

    @Test
    public void testNextId() {
        long id1 = snowflake.nextId();
        long id2 = snowflake.nextId();
        assertNotEquals(id1, id2);
        assertTrue(id1 < id2);
    }
}