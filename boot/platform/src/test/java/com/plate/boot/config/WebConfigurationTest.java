package com.plate.boot.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link WebConfiguration}.
 */
class WebConfigurationTest {

    @Test
    void configureArgumentResolversRegistersPageableResolverWithSizes() throws Exception {
        WebfluxProperties properties = new WebfluxProperties();
        properties.setMaxPageSize(150);
        properties.setDefaultPageSize(30);
        WebConfiguration configuration = new WebConfiguration(properties);
        ArgumentResolverConfigurer configurer = mock(ArgumentResolverConfigurer.class);

        configuration.configureArgumentResolvers(configurer);

        ArgumentCaptor<ReactivePageableHandlerMethodArgumentResolver> captor =
                ArgumentCaptor.forClass(ReactivePageableHandlerMethodArgumentResolver.class);
        verify(configurer).addCustomResolver(captor.capture());

        ReactivePageableHandlerMethodArgumentResolver resolver = captor.getValue();
        assertThat(readField(resolver, "maxPageSize")).isEqualTo(150);
        assertThat(readField(resolver, "fallbackPageable")).isEqualTo(Pageable.ofSize(30));
    }

    @Test
    void configurePathMatchingRegistersEveryRouteDefinition() {
        WebfluxProperties properties = new WebfluxProperties();
        WebfluxProperties.RouteDefinition rel = new WebfluxProperties.RouteDefinition();
        rel.setPath("/rel");
        rel.setBasePackage("com.plate.boot.relational");
        WebfluxProperties.RouteDefinition sec = new WebfluxProperties.RouteDefinition();
        sec.setPath("/sec");
        sec.setBasePackage("com.plate.boot.security");
        properties.setPathPrefixes(List.of(rel, sec));

        WebConfiguration configuration = new WebConfiguration(properties);
        PathMatchConfigurer configurer = mock(PathMatchConfigurer.class);

        configuration.configurePathMatching(configurer);

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(configurer, times(2)).addPathPrefix(pathCaptor.capture(), org.mockito.ArgumentMatchers.any());
        assertThat(pathCaptor.getAllValues()).containsExactly("/rel", "/sec");
    }

    @Test
    void configurePathMatchingSkipsEmptyPrefixes() {
        WebConfiguration configuration = new WebConfiguration(new WebfluxProperties());
        PathMatchConfigurer configurer = mock(PathMatchConfigurer.class);

        configuration.configurePathMatching(configurer);

        verify(configurer, times(0)).addPathPrefix(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private Object readField(Object target, String fieldName) throws Exception {
        Class<?> clazz = target.getClass();
        Field field = null;
        while (clazz != null && field == null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }
        field.setAccessible(true);
        return field.get(target);
    }
}
