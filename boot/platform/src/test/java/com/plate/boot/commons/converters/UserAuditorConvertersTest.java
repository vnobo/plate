package com.plate.boot.commons.converters;

import com.plate.boot.security.core.UserAuditor;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the pure converter logic in {@link UserAuditorConverters}.
 * No Spring / R2DBC connection is required.
 */
class UserAuditorConvertersTest {

    @Test
    void writeConverterReturnsCode() {
        UUID code = UUID.randomUUID();
        UserAuditor auditor = UserAuditor.of(code, "Alice");

        UUID result = new UserAuditorConverters.UserAuditorWriteConverter().convert(auditor);

        assertThat(result).isEqualTo(code);
    }

    @Test
    void readConverterBuildsUserAuditorWithCode() {
        UUID code = UUID.randomUUID();

        UserAuditor result = new UserAuditorConverters.UserAuditorReadConverter().convert(code);

        assertThat(result).isEqualTo(UserAuditor.withCode(code));
        assertThat(result.code()).isEqualTo(code);
        assertThat(result.name()).isNull();
    }
}
