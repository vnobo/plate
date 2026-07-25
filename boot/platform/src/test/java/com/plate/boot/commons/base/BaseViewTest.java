package com.plate.boot.commons.base;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Trivial structural test for the {@link BaseView} marker interfaces (no behaviour to exercise).
 */
class BaseViewTest {

    @Test
    void nestedViewInterfacesAreInterfacesWithExpectedHierarchy() {
        assertThat(BaseView.Public.class).isInterface();
        assertThat(BaseView.Hidden.class).isInterface();
        assertThat(BaseView.Detail.class).isInterface();
        assertThat(BaseView.Admin.class).isInterface();

        // Admin -> Detail -> Public
        assertThat(BaseView.Public.class.isAssignableFrom(BaseView.Detail.class)).isTrue();
        assertThat(BaseView.Public.class.isAssignableFrom(BaseView.Admin.class)).isTrue();
        assertThat(BaseView.Detail.class.isAssignableFrom(BaseView.Admin.class)).isTrue();
    }
}
