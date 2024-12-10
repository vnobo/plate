package com.plate.boot;

import com.plate.boot.config.InfrastructureConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(classes = InfrastructureConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BootApplicationTest {

    @Test
    void contextLoads() {
        assert true;
    }
}