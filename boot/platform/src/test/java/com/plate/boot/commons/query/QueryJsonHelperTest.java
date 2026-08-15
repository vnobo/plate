package com.plate.boot.commons.query;

import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;

import java.util.LinkedHashMap;
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

    @Test
    void transformSortForJsonBuildsDeepNestedPath() {
        Sort result = QueryJsonHelper.transformSortForJson(Sort.by("a.b.c").descending());

        assertThat(result.get().findFirst().orElseThrow().getProperty()).isEqualTo("a->'b'->>'c'");
        assertThat(result.get().findFirst().orElseThrow().isDescending()).isTrue();
    }

    @Test
    void transformSortForJsonThrowsForEmptyColumnName() {
        assertThatThrownBy(() -> QueryJsonHelper.transformSortForJson(Sort.by(".name")))
                .isInstanceOf(QueryException.class);
    }

    @Test
    void transformSortForJsonThrowsForInvalidColumnName() {
        assertThatThrownBy(() -> QueryJsonHelper.transformSortForJson(Sort.by("bad-name")))
                .isInstanceOf(QueryException.class);
    }

    @Test
    void queryJsonBuildsBetweenCondition() {
        Map<String, Object> params = Map.of("extend.ageBetween", "18,65");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);

        assertThat(condition.toSql()).contains("extend->>'age' BETWEEN :extend_age1 AND :extend_age2");
        assertThat(condition.get("extend_age1")).isEqualTo("18");
        assertThat(condition.get("extend_age2")).isEqualTo("65");
    }

    @Test
    void queryJsonBuildsNotBetweenCondition() {
        Map<String, Object> params = Map.of("extend.ageNotBetween", "1,2");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);

        assertThat(condition.toSql()).contains("extend->>'age' NOT BETWEEN :extend_age1 AND :extend_age2");
    }

    @Test
    void queryJsonBuildsInAndNotInConditions() {
        Map<String, Object> params = Map.of(
                "extend.statusIn", "a,b",
                "extend.roleNotIn", "x,y");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'status' IN (:extend_status0, :extend_status1");
        assertThat(sql).contains("extend->>'role' NOT IN (:extend_role0, :extend_role1");
    }

    @Test
    void queryJsonBuildsNullConditions() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("extend.aIsNull", null);
        params.put("extend.bNotNull", null);
        params.put("extend.cIsNotNull", null);
        params.put("extend.dNull", null);
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'a' IS NULL")
                .contains("extend->>'b' IS NOT NULL")
                .contains("extend->>'c' IS NOT NULL")
                .contains("extend->>'d' IS NULL");
    }

    @Test
    void queryJsonBuildsLikeVariants() {
        Map<String, Object> params = Map.of(
                "extend.aLike", "x",
                "extend.bNotLike", "x",
                "extend.cStartingWith", "x",
                "extend.dEndingWith", "x",
                "extend.eContaining", "x",
                "extend.fNotContaining", "x");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'a' LIKE :")
                .contains("extend->>'b' NOT LIKE :")
                .contains("extend->>'c' LIKE :")
                .contains("extend->>'d' LIKE :")
                .contains("extend->>'e' LIKE :")
                .contains("extend->>'f' NOT LIKE :");
    }

    @Test
    void queryJsonBuildsNotAndBooleanConditions() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("extend.aNot", "x");
        params.put("extend.bIsTrue", null);
        params.put("extend.cTrue", null);
        params.put("extend.dIsFalse", null);
        params.put("extend.eFalse", null);
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'a' != :extend_a")
                .contains("extend->>'b' = :extend_b")
                .contains("extend->>'c' = :extend_c")
                .contains("extend->>'d' = :extend_d")
                .contains("extend->>'e' = :extend_e");
        assertThat(condition.get("extend_b")).isEqualTo(true);
        assertThat(condition.get("extend_c")).isEqualTo(true);
        assertThat(condition.get("extend_d")).isEqualTo(false);
        assertThat(condition.get("extend_e")).isEqualTo(false);
    }

    @Test
    void queryJsonBuildsComparisonConditions() {
        Map<String, Object> params = Map.ofEntries(
                Map.entry("extend.aAfter", 1),
                Map.entry("extend.bGreaterThanEqual", 2),
                Map.entry("extend.cGTE", 3),
                Map.entry("extend.dGreaterThan", 4),
                Map.entry("extend.eGT", 5),
                Map.entry("extend.fBefore", 6),
                Map.entry("extend.gLessThanEqual", 7),
                Map.entry("extend.hLTE", 8),
                Map.entry("extend.iLessThan", 9),
                Map.entry("extend.jLT", 10),
                Map.entry("extend.kEqual", 11),
                Map.entry("extend.lEQ", 12));
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'a' > :")
                .contains("extend->>'b' >= :")
                .contains("extend->>'c' >= :")
                .contains("extend->>'d' > :")
                .contains("extend->>'e' > :")
                .contains("extend->>'f' < :")
                .contains("extend->>'g' <= :")
                .contains("extend->>'h' <= :")
                .contains("extend->>'i' < :")
                .contains("extend->>'j' < :")
                .contains("extend->>'k' = :")
                .contains("extend->>'l' = :");
    }

    @Test
    void queryJsonPicksLongestMatchingKeywordSuffix() {
        Map<String, Object> params = Map.of(
                "extend.ageGreaterThanEqual", 18,
                "extend.statusNotIn", "a,b");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);
        String sql = condition.toSql();

        assertThat(sql).contains("extend->>'age' >= :extend_age");
        assertThat(sql).contains("extend->>'status' NOT IN (:extend_status0, :extend_status1");
    }

    @Test
    void queryJsonBuildsDeepNestedPath() {
        Map<String, Object> params = Map.of("extend.address.cityEq", "SF");
        QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, null);

        assertThat(condition.toSql()).contains("extend->'address'->>'city' = :");
    }
}
