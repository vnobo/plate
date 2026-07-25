package com.plate.boot.commons.utils;

import com.plate.boot.commons.exception.JsonPointerException;
import com.plate.boot.commons.exception.RestServerException;
import com.plate.boot.security.core.UserAuditor;
import com.plate.boot.security.core.UserAuditorAware;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BeanUtils} (no Spring / container required).
 * Only the pure helpers are exercised; {@code serializeUserAuditor} is tested with a
 * mocked {@link UserAuditorAware} for the auditor-resolution branch.
 */
class BeanUtilsTest {

    private static JsonMapper savedMapper;
    private static UserAuditorAware savedAuditorAware;

    @BeforeAll
    static void setUp() {
        savedMapper = ContextUtils.OBJECT_MAPPER;
        ContextUtils.OBJECT_MAPPER = JsonMapper.builder().build();
        savedAuditorAware = BeanUtils.USER_AUDITOR_AWARE;
    }

    @AfterAll
    static void tearDown() {
        ContextUtils.OBJECT_MAPPER = savedMapper;
        BeanUtils.USER_AUDITOR_AWARE = savedAuditorAware;
    }

    @Test
    void objectToBytesRoundTripsViaMapper() throws Exception {
        Person person = new Person("Alice", 30, "Alice A");
        byte[] bytes = BeanUtils.objectToBytes(person);

        assertThat(bytes).isNotEmpty();
        Person back = ContextUtils.OBJECT_MAPPER.readValue(bytes, Person.class);
        assertThat(back).isEqualTo(person);
    }

    @Test
    void beanToMapIncludesAllProperties() {
        Person person = new Person("Alice", 30, "Alice A");

        Map<String, Object> map = BeanUtils.beanToMap(person);

        assertThat(map).containsEntry("name", "Alice").containsEntry("age", 30);
    }

    @Test
    void beanToMapConvertsKeysToSnakeCase() {
        Person person = new Person("Alice", 30, "Alice A");

        Map<String, Object> map = BeanUtils.beanToMap(person, true, false);

        assertThat(map).containsKey("full_name");
    }

    @Test
    void beanToMapIgnoresNullValuesWhenRequested() {
        Person person = new Person("Alice", null, null);

        Map<String, Object> map = BeanUtils.beanToMap(person, false, true);

        assertThat(map).containsKey("name").doesNotContainKey("age").doesNotContainKey("fullName");
    }

    @Test
    void beanToMapReturnsNullForNullBean() {
        assertThat(BeanUtils.beanToMap(null)).isNull();
    }

    @Test
    void copyPropertiesIntoTargetClass() {
        Person person = new Person("Bob", 25, "Bob B");

        PersonDto dto = BeanUtils.copyProperties(person, PersonDto.class);

        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getAge()).isEqualTo(25);
        assertThat(dto.getFullName()).isEqualTo("Bob B");
    }

    @Test
    void copyPropertiesIntoTargetObject() {
        Person person = new Person("Bob", 25, "Bob B");
        PersonDto dto = new PersonDto();

        BeanUtils.copyProperties(person, dto);

        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getAge()).isEqualTo(25);
    }

    @Test
    void copyPropertiesIgnoresNullSourceValues() {
        Person person = new Person(null, null, null);
        PersonDto dto = new PersonDto();
        dto.setName("Keep");
        dto.setAge(99);

        BeanUtils.copyProperties(person, dto, true);

        assertThat(dto.getName()).isEqualTo("Keep");
        assertThat(dto.getAge()).isEqualTo(99);
    }

    @Test
    void cacheKeyForBeanContainsPropertyEntries() {
        Person person = new Person("Alice", 30, "Alice A");

        String key = BeanUtils.cacheKey(person);

        assertThat(key).contains("name=Alice").contains("age=30");
    }

    @Test
    void cacheKeyForPageableContainsPageAndSort() {
        String key = BeanUtils.cacheKey(PageRequest.of(0, 10, Sort.by("name").ascending()));

        assertThat(key).contains("0_10").contains("name_ASC");
    }

    @Test
    void jsonPathToBeanExtractsNestedScalarValue() {
        var node = ContextUtils.OBJECT_MAPPER.createObjectNode()
                .set("user", ContextUtils.OBJECT_MAPPER.createObjectNode().put("name", "John"));

        // JSON Pointer expressions must start with '/'.
        String name = BeanUtils.jsonPathToBean(node, "/user/name", String.class);

        assertThat(name).isEqualTo("John");
    }

    @Test
    void jsonPathToBeanExtractsNestedObject() {
        var node = ContextUtils.OBJECT_MAPPER.createObjectNode()
                .set("user", ContextUtils.OBJECT_MAPPER.createObjectNode().put("name", "John"));

        UserName user = BeanUtils.jsonPathToBean(node, "/user", UserName.class);

        assertThat(user.name()).isEqualTo("John");
    }

    @Test
    void jsonPathToBeanThrowsForMissingPath() {
        var node = ContextUtils.OBJECT_MAPPER.createObjectNode().put("name", "John");

        assertThatThrownBy(() -> BeanUtils.jsonPathToBean(node, "/user", String.class))
                .isInstanceOf(JsonPointerException.class);
    }

    @Test
    void serializeUserAuditorResolvesAuditorField() {
        UserAuditorAware aware = mock(UserAuditorAware.class);
        UUID code = UUID.randomUUID();
        when(aware.loadByCode(code)).thenReturn(reactor.core.publisher.Mono.just(UserAuditor.of(code, "Resolved")));
        BeanUtils.USER_AUDITOR_AWARE = aware;

        AuditedBean bean = new AuditedBean();
        bean.setCreatedBy(UserAuditor.withCode(code));

        BeanUtils.serializeUserAuditor(bean).block();

        assertThat(bean.getCreatedBy().name()).isEqualTo("Resolved");
    }

    @Test
    void serializeUserAuditorReturnsObjectWhenNoAuditorField() {
        PlainBean bean = new PlainBean();
        bean.setValue("hi");

        PlainBean result = BeanUtils.serializeUserAuditor(bean).block();

        assertThat(result).isSameAs(bean);
        assertThat(result.getValue()).isEqualTo("hi");
    }

    // ---- test fixtures -----------------------------------------------------

    record UserName(String name) {
    }

    static class Person {
        private String name;
        private Integer age;
        private String fullName;

        Person() {
        }

        Person(String name, Integer age, String fullName) {
            this.name = name;
            this.age = age;
            this.fullName = fullName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Person p)) {
                return false;
            }
            return java.util.Objects.equals(name, p.name)
                    && java.util.Objects.equals(age, p.age)
                    && java.util.Objects.equals(fullName, p.fullName);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, age, fullName);
        }
    }

    static class PersonDto {
        private String name;
        private Integer age;
        private String fullName;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PersonDto p)) {
                return false;
            }
            return java.util.Objects.equals(name, p.name)
                    && java.util.Objects.equals(age, p.age)
                    && java.util.Objects.equals(fullName, p.fullName);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, age, fullName);
        }
    }

    static class PlainBean {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    static class AuditedBean {
        private UserAuditor createdBy;

        public UserAuditor getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(UserAuditor createdBy) {
            this.createdBy = createdBy;
        }
    }
}
