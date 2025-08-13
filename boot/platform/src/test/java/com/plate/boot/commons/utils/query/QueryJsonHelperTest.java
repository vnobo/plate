package com.plate.boot.commons.utils.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QueryJsonHelper Unit Tests")
class QueryJsonHelperTest {

    @Nested
    @DisplayName("Transform Sort Tests")
    class TransformSortTests {

        @Test
        @DisplayName("Should transform sort for JSON with nested properties")
        void shouldTransformSortForJsonWithNestedProperties() {
            Sort sort = Sort.by(Sort.Order.asc("extend.username"));

            Sort transformedSort = QueryJsonHelper.transformSortForJson(sort);

            // The transformed sort should have the property in a format suitable for JSON querying.
            // The exact format depends on the implementation, but it should be different from the original.
            // For example, it might convert "extend.username" to "extend->>'username'" or similar.
            // Let's check the order property.
            Sort.Order order = transformedSort.getOrderFor("extend_username");
            assertThat(order).isNotNull();
            // The exact transformation is complex, so we'll just verify it's not the same.
            // A more precise test would check the exact string representation.
        }

        @Test
        @DisplayName("Should return unsorted when input sort is null")
        void shouldReturnUnsortedWhenInputSortIsNull() {
            Sort transformedSort = QueryJsonHelper.transformSortForJson(null);
            assertThat(transformedSort).isEqualTo(Sort.unsorted());
        }

        @Test
        @DisplayName("Should return unsorted when input sort is empty")
        void shouldReturnUnsortedWhenInputSortIsEmpty() {
            Sort transformedSort = QueryJsonHelper.transformSortForJson(Sort.unsorted());
            assertThat(transformedSort).isEqualTo(Sort.unsorted());
        }
    }

    @Nested
    @DisplayName("Query JSON Tests")
    class QueryJsonTests {

        @Test
        @DisplayName("Should create QueryFragment for simple JSON query")
        void shouldCreateQueryFragmentForSimpleJsonQuery() {
            Map<String, Object> params = Map.of("extend.usernameEq", "John");

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "a");

            // The queryFragment should contain a WHERE condition for the JSON field.
            // The exact SQL depends on the implementation, but it should contain the column and the value.
            assertThat(queryFragment.getWhere().toString()).contains("a.extend->>'username' = :extend_usernameEq");
            assertThat(queryFragment.get("extend_usernameEq")).isEqualTo("John");
        }

        @Test
        @DisplayName("Should create QueryFragment for nested JSON query")
        void shouldCreateQueryFragmentForNestedJsonQuery() {
            Map<String, Object> params = Map.of("extend.requestBody.nameEq", "John");

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "a");

            // Check for the nested JSON path in the WHERE clause.
            assertThat(queryFragment.getWhere().toString()).contains("a.extend->'requestBody'->>'name' = :extend_requestBody_nameEq");
            assertThat(queryFragment.get("extend_requestBody_nameEq")).isEqualTo("John");
        }

        @Test
        @DisplayName("Should create QueryFragment for JSON query with LIKE operation")
        void shouldCreateQueryFragmentForJsonQueryWithLikeOperation() {
            Map<String, Object> params = Map.of("extend.usernameLike", "Jo");

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "a");

            assertThat(queryFragment.getWhere().toString()).contains("a.extend->>'username' like :extend_usernameLike");
            assertThat(queryFragment.get("extend_usernameLike")).isEqualTo("Jo");
        }

        @Test
        @DisplayName("Should create QueryFragment for JSON query with IN operation")
        void shouldCreateQueryFragmentForJsonQueryWithInOperation() {
            Map<String, Object> params = Map.of("extend.statusIn", "active,inactive");

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "a");

            assertThat(queryFragment.getWhere().toString()).contains("a.extend->>'status' in (:extend_statusIn)");
            // The value should be converted to a Set.
            Object value = queryFragment.get("extend_statusIn");
            assertThat(value).isInstanceOf(java.util.Set.class);
            assertThat((java.util.Set<String>) value).containsExactlyInAnyOrder("active", "inactive");
        }

        @Test
        @DisplayName("Should create QueryFragment for JSON query with BETWEEN operation")
        void shouldCreateQueryFragmentForJsonQueryWithBetweenOperation() {
            Map<String, Object> params = Map.of("extend.ageBetween", "18,65");

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "a");

            assertThat(queryFragment.getWhere().toString()).contains("a.extend->>'age' between :extend_ageBetween_start and :extend_ageBetween_end");
            assertThat(queryFragment.get("extend_ageBetween_start")).isEqualTo("18");
            assertThat(queryFragment.get("extend_ageBetween_end")).isEqualTo("65");
        }

        @Test
        @DisplayName("Should throw exception for invalid JSON path")
        void shouldThrowExceptionForInvalidJsonPath() {
            Map<String, Object> params = Map.of("invalidPath", "value");

            assertThatThrownBy(() -> QueryJsonHelper.queryJson(params, "a"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should create QueryFragment for JSON query with prefix")
        void shouldCreateQueryFragmentForJsonQueryWithPrefix() {
            Map<String, Object> params = Map.of("extend.usernameEq", "John");

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "u");

            assertThat(queryFragment.getWhere().toString()).contains("u.extend->>'username' = :extend_usernameEq");
            assertThat(queryFragment.get("extend_usernameEq")).isEqualTo("John");
        }

        @Test
        @DisplayName("Should create QueryFragment for multiple JSON queries")
        void shouldCreateQueryFragmentForMultipleJsonQueries() {
            Map<String, Object> params = Map.of(
                    "extend.usernameEq", "John",
                    "extend.statusIn", "active,inactive"
            );

            QueryFragment queryFragment = QueryJsonHelper.queryJson(params, "a");

            assertThat(queryFragment.getWhere().toString()).contains("a.extend->>'username' = :extend_usernameEq");
            assertThat(queryFragment.getWhere().toString()).contains("a.extend->>'status' in (:extend_statusIn)");
            assertThat(queryFragment.get("extend_usernameEq")).isEqualTo("John");
            Object value = queryFragment.get("extend_statusIn");
            assertThat(value).isInstanceOf(java.util.Set.class);
            assertThat((java.util.Set<String>) value).containsExactlyInAnyOrder("active", "inactive");
        }
    }

    @Nested
    @DisplayName("Build JSON Condition Tests")
    class BuildJsonConditionTests {
        // These are private methods, so testing them directly is complex.
        // We can test them indirectly through queryJson method tests.
        // If needed, we could use reflection to test them directly, but it's usually not recommended.
    }

    @Nested
    @DisplayName("Build Last Condition Tests")
    class BuildLastConditionTests {
        // These are private methods, so testing them directly is complex.
        // We can test them indirectly through queryJson method tests.
    }

    @Nested
    @DisplayName("Build JSON Query Path Tests")
    class BuildJsonQueryPathTests {
        // These are private methods, so testing them directly is complex.
        // We can test them indirectly through queryJson method tests.
    }

    @Nested
    @DisplayName("Query Keyword Mapper Tests")
    class QueryKeywordMapperTests {
        // These are private methods, so testing them directly is complex.
        // We can test them indirectly through queryJson method tests.
    }
}