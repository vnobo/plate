package com.plate.boot.commons.base;

import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the pure logic in {@link AbstractEntity} (no Spring / container required).
 * {@code isNew()} assigns a UUIDv7 code when absent; identity is based on the {@code id} field.
 */
class AbstractEntityTest {

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
    void isNewAssignsUuidV7WhenCodeAbsent() {
        TestEntity entity = new TestEntity();

        boolean isNew = entity.isNew();

        assertThat(isNew).isTrue();
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
    void equalsAndHashCodeBasedOnId() {
        TestEntity a = new TestEntity();
        a.setId(UUID.randomUUID());
        TestEntity b = new TestEntity();
        b.setId(a.getId());
        TestEntity c = new TestEntity();
        c.setId(UUID.randomUUID());

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
    }

    @Test
    void versionDefaultsToNull() {
        assertThat(new TestEntity().getVersion()).isNull();
    }

    @Test
    void queryDefaultsToNull() {
        assertThat(new TestEntity().getQuery()).isNull();
    }
}
