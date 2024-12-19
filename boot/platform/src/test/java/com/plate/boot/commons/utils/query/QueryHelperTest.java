package com.plate.boot.commons.utils.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryHelperTest {

    private static final Pageable DEFAULT_PAGEABLE = PageRequest
            .of(0, 25, Sort.by("id").ascending());

    private Object testObject;
    private Pageable pageable;
    private Collection<String> skipKeys;
    private String prefix;

    @BeforeEach
    void setUp() {
        testObject = new Object();
        pageable = DEFAULT_PAGEABLE;
        skipKeys = Set.of("skipKey1", "skipKey2");
        prefix = "prefix";
    }

    @Test
    void query_withValidObjectAndPageable_returnsQueryFragment() {
        QueryFragment result = QueryHelper.query(testObject, pageable, skipKeys, prefix);
        assertNotNull(result);
        assertTrue(result.getColumns().toString().indexOf("*") > 0);
    }

    @Test
    void query_withEmptyObject_returnsEmptyQueryFragment() {
        testObject = new Object();
        QueryFragment result = QueryHelper.query(testObject, pageable, skipKeys, prefix);
        assertNotNull(result);
        assertTrue(result.getColumns().toString().indexOf("*") > 0);
    }

}