package com.plate.boot.commons.query;

import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.relational.core.query.Criteria;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link QueryFragment} (no Spring / container required).
 * Focus: SQL fragment assembly, pagination, and parameterised conditions
 * (user values must be bound, never concatenated).
 */
class QueryFragmentTest {

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
    void buildsBasicSelectSql() {
        String sql = QueryFragment.from("users")
                .column("id", "name")
                .where("active = :active")
                .orderBy("name ASC")
                .groupBy("tenant")
                .querySql();

        assertThat(sql).contains("SELECT id,name")
                .contains("FROM users")
                .contains("WHERE active = :active")
                .contains("GROUP BY tenant")
                .contains("ORDER BY name ASC")
                .contains("LIMIT 25 OFFSET 0");
    }

    @Test
    void querySqlWithoutFromThrows() {
        assertThatThrownBy(() -> QueryFragment.from().querySql())
                .isInstanceOf(QueryException.class);
    }

    @Test
    void countSqlWrapsSubQuery() {
        String sql = QueryFragment.from("users").where("active = :active").countSql();

        assertThat(sql).contains("SELECT COUNT(*) FROM (SELECT 1 FROM users WHERE active = :active) t");
    }

    @Test
    void countSqlWithoutFromThrows() {
        assertThatThrownBy(() -> QueryFragment.from().countSql())
                .isInstanceOf(QueryException.class);
    }

    @Test
    void emptyClausesRenderToDefaults() {
        QueryFragment f = QueryFragment.from("users");

        assertThat(f.columnSql()).isEqualTo("*");
        assertThat(f.whereSql()).isEmpty();
        assertThat(f.orderSql()).isEmpty();
        assertThat(f.groupSql()).isEmpty();
    }

    @Test
    void inBindsValuesAsParameters() {
        QueryFragment f = QueryFragment.from("users").in("status", List.of("active", "pending"));
        String where = f.whereSql();

        assertThat(where).contains("status IN (:").doesNotContain("active").doesNotContain("pending");
        assertThat(f.get("status0")).isEqualTo("active");
        assertThat(f.get("status1")).isEqualTo("pending");
    }

    @Test
    void inDoesNotConcatenateMaliciousValues() {
        String malicious = "x'); DROP TABLE t; --";
        QueryFragment f = QueryFragment.from("users").in("status", List.of(malicious));

        assertThat(f.whereSql()).doesNotContain(malicious);
        assertThat(f.get("status0")).isEqualTo(malicious);
    }

    @Test
    void notInBindsValuesAsParameters() {
        QueryFragment f = QueryFragment.from("users").notIn("status", List.of("banned"));
        String where = f.whereSql();

        assertThat(where).contains("status NOT IN (:").doesNotContain("banned");
        assertThat(f.get("status0")).isEqualTo("banned");
    }

    @Test
    void likeConditionsBindValue() {
        QueryFragment f = QueryFragment.from("users")
                .like("name", "John")
                .startingWith("code", "A")
                .endingWith("email", "com")
                .notLike("note", "spam");

        String where = f.whereSql();
        assertThat(where).contains("name LIKE :").contains("code LIKE :").contains("email LIKE :").contains("note NOT LIKE :");
        assertThat(where).doesNotContain("John").doesNotContain("spam");
    }

    @Test
    void betweenBindsBothBounds() {
        QueryFragment f = QueryFragment.from("users").between("age", 18, 65);

        assertThat(f.whereSql()).contains("age BETWEEN :age1 AND :age2");
        assertThat(f.get("age1")).isEqualTo(18);
        assertThat(f.get("age2")).isEqualTo(65);
    }

    @Test
    void nullComparisonsNeedNoBoundValue() {
        QueryFragment f = QueryFragment.from("users")
                .isNull("deleted_at")
                .isNotNull("verified_at")
                .isTrue("enabled")
                .isFalse("locked");

        String where = f.whereSql();
        assertThat(where).contains("deleted_at IS NULL")
                .contains("verified_at IS NOT NULL")
                .contains("enabled IS TRUE")
                .contains("locked IS FALSE");
        assertThat(f).doesNotContainKey("deleted_at");
    }

    @Test
    void comparisonOperatorsBindValue() {
        QueryFragment f = QueryFragment.from("users")
                .after("created_at", 100)
                .before("expires_at", 200)
                .greaterThanOrEqual("score", 1)
                .lessThanOrEqual("score", 9)
                .isEq("tenant", 42)
                .not("legacy", 0);

        String where = f.whereSql();
        assertThat(where).contains("created_at > :").contains("expires_at < :")
                .contains("score >= :").contains("score <= :")
                .contains("tenant = :").contains("legacy != :");
        assertThat(f.get("tenant")).isEqualTo(42);
    }

    @Test
    void tsBuildsFullTextSearchWithBoundParameter() {
        String malicious = "'; DROP TABLE t; --";
        QueryFragment f = QueryFragment.from("docs").ts("text_search", malicious);

        String sql = f.querySql();
        assertThat(sql).contains("TO_TSQUERY('chinese',:text_search)")
                .contains("text_search @@ text_search")
                .contains(":text_search");
        assertThat(sql).doesNotContain(malicious);
        assertThat(f.get("text_search")).isEqualTo(malicious);
    }

