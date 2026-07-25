package com.plate.boot.commons.query;

import com.plate.boot.commons.exception.QueryException;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Criteria;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link QueryHelper} (no Spring / container required).
 * Focus: table-name resolution, criteria construction, and verifying that user
 * values are bound as parameters rather than concatenated into raw SQL.
 */
class QueryHelperTest {

    @Table("my_table")
    static class AnnotatedTable {
    }

    @Table("")
    static class EmptyTable {
    }

    static class NoTable {
    }

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
    void annotationTableNameUsesTableValue() {
        assertThat(QueryHelper.annotationTableName(new AnnotatedTable())).isEqualTo("my_table");
    }

    @Test
    void annotationTableNameFallsBackToSnakeCaseWhenValueEmpty() {
        assertThat(QueryHelper.annotationTableName(new EmptyTable())).isEqualTo("empty_table");
    }

    @Test
    void annotationTableNameThrowsForNull() {
        assertThatThrownBy(() -> QueryHelper.annotationTableName(null))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void annotationTableNameThrowsWhenNoAnnotation() {
        assertThatThrownBy(() -> QueryHelper.annotationTableName(new java.util.HashMap<>()))
                .isInstanceOf(RestServerException.class);
    }

    @Test
    void criteriaFromEmptyMapIsEmpty() {
        Criteria criteria = QueryHelper.criteria(Map.of());

        assertThat(criteria).isEqualTo(Criteria.empty());
        assertThat(QueryFragment.Condition.of(criteria).toSql()).isEmpty();
    }

    @Test
    void criteriaBindsUuidStringAndCollectionAsParameters() {
        UUID id = UUID.randomUUID();
        Map<String, Object> map = Map.of(
                "id", id,
                "name", "John",
                "status", List.of("active", "pending"));

        Criteria criteria = QueryHelper.criteria(map);
        String sql = QueryFragment.Condition.of(criteria).toSql();

        // The dynamically-built SQL references bound parameters, never the raw values.
        assertThat(sql).contains("id = :").contains("name LIKE :").contains("status IN (:");
        assertThat(sql).doesNotContain(id.toString());
        assertThat(sql).doesNotContain("John");
        assertThat(sql).doesNotContain("active");
        assertThat(sql).doesNotContain("pending");
    }

    @Test
    void criteriaDoesNotConcatenateMaliciousUserInput() {
        String malicious = "'; DROP TABLE users; --";
        Map<String, Object> map = Map.of("name", malicious);

        Criteria criteria = QueryHelper.criteria(map);
        String sql = QueryFragment.Condition.of(criteria).toSql();

        assertThat(sql).contains("name LIKE :");
        assertThat(sql).doesNotContain(malicious);
    }

    @Test
    void criteriaFromObjectWithSkipKeysExcludesSkippedAndNullFields() {
        SampleEntity entity = new SampleEntity();
        entity.setName("Bob");
        entity.setCode(UUID.randomUUID());

        Criteria criteria = QueryHelper.criteria(entity, Set.of("search"));
        String sql = QueryFragment.Condition.of(criteria).toSql();

        // name and code participate; the skip-key "search" and null "extend" are excluded.
        assertThat(sql).contains("name LIKE :").contains("code = :");
        assertThat(sql).doesNotContain("search");
        assertThat(sql).doesNotContain("extend");
    }

    @Table("sample_entity")
    static class SampleEntity {
        private UUID code;
        private String name;
        private String search;

        public UUID getCode() {
            return code;
        }

        public void setCode(UUID code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }
    }
}
