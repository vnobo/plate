package com.plate.boot.commons.utils.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QueryFragment Unit Tests")
class QueryFragmentTest {

    @Nested
    @DisplayName("Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("Should create QueryFragment with new instance")
        void shouldCreateQueryFragmentWithNewInstance() {
            QueryFragment queryFragment = QueryFragment.withNew();
            assertThat(queryFragment).isNotNull();
            assertThat(queryFragment.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should create QueryFragment with columns")
        void shouldCreateQueryFragmentWithColumns() {
            QueryFragment queryFragment = QueryFragment.withColumns("id", "name");
            assertThat(queryFragment.getColumns().toString()).contains("id", "name");
        }

        @Test
        @DisplayName("Should create QueryFragment with map")
        void shouldCreateQueryFragmentWithMap() {
            Map<String, Object> params = Map.of("name", "John", "age", 30);
            QueryFragment queryFragment = QueryFragment.withMap(params);
            assertThat(queryFragment.get("name")).isEqualTo("John");
            assertThat(queryFragment.get("age")).isEqualTo(30);
        }

        @Test
        @DisplayName("Should create QueryFragment with map and pagination")
        void shouldCreateQueryFragmentWithMapAndPagination() {
            Map<String, Object> params = Map.of("name", "John");
            QueryFragment queryFragment = QueryFragment.withMap(10, 5, params);
            assertThat(queryFragment.get("name")).isEqualTo("John");
            assertThat(queryFragment.getSize()).isEqualTo(10);
            assertThat(queryFragment.getOffset()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should create QueryFragment from another QueryFragment")
        void shouldCreateQueryFragmentFromAnotherQueryFragment() {
            QueryFragment original = QueryFragment.withColumns("id").from("users").where("age > :age");
            original.put("age", 18);
            QueryFragment copy = QueryFragment.of(original);
            assertThat(copy.getColumns().toString()).contains("id");
            assertThat(copy.getFrom().toString()).contains("users");
            assertThat(copy.getWhere().toString()).contains("age > :age");
            assertThat(copy.get("age")).isEqualTo(18);
        }

        @Test
        @DisplayName("Should create QueryFragment from another QueryFragment with pagination")
        void shouldCreateQueryFragmentFromAnotherQueryFragmentWithPagination() {
            QueryFragment original = QueryFragment.withNew();
            original.put("name", "John");
            QueryFragment copy = QueryFragment.of(5, 10, original);
            assertThat(copy.get("name")).isEqualTo("John");
            assertThat(copy.getSize()).isEqualTo(5);
            assertThat(copy.getOffset()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should add columns to QueryFragment")
        void shouldAddColumnsToQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().columns("id", "name", "email");
            assertThat(queryFragment.getColumns().toString()).contains("id", "name", "email");
        }

        @Test
        @DisplayName("Should add from clause to QueryFragment")
        void shouldAddFromClauseToQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().from("users", "orders");
            assertThat(queryFragment.getFrom().toString()).contains("users", "orders");
        }

        @Test
        @DisplayName("Should add where conditions to QueryFragment")
        void shouldAddWhereConditionsToQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().where("age > :age").where("name LIKE :name");
            assertThat(queryFragment.getWhere().toString()).contains("age > :age", "name LIKE :name");
        }

        @Test
        @DisplayName("Should add order by clauses to QueryFragment")
        void shouldAddOrderByClausesToQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().orderBy("name ASC").orderBy("email DESC");
            assertThat(queryFragment.getOrderBy().toString()).contains("name ASC", "email DESC");
        }

        @Test
        @DisplayName("Should add group by columns to QueryFragment")
        void shouldAddGroupByColumnsToQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().groupBy("category", "type");
            assertThat(queryFragment.getGroupBy().toString()).contains("category", "type");
        }

        @Test
        @DisplayName("Should set limit and offset for QueryFragment")
        void shouldSetLimitAndOffsetForQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().limit(10, 5);
            assertThat(queryFragment.getSize()).isEqualTo(10);
            assertThat(queryFragment.getOffset()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should add full-text search condition to QueryFragment")
        void shouldAddFullTextSearchConditionToQueryFragment() {
            QueryFragment queryFragment = QueryFragment.withNew().ts("description", "search term");
            // Verify that the columns, from, and where clauses are updated
            assertThat(queryFragment.getColumns().toString()).contains("TS_RANK_CD");
            assertThat(queryFragment.getFrom().toString()).contains("TO_TSQUERY");
            assertThat(queryFragment.getWhere().toString()).contains("@@");
            assertThat(queryFragment.get("description")).isEqualTo("search term");
        }

        @Test
        @DisplayName("Should chain multiple builder methods")
        void shouldChainMultipleBuilderMethods() {
            QueryFragment queryFragment = QueryFragment.withNew()
                    .columns("id", "name")
                    .from("users")
                    .where("age > :age")
                    .orderBy("name ASC")
                    .groupBy("category")
                    .limit(5, 10);
            queryFragment.put("age", 18);

            assertThat(queryFragment.getColumns().toString()).contains("id", "name");
            assertThat(queryFragment.getFrom().toString()).contains("users");
            assertThat(queryFragment.getWhere().toString()).contains("age > :age");
            assertThat(queryFragment.getOrderBy().toString()).contains("name ASC");
            assertThat(queryFragment.getGroupBy().toString()).contains("category");
            assertThat(queryFragment.getSize()).isEqualTo(5);
            assertThat(queryFragment.getOffset()).isEqualTo(10);
            assertThat(queryFragment.get("age")).isEqualTo(18);
        }
    }

    @Nested
    @DisplayName("SQL Generation Tests")
    class SqlGenerationTests {

        @Test
        @DisplayName("Should generate WHERE clause")
        void shouldGenerateWhereClause() {
            QueryFragment queryFragment = QueryFragment.withNew().where("age > :age").where("name LIKE :name");
            String whereSql = queryFragment.whereSql();
            assertThat(whereSql).isEqualTo(" WHERE age > :age AND name LIKE :name");
        }

        @Test
        @DisplayName("Should generate empty WHERE clause when no conditions")
        void shouldGenerateEmptyWhereClauseWhenNoConditions() {
            QueryFragment queryFragment = QueryFragment.withNew();
            String whereSql = queryFragment.whereSql();
            assertThat(whereSql).isEqualTo("");
        }

        @Test
        @DisplayName("Should generate ORDER BY clause")
        void shouldGenerateOrderByClause() {
            QueryFragment queryFragment = QueryFragment.withNew().orderBy("name ASC").orderBy("email DESC");
            String orderSql = queryFragment.orderSql();
            assertThat(orderSql).isEqualTo(" ORDER BY name ASC,email DESC");
        }

        @Test
        @DisplayName("Should generate empty ORDER BY clause when no orders")
        void shouldGenerateEmptyOrderByClauseWhenNoOrders() {
            QueryFragment queryFragment = QueryFragment.withNew();
            String orderSql = queryFragment.orderSql();
            assertThat(orderSql).isEqualTo("");
        }

        @Test
        @DisplayName("Should generate GROUP BY clause")
        void shouldGenerateGroupByClause() {
            QueryFragment queryFragment = QueryFragment.withNew().groupBy("category", "type");
            String groupSql = queryFragment.groupSql();
            assertThat(groupSql).isEqualTo(" GROUP BY category,type");
        }

        @Test
        @DisplayName("Should generate empty GROUP BY clause when no group by columns")
        void shouldGenerateEmptyGroupByClauseWhenNoGroupByColumns() {
            QueryFragment queryFragment = QueryFragment.withNew();
            String groupSql = queryFragment.groupSql();
            assertThat(groupSql).isEqualTo("");
        }

        @Test
        @DisplayName("Should generate query SQL")
        void shouldGenerateQuerySql() {
            QueryFragment queryFragment = QueryFragment.withNew()
                    .columns("id", "name")
                    .from("users")
                    .where("age > :age")
                    .orderBy("name ASC")
                    .groupBy("category")
                    .limit(5, 10);
            queryFragment.put("age", 18);

            String querySql = queryFragment.querySql();
            assertThat(querySql).isEqualTo("SELECT id,name FROM users  WHERE age > :age  ORDER BY name ASC  GROUP BY category LIMIT 5 OFFSET 10");
        }

        @Test
        @DisplayName("Should throw exception when generating query SQL without from clause")
        void shouldThrowExceptionWhenGeneratingQuerySqlWithoutFromClause() {
            QueryFragment queryFragment = QueryFragment.withNew().columns("id", "name");
            assertThatThrownBy(queryFragment::querySql)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("This querySql is null");
        }

        @Test
        @DisplayName("Should generate count SQL")
        void shouldGenerateCountSql() {
            QueryFragment queryFragment = QueryFragment.withNew()
                    .columns("id", "name")
                    .from("users")
                    .where("age > :age");
            queryFragment.put("age", 18);

            String countSql = queryFragment.countSql();
            assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT id,name FROM users WHERE age > :age) t");
        }

        @Test
        @DisplayName("Should throw exception when generating count SQL without from clause")
        void shouldThrowExceptionWhenGeneratingCountSqlWithoutFromClause() {
            QueryFragment queryFragment = QueryFragment.withNew().columns("id", "name");
            assertThatThrownBy(queryFragment::countSql)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("This countSql is null");
        }
    }

    @Nested
    @DisplayName("Parameter Handling Tests")
    class ParameterHandlingTests {

        @Test
        @DisplayName("Should put single parameter")
        void shouldPutSingleParameter() {
            QueryFragment queryFragment = QueryFragment.withNew();
            queryFragment.put("name", "John");
            assertThat(queryFragment.get("name")).isEqualTo("John");
        }

        @Test
        @DisplayName("Should put multiple parameters")
        void shouldPutMultipleParameters() {
            QueryFragment queryFragment = QueryFragment.withNew();
            queryFragment.put("name", "John");
            queryFragment.put("age", 30);
            assertThat(queryFragment.get("name")).isEqualTo("John");
            assertThat(queryFragment.get("age")).isEqualTo(30);
        }

        @Test
        @DisplayName("Should put all parameters from map")
        void shouldPutAllParametersFromMap() {
            QueryFragment queryFragment = QueryFragment.withNew();
            Map<String, Object> params = Map.of("name", "John", "age", 30);
            queryFragment.putAll(params);
            assertThat(queryFragment.get("name")).isEqualTo("John");
            assertThat(queryFragment.get("age")).isEqualTo(30);
        }
    }
}