    @Test
    void pageableAppliesLimitOffsetAndSort() {
        QueryFragment f = QueryFragment.from("users")
                .pageable(PageRequest.of(2, 15, Sort.by(Sort.Order.desc("created_at"))));

        assertThat(f.getSize()).isEqualTo(15);
        assertThat(f.getOffset()).isEqualTo(30);
        assertThat(f.orderSql()).contains("created_at DESC");
    }

    @Test
    void conditionAddsSqlAndParametersFromCriteria() {
        Criteria criteria = Criteria.where("name").like("Bob").ignoreCase(true);
        QueryFragment f = QueryFragment.from("users").condition(QueryFragment.Condition.of(criteria));

        assertThat(f.whereSql()).contains("name LIKE :");
        assertThat(f.whereSql()).doesNotContain("Bob");
    }

    @Test
    void conditionWithPrefixQualifiesColumn() {
        Criteria criteria = Criteria.where("id").is(1);
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria, "u");
        String sql = condition.toSql();

        assertThat(sql).contains("u.id = :");
    }

    @Test
    void conditionalFactoryBuildsFromConditions() {
        Criteria criteria = Criteria.where("active").is(true);
        QueryFragment f = QueryFragment.conditional(QueryFragment.Condition.of(criteria));

        assertThat(f.whereSql()).contains("active = :");
    }

    @Test
    void conditionBindsBetweenBounds() {
        Criteria criteria = Criteria.where("age").between(18, 65);
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        assertThat(condition.toSql()).contains("age BETWEEN :age1 AND :age2");
        assertThat(condition.get("age1")).isEqualTo(18);
        assertThat(condition.get("age2")).isEqualTo(65);
    }

    @Test
    void conditionBindsInValuesAsParameters() {
        Criteria criteria = Criteria.where("status").in("active", "pending");
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        assertThat(condition.toSql()).contains("status IN (:").doesNotContain("active").doesNotContain("pending");
        assertThat(condition.get("status0")).isEqualTo("active");
        assertThat(condition.get("status1")).isEqualTo("pending");
    }

    @Test
    void conditionRendersNullAndBooleanComparatorsWithoutValues() {
        Criteria criteria = Criteria.where("a").isNull().and("b").isNotNull().and("c").isTrue().and("d").isFalse();
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        String sql = condition.toSql();
        assertThat(sql).contains("a IS NULL").contains("b IS NOT NULL").contains("c IS TRUE").contains("d IS FALSE");
        assertThat(condition).doesNotContainKeys("a", "b", "c", "d");
    }

    @Test
    void conditionRendersChainedCriteriaWithCombinator() {
        Criteria criteria = Criteria.where("a").is(1).and("b").is(2);
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        assertThat(condition.toSql()).contains("a = :a").contains(" AND ").contains("b = :b");
        assertThat(condition.get("a")).isEqualTo(1);
        assertThat(condition.get("b")).isEqualTo(2);
    }

    @Test
    void conditionWithPrefixQualifiesInColumn() {
        Criteria criteria = Criteria.where("status").in("a");
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria, "u");

        assertThat(condition.toSql()).contains("u.status IN (:u_status0");
        assertThat(condition.get("u_status0")).isEqualTo("a");
    }

    @Test
    void conditionSkipsBlankSql() {
        QueryFragment f = QueryFragment.from("users").condition(
                QueryFragment.Condition.of(Criteria.empty()));

        assertThat(f.whereSql()).isEmpty();
    }

    @Test
    void tsKeepsExistingColumns() {
        QueryFragment f = QueryFragment.from("docs").column("id").ts("text_search", "hi");

        assertThat(f.columnSql()).startsWith("id,TS_RANK_CD(").contains("AS rank");
        assertThat(f.querySql()).contains("SELECT id,TS_RANK_CD(");
    }

    @Test
    void nullLikeValueAddsNoCondition() {
        QueryFragment f = QueryFragment.from("users").like("name", null);

        assertThat(f.whereSql()).isEmpty();
    }

    @Test
    void conditionBindsNotBetweenBounds() {
        Criteria criteria = Criteria.where("age").notBetween(18, 65);
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        assertThat(condition.toSql()).contains("age NOT BETWEEN :age1 AND :age2");
        assertThat(condition.get("age1")).isEqualTo(18);
        assertThat(condition.get("age2")).isEqualTo(65);
    }

    @Test
    void conditionBindsNotInValues() {
        Criteria criteria = Criteria.where("status").notIn("a", "b");
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        assertThat(condition.toSql()).contains("status NOT IN (:status0, :status1");
        assertThat(condition.get("status0")).isEqualTo("a");
        assertThat(condition.get("status1")).isEqualTo("b");
    }

    @Test
    void conditionHandlesEmptyInCollection() {
        Criteria criteria = Criteria.where("status").in(java.util.Collections.emptyList());
        QueryFragment.Condition condition = QueryFragment.Condition.of(criteria);

        assertThat(condition.toSql()).contains("status IN ()");
    }

    @Test
    void ofMapInitializesFragmentWithoutRecursion() {
        QueryFragment fragment = QueryFragment.of(Map.of("name", "alice"));

        assertThat(fragment).containsEntry("name", "alice");
    }

    @Test
    void ofSizeOffsetMapInitializesFragmentWithoutRecursion() {
        QueryFragment fragment = QueryFragment.of(10, 5, Map.of("name", "bob"));

        assertThat(fragment).containsEntry("name", "bob");
    }
}
