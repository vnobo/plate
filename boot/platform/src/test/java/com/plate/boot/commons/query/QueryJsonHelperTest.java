package com.plate.boot.commons.query;

import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link QueryJsonHelper} (no Spring / container required).
 * Focus: JSON sort-property transformation and JSON-path condition building,
 * including escaping of JSON keys and parameter binding (no value concatenation).
 */
class QueryJsonHelperTest {

    private static R2dbcConverter savedConverter;

    @BeforeEach
    void setUpConverter() {
        savedConverter = DatabaseUtils.R2DBC_CONVERTER;
        R2dbcConverter stub = mock(R2dbcConverter.class);
        when(stub.writeValue(any(), any(org.springframework.data.core.TypeInformation.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        DatabaseUtils.R2DBC_CONVERTER = stub;
    }

    @AfterEach
    void tearDownConverter() {
        DatabaseUtils.R2DBC_CONVERTER = savedConverter;
    }


    @Test
    void transformSortForJsonNullReturnsUnsorted() {
        assertThat(QueryJsonHelper.transformSortForJson(null)).isEqualTo(Sort.unsorted());
    }

    @Test
    void transformSortForJsonEmptyReturnsUnsorted() {
        assertThat(QueryJsonHelper.transformSortForJson(Sort.unsorted())).isEqualTo(Sort.unsorted());
    }

    @Test
    void transformSortForJsonConvertsCamelToSnakeCase() {
        Sort result = QueryJsonHelper.transformSortForJson(Sort.by("userName").ascending());

        assertThat(result.get().findFirst().orElseThrow().getProperty()).isEqualTo("user_name");
    }

    @Test
    void transformSortForJsonBuildsJsonPathForNestedProperty() {
        Sort result = QueryJsonHelper.transformSortForJson(Sort.by("extend.name").descending());

        assertThat(result.get().findFirst().orElseThrow().getProperty()).isEqualTo("extend->>'name'");
        assertThat(result.get().findFirst().orElseThrow().isDescending()).isTrue();
    }

    @Test
    void queryJsonBuildsLikeConditionForNestedPath() {
        Map<String, Object> params = Map.of("extend.usernameLike", "John");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'username'");
        assertThat(sql).contains("LIKE :");
        assertThat(sql).doesNotContain("John");
    }

    @Test
    void queryJsonEscapesSingleQuotesInKey() {
        Map<String, Object> params = Map.of("extend.user'NameLike", "x");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        // The apostrophe inside the JSON key must be doubled to avoid breaking SQL.
        assertThat(sql).contains("user''");
        assertThat(sql).contains("LIKE :");
    }

    @Test
    void queryJsonThrowsForSingleSegmentPath() {
        Map<String, Object> params = Map.of("invalid", "value");

        assertThatThrownBy(() -> QueryJsonHelper.queryJson(params, null))
                .isInstanceOf(QueryException.class);
    }

    @Test
    void queryJsonWithPrefixQualifiesColumn() {
        Map<String, Object> params = Map.of("extend.ageGt", 18);
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "e");
        String sql = condition.toSql();

        assertThat(sql).contains("e.extend->>'age'");
        assertThat(sql).contains(">");
        assertThat(sql).doesNotContain("18");
    }
}
