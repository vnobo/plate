package com.plate.boot.commons.utils.query;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for QueryHelper.
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
class QueryHelperTest {

    @Test
    void applyPageTest() {
        // Prepare test data
        Pageable pageable = PageRequest.of(0, 10);
        String prefix = "tableAlias";

        // Call the method under test
        String result = QueryHelper.applyPage(pageable, prefix);

        // Assert expected outcome
        assertEquals(" order by tableAlias.id desc limit 10 offset 0", result);
    }

    @Test
    void applySortWithSingleOrder() {
        // Prepare test data
        Sort sort = Sort.by("name").ascending();
        String prefix = "tableAlias";

        // Call the method under test
        String result = QueryHelper.applySort(sort, prefix);

        // Assert expected outcome
        assertEquals(" order by tableAlias.name asc", result);
    }

    @Test
    void applySortWithMultipleOrders() {
        // Prepare test data
        Sort sort = Sort.by("name").ascending().and(Sort.by("id").descending())
                .and(Sort.by("extend.name").descending())
                .and(Sort.by("extend.userModel.name").ascending());
        String prefix = "tableAlias";

        // Call the method under test
        String result = QueryHelper.applySort(sort, prefix);

        // Assert expected outcome
        assertEquals(" order by tableAlias.name asc, tableAlias.id desc, tableAlias.extend->>'name' desc, tableAlias.extend->'userModel'->>'name' asc", result);
    }

    @Test
    void applySortWithoutPrefix() {
        // Prepare test data
        Sort sort = Sort.by("name").ascending().and(Sort.by("extend.name").descending())
                .and(Sort.by("extend.user.name").ascending());

        // Call the method under test
        String result = QueryHelper.applySort(sort, null);

        // Assert expected outcome
        assertEquals(" order by name asc, extend->>'name' desc, extend->'user'->>'name' asc", result);
    }

    @Test
    void testApplyPageWithEmptyPrefix() {
        // Prepare test data
        Pageable pageable = PageRequest.of(1, 5);

        // Call the method under test
        String result = QueryHelper.applyPage(pageable, null);

        // Assert expected outcome
        assertEquals(" order by id desc limit 5 offset 5", result);
    }


    /**
     * Test applying page with a different page size and zero-based page index.
     */
    @Test
    void testApplyPageWithDifferentSizeAndIndex() {
        // Prepare test data
        Pageable pageable = PageRequest.of(2, 20);
        String prefix = "tableAlias";

        // Call the method under test
        String result = QueryHelper.applyPage(pageable, prefix);

        // Assert expected outcome
        assertEquals(" order by tableAlias.id desc limit 20 offset 40", result);
    }
}
