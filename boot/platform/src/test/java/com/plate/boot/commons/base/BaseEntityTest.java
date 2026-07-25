package com.plate.boot.commons.base;

import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void criteriaReturnsNonNullCriteria() {
        TestEntity entity = new TestEntity();

        assertThat(entity.criteria(List.of())).isNotNull();
    }

    @Test
    void queryBuildsSqlFromTableName() {
        TestEntity entity = new TestEntity();

        String sql = entity.query(List.of()).querySql();

        assertThat(sql).contains("FROM test_entity");
        assertThat(sql).contains("LIMIT 25 OFFSET 0");
    }
}
