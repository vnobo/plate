package com.plate.boot;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit test for the {@link BootApplication} entry point.
 * <p>
 * The {@code main} method only delegates to {@link SpringApplication#run(Class, String...)}. The
 * static call is mocked here so no Spring context, database, or Redis is started; a real container
 * boot is covered by the integration tests instead.
 */
class BootApplicationTest {

    @Test
    void mainDelegatesToSpringApplicationRun() {
        try (MockedStatic<SpringApplication> application = mockStatic(SpringApplication.class)) {
            BootApplication.main(new String[]{"--server.port=0"});

            application.verify(() -> SpringApplication.run(eq(BootApplication.class), any(String[].class)));
        }
    }
}
