package com.plate.boot.commons.query;

import com.plate.boot.commons.exception.QueryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the QueryFragment class.
 */
@DisplayName("QueryFragment Tests")
class QueryFragmentTest {

    private QueryFragment queryFragment;

    @BeforeEach
    void setUp() {
        queryFragment = QueryFragment.from("users");
    }

    @Nested
    @DisplayName("Basic Query Construction")
    class BasicQueryConstruction {

        @Test
        @DisplayName("Should create QueryFragment with table")
        void shouldCreateQueryFragmentWithTable() {
            QueryFragment fragment = QueryFragment.from("users");
            assertThat(fragment).isNotNull();
        }

        @Test
        @DisplayName("Should add columns to SELECT clause")
        void shouldAddColumnsToSelectClause() {
            queryFragment.column("id", "name", "email");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("SELECT id,name,email FROM users");
        }

        @Test
        @DisplayName("Should use asterisk when no columns specified")
        void shouldUseAsteriskWhenNoColumnsSpecified() {
            String sql = queryFragment.querySql();
            assertThat(sql).contains("SELECT * FROM users");
        }

        @Test
        @DisplayName("Should add WHERE conditions")
        void shouldAddWhereConditions() {
            queryFragment.where("age > :age", "status = 'active'");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("WHERE age > :age AND status = 'active'");
        }

        @Test
        @DisplayName("Should add ORDER BY clauses")
        void shouldAddOrderByClauses() {
            queryFragment.orderBy("name ASC", "email DESC");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("ORDER BY name ASC,email DESC");
        }

        @Test
        @DisplayName("Should add GROUP BY clauses")
        void shouldAddGroupByClauses() {
            queryFragment.groupBy("category", "type");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("GROUP BY category,type");
        }
    }

    @Nested
    @DisplayName("Pagination and Limit")
    class PaginationAndLimit {

        @Test
        @DisplayName("Should set limit and offset")
        void shouldSetLimitAndOffset() {
            queryFragment.limit(10, 20);
            String sql = queryFragment.querySql();
            assertThat(sql).contains("LIMIT 10 OFFSET 20");
        }

        @Test
        @DisplayName("Should configure with Pageable")
        void shouldConfigureWithPageable() {
            Pageable pageable = PageRequest.of(2, 15, Sort.by("name").ascending());
            queryFragment.pageable(pageable);
            String sql = queryFragment.querySql();
            assertThat(sql).contains("ORDER BY name ASC");
            assertThat(sql).contains("LIMIT 15 OFFSET 30");
        }
    }

    @Nested
    @DisplayName("Conditional Methods")
    class ConditionalMethods {

        @Test
        @DisplayName("Should add IN condition")
        void shouldAddInCondition() {
            List<String> values = Arrays.asList("admin", "user");
            queryFragment.in("role", values);
            String sql = queryFragment.querySql();
            assertThat(sql).contains("role IN");
            // The exact key names may vary, so we're checking for the presence of the IN clause
        }

        @Test
        @DisplayName("Should add NOT IN condition")
        void shouldAddNotInCondition() {
            List<Integer> values = Arrays.asList(1, 2, 3);
            queryFragment.notIn("status", values);
            String sql = queryFragment.querySql();
            assertThat(sql).contains("status NOT IN");
        }

        @Test
        @DisplayName("Should add LIKE condition")
        void shouldAddLikeCondition() {
            queryFragment.like("name", "%john%");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("name LIKE :name");
            assertThat(queryFragment).containsEntry("name", "%john%");
        }

        @Test
        @DisplayName("Should add IS NULL condition")
        void shouldAddIsNullCondition() {
            queryFragment.isNull("email");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("email IS NULL");
        }

        @Test
        @DisplayName("Should add IS NOT NULL condition")
        void shouldAddIsNotNullCondition() {
            queryFragment.isNotNull("email");
            String sql = queryFragment.querySql();
            assertThat(sql).contains("email IS NOT NULL");
        }

        @Test
        @DisplayName("Should add BETWEEN condition")
        void shouldAddBetweenCondition() {
            queryFragment.between("age", 18, 65);
            String sql = queryFragment.querySql();
            assertThat(sql).contains("age BETWEEN");
        }
    }

    @Nested
    @DisplayName("SQL Generation")
    class SqlGeneration {

        @Test
        @DisplayName("Should generate complete SQL query")
        void shouldGenerateCompleteSqlQuery() {
            queryFragment.column("id", "name")
                    .where("age > :age")
                    .orderBy("name ASC")
                    .limit(10, 0);
            queryFragment.put("age", 18);

            String sql = queryFragment.querySql();
            assertThat(sql).isEqualTo("SELECT id,name FROM users WHERE age > :age ORDER BY name ASC LIMIT 10 OFFSET 0");
        }

        @Test
        @DisplayName("Should generate COUNT SQL query")
        void shouldGenerateCountSqlQuery() {
            queryFragment.where("status = 'active'");
            String countSql = queryFragment.countSql();
            assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT 1 FROM users WHERE status = 'active') t");
        }

        @Test
        @DisplayName("Should throw exception when query is null")
        void shouldThrowExceptionWhenQueryIsNull() {
            QueryFragment emptyFragment = QueryFragment.from();
            assertThatThrownBy(emptyFragment::querySql)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("This query is null");
        }

        @Test
        @DisplayName("Should throw exception when countSql is null")
        void shouldThrowExceptionWhenCountSqlIsNull() {
            QueryFragment emptyFragment = QueryFragment.from();
            assertThatThrownBy(emptyFragment::countSql)
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("This countSql is null");
        }
    }

    @Nested
    @DisplayName("Condition Class Tests")
    class ConditionClassTests {

        @Test
        @DisplayName("Should create Condition from Criteria")
        void shouldCreateConditionFromCriteria() {
            Criteria criteria = Criteria.where("name").is("John");
            QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);
            assertThat(condition).isNotNull();
            assertThat(condition.toSql()).isNotEmpty();
        }

        @Test
        @DisplayName("Should create Condition with prefix")
        void shouldCreateConditionWithPrefix() {
            Criteria criteria = Criteria.where("age").greaterThan(18);
            QueryFragment.Condition condition = QueryFragment.Condition.of(criteria, "user");
            assertThat(condition).isNotNull();
        }
    }
}