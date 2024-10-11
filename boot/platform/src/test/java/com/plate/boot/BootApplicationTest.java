package com.plate.boot;

import com.plate.boot.config.WebConfigurationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.security.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://github.com/vnobo">Alex Bob</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.profiles.active=test")
@Import({WebConfigurationTest.class})
class BootApplicationTest {

    @Test
    void contextLoadsTest() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        assertThat(privateKey).isNotNull();
        assertThat(publicKey).isNotNull();
    }
}