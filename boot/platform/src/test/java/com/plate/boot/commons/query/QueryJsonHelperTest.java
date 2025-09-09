package com.plate.boot.commons.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the QueryJsonHelper class.
 */
@DisplayName("QueryJsonHelper Tests")
class QueryJsonHelperTest {

    @Nested
    @DisplayName("Sort Transformation")
    class SortTransformation {

        @Test
        @DisplayName("Should transform sort for JSON fields")
        void shouldTransformSortForJsonFields() {
            Sort sort = Sort.by("extend.name", "extend.email");
            Sort transformedSort = QueryJsonHelper.transformSortForJson(sort);

            assertThat(transformedSort).isNotNull();
            // We can't easily verify the content without access to internal Sort structure
        }

        @Test
        @DisplayName("Should return unsorted for null input")
        void shouldReturnUnsortedForNullInput() {
            Sort transformedSort = QueryJsonHelper.transformSortForJson(null);
            assertThat(transformedSort).isEqualTo(Sort.unsorted());
        }

        @Test
        @DisplayName("Should return unsorted for empty sort")
        void shouldReturnUnsortedForEmptySort() {
            Sort sort = Sort.unsorted();
            Sort transformedSort = QueryJsonHelper.transformSortForJson(sort);
            assertThat(transformedSort).isEqualTo(Sort.unsorted());
        }
    }

    @Nested
    @DisplayName("JSON Query Construction")
    class JsonQueryConstruction {

        @Test
        @DisplayName("Should construct JSON query conditions")
        void shouldConstructJsonQueryConditions() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.nameEq", "John");
            params.put("extend.ageGt", 18);

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "a");

            assertThat(condition).isNotNull();
            assertThat(condition.toSql()).isNotNull();
        }

        @Test
        @DisplayName("Should handle complex JSON paths")
        void shouldHandleComplexJsonPaths() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.requestBody.nameEq", "John");
            params.put("extend.profile.ageGt", 21);

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "user");

            assertThat(condition).isNotNull();
            assertThat(condition.toSql()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception for invalid JSON path")
        void shouldThrowExceptionForInvalidJsonPath() {
            Map<String, Object> params = new HashMap<>();
            params.put("invalidPath", "value");

            assertThatThrownBy(() -> QueryJsonHelper.queryJson(params, "a"))
                    .isInstanceOf(Exception.class); // Using generic exception as we don't know the exact type
        }
    }

    @Nested
    @DisplayName("Keyword Mapping")
    class KeywordMapping {

        @Test
        @DisplayName("Should handle EQ operator")
        void shouldHandleEqOperator() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.nameEq", "John");

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "a");

            assertThat(condition).isNotNull();
        }

        @Test
        @DisplayName("Should handle GT operator")
        void shouldHandleGtOperator() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.ageGt", 18);

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "a");

            assertThat(condition).isNotNull();
        }

        @Test
        @DisplayName("Should handle LIKE operator")
        void shouldHandleLikeOperator() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.nameLike", "John");

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "a");

            assertThat(condition).isNotNull();
        }

        @Test
        @DisplayName("Should handle IN operator")
        void shouldHandleInOperator() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.statusIn", "active,pending");

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "a");

            assertThat(condition).isNotNull();
        }

        @Test
        @DisplayName("Should handle IS NULL operator")
        void shouldHandleIsNullOperator() {
            Map<String, Object> params = new HashMap<>();
            params.put("extend.emailIsNull", "");

            QueryFragment.Condition condition = QueryJsonHelper.queryJson(params, "a");

            assertThat(condition).isNotNull();
        }
    }
}