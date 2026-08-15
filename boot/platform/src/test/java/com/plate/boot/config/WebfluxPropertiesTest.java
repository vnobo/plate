package com.plate.boot.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link WebfluxProperties} and its inner {@link WebfluxProperties.RouteDefinition}.
 */
class WebfluxPropertiesTest {

    @Test
    void defaultsAreBoundedToDocumentedValues() {
        WebfluxProperties properties = new WebfluxProperties();

        assertThat(properties.getMaxPageSize()).isEqualTo(100);
        assertThat(properties.getDefaultPageSize()).isEqualTo(25);
        assertThat(properties.getPathPrefixes()).isNotNull().isEmpty();
    }

    @Test
    void settersUpdatePaginationProperties() {
        WebfluxProperties properties = new WebfluxProperties();

        properties.setMaxPageSize(200);
        properties.setDefaultPageSize(10);

        assertThat(properties.getMaxPageSize()).isEqualTo(200);
        assertThat(properties.getDefaultPageSize()).isEqualTo(10);
    }

    @Test
    void pathPrefixesCanBeReplaced() {
        WebfluxProperties properties = new WebfluxProperties();
        WebfluxProperties.RouteDefinition definition = new WebfluxProperties.RouteDefinition();
        definition.setPath("/api");
        definition.setBasePackage("com.example.api");

        properties.setPathPrefixes(List.of(definition));

        assertThat(properties.getPathPrefixes()).containsExactly(definition);
    }

    @Test
    void routeDefinitionDefaultsAndSetters() {
        WebfluxProperties.RouteDefinition definition = new WebfluxProperties.RouteDefinition();

        assertThat(definition.getPath()).isNull();
        assertThat(definition.getBasePackage()).isNull();

        definition.setPath("/sec");
        definition.setBasePackage("com.plate.boot.security");

        assertThat(definition.getPath()).isEqualTo("/sec");
        assertThat(definition.getBasePackage()).isEqualTo("com.plate.boot.security");
    }

    @Test
    void equalsAndHashCodeAreFieldBased() {
        WebfluxProperties a = new WebfluxProperties();
        WebfluxProperties b = new WebfluxProperties();
        WebfluxProperties c = new WebfluxProperties();
        c.setMaxPageSize(200);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo(c).doesNotHaveSameHashCodeAs(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("not-a-properties");
    }

    @Test
    void toStringContainsFieldNames() {
        WebfluxProperties properties = new WebfluxProperties();
        properties.setMaxPageSize(200);

        String s = properties.toString();
        assertThat(s).contains("maxPageSize", "defaultPageSize", "pathPrefixes");
    }

    @Test
    void routeDefinitionEqualsHashCodeToString() {
        WebfluxProperties.RouteDefinition a = new WebfluxProperties.RouteDefinition();
        a.setPath("/api");
        a.setBasePackage("com.example.api");

        WebfluxProperties.RouteDefinition b = new WebfluxProperties.RouteDefinition();
        b.setPath("/api");
        b.setBasePackage("com.example.api");

        WebfluxProperties.RouteDefinition c = new WebfluxProperties.RouteDefinition();
        c.setPath("/sec");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("different-type");
        assertThat(a.toString()).contains("path", "basePackage");
    }
}
