package com.plate.boot.commons.base;

import com.plate.boot.commons.query.QueryFragment;
import com.plate.boot.commons.utils.DatabaseUtils;
import org.junit.jupiter.api.Test;
import org.springframework.data.core.TypeInformation;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the default methods declared on {@link BaseEntity} (no Spring / container required).
 * {@code criteria(...)} and {@code query(...)} build Spring {@code Criteria}/{@code QueryFragment}
 * purely from the entity's bean properties and its {@code @Table} name.
 */
class BaseEntityTest {

    @Table("test_entity")
    static class TestEntity extends AbstractEntity<UUID> {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Table("bare_entity")
    static class BareEntity implements BaseEntity<UUID> {
        @Override
        public UUID getId() {
            return null;
        }
    }

    @Test
    void criteriaReturnsNonNullCriteria() {
        TestEntity entity = new TestEntity();

        assertThat(entity.criteria(List.of())).isNotNull();
    }

    @Test
    void isNewAssignsCodeAndReturnsTrueWhenIdAbsent() {
        TestEntity entity = new TestEntity();

        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getCode()).isNotNull();
        assertThat(entity.getCode().version()).isEqualTo(7);
    }

    @Test
    void isNewReturnsFalseWhenIdPresent() {
        TestEntity entity = new TestEntity();
        entity.setId(UUID.randomUUID());

        assertThat(entity.isNew()).isFalse();
    }

    @Test
    void isNewPreservesExistingCodeWhenIdAbsent() {
        TestEntity entity = new TestEntity();
        UUID existing = UUID.randomUUID();
        entity.setCode(existing);

        assertThat(entity.isNew()).isTrue();
        assertThat(entity.getCode()).isEqualTo(existing);
    }

    @Test
    void defaultAccessorsReturnNullAndSetCodeIsNoOp() {
        BareEntity entity = new BareEntity();

        assertThat((Object) entity.getCode()).isNull();
        assertThat(entity.getQuery()).isNull();
        assertThat(entity.getSearch()).isNull();
        assertThat(entity.getSecurityCode()).isNull();

        entity.setCode(UUID.randomUUID());
        assertThat((Object) entity.getCode()).isNull();
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void queryBuildsSqlFromTableName() {
        TestEntity entity = new TestEntity();

        String sql = entity.query(List.of()).querySql();

        assertThat(sql).contains("FROM test_entity");
        assertThat(sql).contains("LIMIT 25 OFFSET 0");
    }

    @Test
    void queryWithoutArgsBehavesLikeEmptySkipKeys() {
        TestEntity entity = new TestEntity();

        assertThat(entity.query().querySql()).isEqualTo(entity.query(List.of()).querySql());
    }

    @Test
    void queryWithSearchAddsFullTextSearchClause() {
        TestEntity entity = new TestEntity();
        entity.setSearch("hello world");

        QueryFragment fragment = entity.query();

        assertThat(fragment.querySql()).contains("ts_text_search @@ text_search")
                .contains("TO_TSQUERY('chinese',:text_search)");
        assertThat(fragment.get("text_search")).isEqualTo("hello world");
    }

    @Test
    void queryWithSecurityCodeFiltersByTenantCode() {
        TestEntity entity = new TestEntity();
        UUID code = UUID.randomUUID();
        entity.setSecurityCode(code);

        QueryFragment fragment = entity.query();

        assertThat(fragment.whereSql()).contains("tenant_code = :tenantCode");
        assertThat(fragment.get("tenantCode")).isEqualTo(code);
    }

    @Test
    void queryWithJsonQueryBuildsJsonCondition() {
        R2dbcConverter saved = DatabaseUtils.R2DBC_CONVERTER;
        R2dbcConverter stub = mock(R2dbcConverter.class);
        when(stub.writeValue(any(), any(TypeInformation.class))).thenAnswer(inv -> inv.getArgument(0));
        DatabaseUtils.R2DBC_CONVERTER = stub;
        try {
            TestEntity entity = new TestEntity();
            entity.setQuery(Map.of("extend.nameEq", "John"));

            QueryFragment fragment = entity.query();

            assertThat(fragment.whereSql()).contains("extend->>'name' = :extend_name");
            assertThat(fragment.get("extend_name")).isEqualTo("John");
        } finally {
            DatabaseUtils.R2DBC_CONVERTER = saved;
        }
    }
}
