package com.plate.boot.relational.dictionaries;

import com.plate.boot.AbstractIntegrationTests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DictionariesController}.
 * <p>
 * Verifies the routing fix: the controller no longer hardcodes the {@code /rel} prefix in its
 * {@code @RequestMapping}, so endpoints are exposed once under the framework-applied prefix
 * (e.g. {@code /rel/dictionaries}) and NOT under a doubled {@code /rel/rel/dictionaries} path.
 */
@DisplayName("Dictionaries")
class DictionariesIntegrationTests extends AbstractIntegrationTests {

    private String adminToken;

    @BeforeEach
    void setUp() {
        adminToken = loginAndGetToken(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    @Test
    @DisplayName("should expose dictionaries under the single path prefix")
    void shouldExposeDictionariesUnderSinglePrefix() {
        webTestClient.get().uri(relPrefix() + "/dictionaries")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("should not expose dictionaries under a doubled path prefix")
    void shouldNotExposeDictionariesUnderDoubledPrefix() {
        webTestClient.get().uri(relPrefix() + "/rel/dictionaries")
                .headers(headers -> headers.setBearerAuth(adminToken))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }
}
